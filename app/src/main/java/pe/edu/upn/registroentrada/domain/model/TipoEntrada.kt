package pe.edu.upn.registroentrada.domain.model

/** Tipo de entrada de un evento (general, preventa, VIP), con cupo y precio. */
data class TipoEntrada(
    val id: Long = 0,
    val eventoId: Long,
    val nombre: String,
    val precio: Double,
    val cupo: Int,
    val color: String
)
