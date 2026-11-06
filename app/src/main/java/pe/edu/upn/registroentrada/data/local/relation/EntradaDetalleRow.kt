package pe.edu.upn.registroentrada.data.local.relation

/** Fila plana con la entrada y los datos que necesita la interfaz. */
data class EntradaDetalleRow(
    val entradaId: Long,
    val codigo: String,
    val firma: String,
    val eventoId: Long,
    val tipoEntradaId: Long,
    val asistenteId: Long,
    val estado: String,
    val emitidaEn: Long,
    val usadaEn: Long?,
    val nombreAsistente: String?,
    val documentoAsistente: String?,
    val nombreTipoEntrada: String,
    val colorTipoEntrada: String,
    val nombreEvento: String,
    val lugarEvento: String
)
