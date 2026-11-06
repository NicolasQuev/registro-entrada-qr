package pe.edu.upn.registroentrada.domain.model

import java.time.Instant

/** Estado de una entrada emitida. */
enum class EstadoEntrada {
    VALIDA,
    USADA,
    ANULADA;

    companion object {
        fun desde(texto: String): EstadoEntrada =
            entries.firstOrNull { it.name == texto } ?: VALIDA
    }
}

/**
 * Entrada emitida. El `codigo` es el UUID que viaja en el QR y la `firma`
 * es su HMAC truncado con la clave del dispositivo.
 */
data class Entrada(
    val id: Long = 0,
    val codigo: String,
    val firma: String,
    val eventoId: Long,
    val tipoEntradaId: Long,
    val asistenteId: Long? = null,
    val estado: EstadoEntrada = EstadoEntrada.VALIDA,
    val emitidaEn: Instant,
    val usadaEn: Instant? = null
)

/** Entrada con los datos de presentación ya resueltos (para listas y detalle). */
data class EntradaDetalle(
    val entrada: Entrada,
    val nombreAsistente: String?,
    val documentoAsistente: String?,
    val nombreTipoEntrada: String,
    val colorTipoEntrada: String,
    val nombreEvento: String,
    val lugarEvento: String
)
