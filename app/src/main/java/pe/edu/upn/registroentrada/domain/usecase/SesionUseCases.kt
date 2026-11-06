package pe.edu.upn.registroentrada.domain.usecase

import java.time.Duration
import javax.inject.Inject
import kotlinx.coroutines.flow.first
import pe.edu.upn.registroentrada.core.Hasher
import pe.edu.upn.registroentrada.core.Reloj
import pe.edu.upn.registroentrada.core.Resultado
import pe.edu.upn.registroentrada.domain.model.EstadoEvento
import pe.edu.upn.registroentrada.domain.model.Operador
import pe.edu.upn.registroentrada.domain.model.TemaApp
import pe.edu.upn.registroentrada.domain.repository.AjustesRepository
import pe.edu.upn.registroentrada.domain.repository.EventoRepository
import pe.edu.upn.registroentrada.domain.repository.OperadorRepository

/**
 * Inicia sesión con usuario y PIN (HU-10). Aplica RNF-16: tres intentos
 * fallidos bloquean el acceso sesenta segundos.
 */
class IniciarSesionUseCase @Inject constructor(
    private val operadores: OperadorRepository,
    private val ajustes: AjustesRepository,
    private val reloj: Reloj
) {
    suspend operator fun invoke(usuario: String, pin: String): Resultado<Operador> {
        val ahora = reloj.ahora().toEpochMilli()
        val preferencias = ajustes.ajustes.first()

        if (ahora < preferencias.bloqueoHasta) {
            val restantes = ((preferencias.bloqueoHasta - ahora) / 1000).coerceAtLeast(1)
            return Resultado.Error("Acceso bloqueado. Intenta de nuevo en $restantes segundos")
        }

        val operador = operadores.buscarPorUsuario(usuario.trim())
            ?: return fallar(usuario, ahora)

        if (!operador.activo) {
            return Resultado.Error("El operador está desactivado")
        }

        return if (Hasher.verificarPin(pin, operador.pinSal, operador.pinHash)) {
            ajustes.reiniciarIntentos()
            ajustes.guardarOperadorActivo(operador.id, operador.nombre)
            Resultado.Exito(operador)
        } else {
            fallar(usuario, ahora)
        }
    }

    private suspend fun fallar(usuario: String, ahora: Long): Resultado.Error {
        val intentos = ajustes.registrarIntentoFallido()
        return if (intentos >= MAX_INTENTOS) {
            ajustes.bloquearHasta(java.time.Instant.ofEpochMilli(ahora + bloqueoMs))
            Resultado.Error("Demasiados intentos fallidos. Acceso bloqueado 60 segundos")
        } else {
            Resultado.Error("PIN incorrecto, intento $intentos de $MAX_INTENTOS")
        }
    }

    private companion object {
        const val MAX_INTENTOS = 3
        val bloqueoMs: Long = Duration.ofSeconds(60).toMillis()
    }
}

/** Guarda el evento que se va a atender y la puerta (HU-11, RN-11). */
class SeleccionarEventoActivoUseCase @Inject constructor(
    private val eventos: EventoRepository,
    private val ajustes: AjustesRepository
) {
    suspend operator fun invoke(eventoId: Long, puerta: String): Resultado<Unit> {
        val evento = eventos.observarEvento(eventoId).first()
            ?: return Resultado.Error("El evento ya no existe")
        if (evento.estado == EstadoEvento.CANCELADO) {
            return Resultado.Error("No puedes atender un evento cancelado")
        }
        if (evento.estado == EstadoEvento.FINALIZADO) {
            return Resultado.Error("Ese evento ya finalizó")
        }
        ajustes.guardarEventoActivo(eventoId, puerta.trim().ifBlank { "P1" })
        return Resultado.Exito(Unit)
    }
}

/** Persiste las preferencias de la pantalla de ajustes (HU-23, HT-04). */
class GuardarPreferenciasUseCase @Inject constructor(
    private val ajustes: AjustesRepository
) {
    suspend fun tema(tema: TemaApp) = ajustes.guardarTema(tema)
    suspend fun sonido(activo: Boolean) = ajustes.guardarSonido(activo)
    suspend fun vibracion(activa: Boolean) = ajustes.guardarVibracion(activa)
    suspend fun puerta(puerta: String) {
        val actual = ajustes.ajustes.first()
        ajustes.guardarEventoActivo(actual.eventoActivoId, puerta)
    }
}

/** Cierra la sesión del operador sin borrar los datos de negocio. */
class CerrarSesionUseCase @Inject constructor(
    private val ajustes: AjustesRepository
) {
    suspend operator fun invoke() = ajustes.limpiarOperadorActivo()
}
