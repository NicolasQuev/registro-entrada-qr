package pe.edu.upn.registroentrada.domain.model

/** Resumen de resultados de un evento para la pantalla de reportes (HU-20). */
data class ResumenEvento(
    val entradasEmitidas: Int,
    val entradasValidas: Int,
    val entradasUsadas: Int,
    val entradasAnuladas: Int,
    val ingresosPermitidos: Int,
    val rechazosPorMotivo: Map<ResultadoAcceso, Int>,
    val aforo: Aforo
) {
    val totalIntentos: Int
        get() = ingresosPermitidos + rechazosPorMotivo.values.sum()

    val porcentajeOcupacion: Float
        get() = aforo.porcentaje
}
