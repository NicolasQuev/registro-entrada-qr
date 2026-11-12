package pe.edu.upn.registroentrada.core.location

/** Coordenadas geográficas obtenidas desde el dispositivo. */
data class Coordenadas(val latitud: Double, val longitud: Double)

/** Abstracción para obtener la ubicación GPS actual del dispositivo. */
fun interface ProveedorUbicacion {
    suspend fun obtenerUbicacionActual(): Result<Coordenadas>
}
