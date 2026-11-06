package pe.edu.upn.registroentrada.domain.usecase

import java.time.Instant
import javax.inject.Inject
import kotlinx.coroutines.flow.first
import pe.edu.upn.registroentrada.core.FirmadorQr
import pe.edu.upn.registroentrada.core.Reloj
import pe.edu.upn.registroentrada.domain.model.EntradaDetalle
import pe.edu.upn.registroentrada.domain.model.EstadoEntrada
import pe.edu.upn.registroentrada.domain.model.EstadoEvento
import pe.edu.upn.registroentrada.domain.model.RegistroAcceso
import pe.edu.upn.registroentrada.domain.model.ResultadoAcceso
import pe.edu.upn.registroentrada.domain.repository.AccesoRepository
import pe.edu.upn.registroentrada.domain.repository.EntradaRepository
import pe.edu.upn.registroentrada.domain.repository.EventoRepository

/** Resultado de validar un escaneo, listo para pintar en la pantalla. */
data class ValidacionAcceso(
    val registro: RegistroAcceso,
    val permitido: Boolean,
    val detalleEntrada: EntradaDetalle?,
    val observacion: String? = null
)

/**
 * Corazón del producto: decide si un código permite el ingreso y deja
 * constancia de TODOS los intentos (RN-01, RN-04, RN-05, RN-06, RN-09,
 * RN-11, RN-13, RNF-13, RNF-17).
 *
 * Acepta el texto completo de un QR (`RE|1|codigo|firma`) o el código
 * escrito a mano cuando la cámara no puede leerlo (HU-15).
 */
class RegistrarAccesoUseCase @Inject constructor(
    private val entradas: EntradaRepository,
    private val accesos: AccesoRepository,
    private val eventos: EventoRepository,
    private val firmador: FirmadorQr,
    private val reloj: Reloj
) {
    suspend operator fun invoke(
        textoQr: String,
        eventoActivoId: Long,
        puerta: String,
        operadorId: Long,
        observacion: String? = null
    ): ValidacionAcceso {
        val ahora = reloj.ahora()
        val texto = textoQr.trim()

        val contenido = firmador.desempaquetar(texto)
        val esQr = contenido != null || texto.startsWith(FirmadorQr.PREFIJO)
        val firmaValida = contenido == null || firmador.verificar(contenido.codigo, contenido.firma)
        val codigo = contenido?.codigo ?: texto

        val detalle = if (esQr && !firmaValida) null else entradas.buscarPorCodigo(codigo)
        val evento = eventos.observarEvento(eventoActivoId).first()

        val resultado: ResultadoAcceso = when {
            // RNF-13: el QR existe pero la firma no corresponde a la clave local.
            esQr && !firmaValida -> ResultadoAcceso.FIRMA_INVALIDA
            detalle == null -> ResultadoAcceso.NO_EXISTE
            detalle.entrada.eventoId != eventoActivoId -> ResultadoAcceso.OTRO_EVENTO
            detalle.entrada.estado == EstadoEntrada.USADA -> ResultadoAcceso.YA_USADA
            detalle.entrada.estado == EstadoEntrada.ANULADA -> ResultadoAcceso.ANULADA
            evento?.estado == EstadoEvento.CANCELADO -> ResultadoAcceso.EVENTO_CANCELADO
            accesos.observarAforo(eventoActivoId).first().completo -> ResultadoAcceso.AFORO_LLENO
            else -> ResultadoAcceso.PERMITIDO
        }

        return registrar(resultado, detalle, codigo, eventoActivoId, puerta, operadorId, ahora, observacion)
    }

    private suspend fun registrar(
        resultado: ResultadoAcceso,
        detalle: EntradaDetalle?,
        codigo: String,
        eventoActivoId: Long,
        puerta: String,
        operadorId: Long,
        ahora: Instant,
        observacion: String?
    ): ValidacionAcceso {
        val id = accesos.registrarAcceso(
            registro = RegistroAcceso(
                entradaId = detalle?.entrada?.id,
                eventoId = eventoActivoId,
                codigoEscaneado = codigo,
                resultado = resultado,
                puerta = puerta,
                operadorId = operadorId,
                fechaHora = ahora,
                observacion = observacion
            ),
            marcarEntradaUsada = resultado == ResultadoAcceso.PERMITIDO
        )
        return ValidacionAcceso(
            registro = RegistroAcceso(
                id = id,
                entradaId = detalle?.entrada?.id,
                eventoId = eventoActivoId,
                codigoEscaneado = codigo,
                resultado = resultado,
                puerta = puerta,
                operadorId = operadorId,
                fechaHora = ahora,
                observacion = observacion
            ),
            permitido = resultado.permitido,
            detalleEntrada = detalle,
            observacion = observacion
        )
    }
}
