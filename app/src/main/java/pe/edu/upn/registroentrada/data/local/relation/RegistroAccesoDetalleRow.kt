package pe.edu.upn.registroentrada.data.local.relation

/** Fila plana de un registro de acceso con el nombre del operador y del asistente. */
data class RegistroAccesoDetalleRow(
    val id: Long,
    val entradaId: Long?,
    val eventoId: Long,
    val codigoEscaneado: String,
    val resultado: String,
    val puerta: String,
    val operadorId: Long,
    val fechaHora: Long,
    val observacion: String?,
    val nombreOperador: String,
    val nombreAsistente: String?
)
