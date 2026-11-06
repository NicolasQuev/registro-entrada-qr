package pe.edu.upn.registroentrada.core

/**
 * Validaciones de datos de entrada del usuario. Se aíslan aquí para poder
 * probarlas en la JVM sin Android (RNF-30) y reutilizarlas en los casos de uso.
 */
object ValidadorDocumento {

    private val regexDni = Regex("^\\d{8}$")
    private val regexCorreo = Regex("^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$")
    private val regexTelefono = Regex("^\\d{6,15}$")

    /** DNI peruano: exactamente 8 dígitos numéricos. */
    fun documentoValido(documento: String): Boolean = regexDni.matches(documento.trim())

    /** Correo con forma básica `usuario@dominio.tld`. */
    fun correoValido(correo: String): Boolean = regexCorreo.matches(correo.trim())

    /** Teléfono de 6 a 15 dígitos. */
    fun telefonoValido(telefono: String): Boolean = regexTelefono.matches(telefono.trim())

    /** Mensaje de error del documento, o null si es válido. */
    fun errorDocumento(documento: String): String? = when {
        documento.isBlank() -> "El documento es obligatorio"
        !documentoValido(documento) -> "El documento debe tener 8 dígitos numéricos"
        else -> null
    }

    /** Mensaje de error del correo (opcional), o null si es válido o vacío. */
    fun errorCorreo(correo: String): String? {
        val limpio = correo.trim()
        return if (limpio.isNotEmpty() && !correoValido(limpio)) {
            "Ingresa un correo válido (ejemplo@dominio.com)"
        } else {
            null
        }
    }

    /** Mensaje de error del teléfono (opcional), o null si es válido o vacío. */
    fun errorTelefono(telefono: String): String? {
        val limpio = telefono.trim()
        return if (limpio.isNotEmpty() && !telefonoValido(limpio)) {
            "El teléfono debe tener entre 6 y 15 dígitos"
        } else {
            null
        }
    }
}
