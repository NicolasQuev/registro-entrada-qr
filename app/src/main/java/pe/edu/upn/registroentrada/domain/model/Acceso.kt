package pe.edu.upn.registroentrada.domain.model

import java.time.Instant

/**
 * Motivo registrado de cada intento de acceso. Se guardan también los
 * rechazos porque son la evidencia de auditoría (RNF-17).
 */
enum class ResultadoAcceso {
    PERMITIDO,
    YA_USADA,
    NO_EXISTE,
    OTRO_EVENTO,
    ANULADA,
    FIRMA_INVALIDA,
    AFORO_LLENO,
    EVENTO_CANCELADO;

    val permitido: Boolean
        get() = this == PERMITIDO

    companion object {
        fun desde(texto: String): ResultadoAcceso =
            entries.firstOrNull { it.name == texto } ?: NO_EXISTE
    }
}

/** Registro inmutable de un escaneo, permitido o rechazado. */
data class RegistroAcceso(
    val id: Long = 0,
    val entradaId: Long? = null,
    val eventoId: Long,
    val codigoEscaneado: String,
    val resultado: ResultadoAcceso,
    val puerta: String,
    val operadorId: Long,
    val fechaHora: Instant,
    val observacion: String? = null
)

/** Registro con el nombre del operador (y del asistente si se reconoció). */
data class RegistroAccesoDetalle(
    val registro: RegistroAcceso,
    val nombreOperador: String,
    val nombreAsistente: String?
)

/** Aforo calculado a partir de los ingresos permitidos (RN-05). */
data class Aforo(
    val ocupados: Int,
    val maximo: Int
) {
    val disponible: Int get() = (maximo - ocupados).coerceAtLeast(0)
    val sobrecupo: Int get() = (ocupados - maximo).coerceAtLeast(0)
    val completo: Boolean get() = maximo > 0 && ocupados >= maximo
    val porcentaje: Float get() = if (maximo <= 0) 0f else ocupados.toFloat() / maximo.toFloat()
}

/** Filtros del historial de accesos (HU-19). */
data class FiltroRegistros(
    val fechaDesde: Instant? = null,
    val fechaHasta: Instant? = null,
    val puerta: String? = null,
    val resultado: ResultadoAcceso? = null,
    val operadorId: Long? = null
)
