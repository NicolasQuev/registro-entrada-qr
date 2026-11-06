package pe.edu.upn.registroentrada.core

/**
 * Resultado de una operación del dominio. Evita usar excepciones para
 * los errores esperados (validaciones, cupos, credenciales) y obliga a
 * la capa de presentación a tratar el caso de error.
 */
sealed interface Resultado<out T> {

    data class Exito<T>(val valor: T) : Resultado<T>

    data class Error(val mensaje: String) : Resultado<Nothing>

    val esExito: Boolean
        get() = this is Exito

    fun obtenerOSerieError(): T = when (this) {
        is Exito -> valor
        is Error -> error(mensaje)
    }
}

/** Transforma el valor de un resultado exitoso conservando el error. */
inline fun <T, R> Resultado<T>.mapear(transformar: (T) -> R): Resultado<R> = when (this) {
    is Resultado.Exito -> Resultado.Exito(transformar(valor))
    is Resultado.Error -> this
}

/** Ejecuta una acción solo si el resultado fue exitoso. */
inline fun <T> Resultado<T>.alExito(accion: (T) -> Unit): Resultado<T> {
    if (this is Resultado.Exito) accion(valor)
    return this
}
