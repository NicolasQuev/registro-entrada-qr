package pe.edu.upn.registroentrada.domain.usecase

import javax.inject.Inject
import kotlinx.coroutines.flow.first
import pe.edu.upn.registroentrada.core.FirmadorQr
import pe.edu.upn.registroentrada.core.GeneradorId
import pe.edu.upn.registroentrada.core.Reloj
import pe.edu.upn.registroentrada.core.Resultado
import pe.edu.upn.registroentrada.domain.model.Entrada
import pe.edu.upn.registroentrada.domain.model.EstadoEntrada
import pe.edu.upn.registroentrada.domain.model.ResultadoAcceso
import pe.edu.upn.registroentrada.domain.model.ResumenEvento
import pe.edu.upn.registroentrada.domain.repository.AccesoRepository
import pe.edu.upn.registroentrada.domain.repository.EntradaRepository
import pe.edu.upn.registroentrada.domain.repository.EventoRepository

/**
 * Emite una o varias entradas genéricas con código UUID firmado. Cada entrada
 * recibe un id único (UUID v4 + índice único en la base) y guarda su hora de
 * emisión y estado. Aplica RN-02 (no superar el cupo del tipo).
 */
class EmitirEntradaUseCase @Inject constructor(
    private val eventos: EventoRepository,
    private val entradas: EntradaRepository,
    private val generadorId: GeneradorId,
    private val firmador: FirmadorQr,
    private val reloj: Reloj
) {
    suspend operator fun invoke(
        eventoId: Long,
        tipoEntradaId: Long,
        cantidad: Int
    ): Resultado<Int> {
        if (cantidad < 1) return Resultado.Error("Indica cuántas entradas vas a emitir")
        val tipo = eventos.obtenerTipoDeEntrada(tipoEntradaId)
            ?: return Resultado.Error("El tipo de entrada seleccionado no existe")
        if (tipo.eventoId != eventoId) {
            return Resultado.Error("El tipo de entrada no pertenece a este evento")
        }
        val vigentes = eventos.contarEntradasVigentesPorTipo(tipoEntradaId)
        val disponible = (tipo.cupo - vigentes).coerceAtLeast(0)
        if (cantidad > disponible) {
            val restante = if (disponible == 1) {
                "Queda 1 entrada disponible"
            } else {
                "Quedan $disponible entradas disponibles"
            }
            return Resultado.Error("$restante de este tipo")
        }

        val momento = reloj.ahora()
        repeat(cantidad) {
            val codigo = generadorId.nuevo()
            entradas.emitir(
                Entrada(
                    codigo = codigo,
                    firma = firmador.firmar(codigo),
                    eventoId = eventoId,
                    tipoEntradaId = tipoEntradaId,
                    asistenteId = null,
                    estado = EstadoEntrada.VALIDA,
                    emitidaEn = momento
                )
            )
        }
        return Resultado.Exito(cantidad)
    }
}

/** Anula una entrada válida (HU-09). Una entrada USADA no puede anularse (RN-07). */
class AnularEntradaUseCase @Inject constructor(
    private val entradas: EntradaRepository
) {
    suspend operator fun invoke(entradaId: Long): Resultado<Unit> {
        val detalle = entradas.observarEntrada(entradaId).first()
            ?: return Resultado.Error("La entrada ya no existe")
        return when (detalle.entrada.estado) {
            EstadoEntrada.USADA ->
                Resultado.Error("No se puede anular una entrada ya usada; registra una incidencia")
            EstadoEntrada.ANULADA ->
                Resultado.Error("La entrada ya está anulada")
            EstadoEntrada.VALIDA -> {
                entradas.anular(entradaId)
                Resultado.Exito(Unit)
            }
        }
    }
}

/** Arma el resumen de resultados del evento para la pantalla de reportes (HU-20). */
class ObtenerResumenEventoUseCase @Inject constructor(
    private val entradas: EntradaRepository,
    private val accesos: AccesoRepository
) {
    suspend operator fun invoke(eventoId: Long): ResumenEvento {
        val usadas = entradas.contarPorEventoYEstado(eventoId, EstadoEntrada.USADA)
        val validas = entradas.contarPorEventoYEstado(eventoId, EstadoEntrada.VALIDA)
        val anuladas = entradas.contarPorEventoYEstado(eventoId, EstadoEntrada.ANULADA)
        val rechazos = ResultadoAcceso.entries
            .filter { it != ResultadoAcceso.PERMITIDO }
            .mapNotNull { motivo ->
                val total = accesos.contarPorResultado(eventoId, motivo)
                if (total > 0) motivo to total else null
            }
            .toMap()
        return ResumenEvento(
            entradasEmitidas = usadas + validas + anuladas,
            entradasValidas = validas,
            entradasUsadas = usadas,
            entradasAnuladas = anuladas,
            ingresosPermitidos = accesos.contarPorResultado(eventoId, ResultadoAcceso.PERMITIDO),
            rechazosPorMotivo = rechazos,
            aforo = accesos.observarAforo(eventoId).first()
        )
    }
}
