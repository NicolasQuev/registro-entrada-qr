package pe.edu.upn.registroentrada.domain.model

import java.time.Instant

/** Ciclo de vida de un evento del local. */
enum class EstadoEvento {
    BORRADOR,
    PROGRAMADO,
    EN_CURSO,
    FINALIZADO,
    CANCELADO;

    companion object {
        fun desde(texto: String): EstadoEvento =
            entries.firstOrNull { it.name == texto } ?: BORRADOR
    }
}

/** Evento del local: concierto, conferencia, feria, etc. */
data class Evento(
    val id: Long = 0,
    val remoteId: String? = null,
    val nombre: String,
    val descripcion: String? = null,
    val lugar: String,
    val latitud: Double? = null,
    val longitud: Double? = null,
    val fechaInicio: Instant,
    val fechaFin: Instant,
    val aforoMaximo: Int,
    val estado: EstadoEvento = EstadoEvento.PROGRAMADO,
    val creadoEn: Instant
) {
    val admiteIngresos: Boolean
        get() = estado != EstadoEvento.CANCELADO && estado != EstadoEvento.FINALIZADO
}
