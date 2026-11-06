package pe.edu.upn.registroentrada.domain.usecase

import java.time.Instant
import javax.inject.Inject
import kotlinx.coroutines.flow.first
import pe.edu.upn.registroentrada.core.Reloj
import pe.edu.upn.registroentrada.core.Resultado
import pe.edu.upn.registroentrada.domain.model.AjustesUsuario
import pe.edu.upn.registroentrada.domain.model.EstadoEvento
import pe.edu.upn.registroentrada.domain.model.Evento
import pe.edu.upn.registroentrada.domain.model.TipoEntrada
import pe.edu.upn.registroentrada.domain.repository.AccesoRepository
import pe.edu.upn.registroentrada.domain.repository.AjustesRepository
import pe.edu.upn.registroentrada.domain.repository.EventoRepository
import pe.edu.upn.registroentrada.domain.model.ResultadoAcceso

/**
 * Crea un evento aplicando las validaciones de HU-01. Devuelve el id
 * generado para que la pantalla pueda navegar al detalle.
 */
class CrearEventoUseCase @Inject constructor(
    private val eventos: EventoRepository,
    private val reloj: Reloj
) {
    suspend operator fun invoke(
        nombre: String,
        descripcion: String?,
        lugar: String,
        fechaInicio: Instant,
        fechaFin: Instant,
        aforoMaximo: Int,
        latitud: Double? = null,
        longitud: Double? = null
    ): Resultado<Long> {
        validar(nombre, lugar, fechaInicio, fechaFin, aforoMaximo)?.let { return it }
        val evento = Evento(
            nombre = nombre.trim(),
            descripcion = descripcion?.trim()?.ifBlank { null },
            lugar = lugar.trim(),
            latitud = latitud,
            longitud = longitud,
            fechaInicio = fechaInicio,
            fechaFin = fechaFin,
            aforoMaximo = aforoMaximo,
            estado = EstadoEvento.PROGRAMADO,
            creadoEn = reloj.ahora()
        )
        return Resultado.Exito(eventos.guardar(evento))
    }

    private fun validar(
        nombre: String,
        lugar: String,
        fechaInicio: Instant,
        fechaFin: Instant,
        aforoMaximo: Int
    ): Resultado.Error? = when {
        nombre.isBlank() -> Resultado.Error("El nombre del evento es obligatorio")
        lugar.isBlank() -> Resultado.Error("El lugar del evento es obligatorio")
        aforoMaximo <= 0 -> Resultado.Error("El aforo debe ser un número mayor a 0")
        fechaFin.isBefore(fechaInicio) ->
            Resultado.Error("La fecha de fin no puede ser anterior a la fecha de inicio")
        else -> null
    }
}

/** Edita un evento existente (HU-03) respetando la regla RN-14 del aforo. */
class ActualizarEventoUseCase @Inject constructor(
    private val eventos: EventoRepository,
    private val accesos: AccesoRepository
) {
    suspend operator fun invoke(
        eventoId: Long,
        nombre: String,
        descripcion: String?,
        lugar: String,
        fechaInicio: Instant,
        fechaFin: Instant,
        aforoMaximo: Int
    ): Resultado<Unit> {
        val actual = eventos.observarEvento(eventoId).first()
            ?: return Resultado.Error("El evento ya no existe")
        when {
            nombre.isBlank() -> return Resultado.Error("El nombre del evento es obligatorio")
            lugar.isBlank() -> return Resultado.Error("El lugar del evento es obligatorio")
            aforoMaximo <= 0 -> return Resultado.Error("El aforo debe ser un número mayor a 0")
            fechaFin.isBefore(fechaInicio) ->
                return Resultado.Error("La fecha de fin no puede ser anterior a la fecha de inicio")
        }
        val ingresos = accesos.contarPorResultado(eventoId, ResultadoAcceso.PERMITIDO)
        if (aforoMaximo < ingresos) {
            return Resultado.Error("El nuevo aforo es menor a los ingresos registrados")
        }
        eventos.actualizar(
            actual.copy(
                nombre = nombre.trim(),
                descripcion = descripcion?.trim()?.ifBlank { null },
                lugar = lugar.trim(),
                fechaInicio = fechaInicio,
                fechaFin = fechaFin,
                aforoMaximo = aforoMaximo
            )
        )
        return Resultado.Exito(Unit)
    }
}

/** Define un tipo de entrada con cupo y precio (HU-04, RN-02). */
class GuardarTipoEntradaUseCase @Inject constructor(
    private val eventos: EventoRepository
) {
    suspend operator fun invoke(
        tipoId: Long,
        eventoId: Long,
        nombre: String,
        precio: Double,
        cupo: Int,
        color: String
    ): Resultado<Long> {
        when {
            nombre.isBlank() -> return Resultado.Error("El nombre del tipo de entrada es obligatorio")
            precio < 0 -> return Resultado.Error("El precio no puede ser negativo")
            cupo < 1 -> return Resultado.Error("El cupo debe ser al menos 1")
        }
        val existentes = eventos.observarTiposDeEntrada(eventoId).first()
        val duplicado = existentes.any {
            it.nombre.equals(nombre.trim(), ignoreCase = true) && it.id != tipoId
        }
        if (duplicado) {
            return Resultado.Error("Ya existe un tipo de entrada con ese nombre")
        }
        val id = eventos.guardarTipoDeEntrada(
            TipoEntrada(
                id = tipoId,
                eventoId = eventoId,
                nombre = nombre.trim(),
                precio = precio,
                cupo = cupo,
                color = color
            )
        )
        return Resultado.Exito(id)
    }
}

/**
 * Elimina un evento creado por error. La base borra en cascada sus tipos,
 * entradas y registros de acceso. Si era el evento activo, se deselecciona.
 */
class EliminarEventoUseCase @Inject constructor(
    private val eventos: EventoRepository,
    private val ajustes: AjustesRepository
) {
    suspend operator fun invoke(eventoId: Long): Resultado<Unit> {
        eventos.observarEvento(eventoId).first()
            ?: return Resultado.Error("El evento ya no existe")
        val preferencias = ajustes.ajustes.first()
        eventos.eliminarEvento(eventoId)
        if (preferencias.eventoActivoId == eventoId) {
            ajustes.guardarEventoActivo(
                AjustesUsuario.SIN_EVENTO,
                preferencias.puertaActiva
            )
        }
        return Resultado.Exito(Unit)
    }
}

/** Elimina un tipo de entrada que todavía no tenga entradas emitidas (HU-04). */
class EliminarTipoEntradaUseCase @Inject constructor(
    private val eventos: EventoRepository
) {
    suspend operator fun invoke(tipoId: Long): Resultado<Unit> {
        if (eventos.contarEntradasVigentesPorTipo(tipoId) > 0) {
            return Resultado.Error("Este tipo tiene entradas emitidas; anúlalas primero")
        }
        return try {
            eventos.eliminarTipoDeEntrada(tipoId)
            Resultado.Exito(Unit)
        } catch (_: Exception) {
            Resultado.Error("No se puede eliminar: el tipo tiene entradas asociadas")
        }
    }
}

/** Caso de uso para sincronizar eventos con la API REST en la nube */
class SincronizarEventosRestUseCase @Inject constructor(
    private val eventos: EventoRepository
) {
    suspend operator fun invoke(): Resultado<Int> {
        val res = eventos.sincronizarConApiRest()
        return res.fold(
            onSuccess = { Resultado.Exito(it) },
            onFailure = { Resultado.Error(it.message ?: "Error de comunicación con API REST") }
        )
    }
}
