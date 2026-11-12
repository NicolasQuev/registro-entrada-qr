package pe.edu.upn.registroentrada.core

/** Valida coordenadas geográficas (latitud y longitud) para eventos con mapa. */
object ValidadorCoordenadas {

    fun validar(latitud: Double?, longitud: Double?): String? = when {
        (latitud == null) != (longitud == null) ->
            "Debes indicar tanto la latitud como la longitud"
        latitud != null && (!latitud.isFinite() || latitud !in -90.0..90.0) ->
            "La latitud debe estar entre -90 y 90"
        longitud != null && (!longitud.isFinite() || longitud !in -180.0..180.0) ->
            "La longitud debe estar entre -180 y 180"
        else -> null
    }
}
