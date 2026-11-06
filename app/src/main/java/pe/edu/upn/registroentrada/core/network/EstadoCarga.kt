package pe.edu.upn.registroentrada.core.network

/**
 * Representa el estado de una operación de red o carga en la UI (LCE: Loading-Content-Error).
 */
sealed interface EstadoCarga<out T> {
    data object Inactivo : EstadoCarga<Nothing>
    data object Cargando : EstadoCarga<Nothing>
    data class Exito<T>(val datos: T) : EstadoCarga<T>
    data class Error(val mensaje: String, val excepcion: Throwable? = null) : EstadoCarga<Nothing>
}
