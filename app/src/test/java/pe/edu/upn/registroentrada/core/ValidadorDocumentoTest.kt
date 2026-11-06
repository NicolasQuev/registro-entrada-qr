package pe.edu.upn.registroentrada.core

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class ValidadorDocumentoTest {

    @Test
    fun `documento de 8 digitos es valido`() {
        assertTrue(ValidadorDocumento.documentoValido("12345678"))
        assertNull(ValidadorDocumento.errorDocumento("12345678"))
    }

    @Test
    fun `documento con letras o longitud incorrecta es invalido`() {
        assertFalse(ValidadorDocumento.documentoValido("1234567"))
        assertFalse(ValidadorDocumento.documentoValido("123456789"))
        assertFalse(ValidadorDocumento.documentoValido("1234abcd"))
        assertEquals(
            "El documento debe tener 8 dígitos numéricos",
            ValidadorDocumento.errorDocumento("1234abcd")
        )
    }

    @Test
    fun `correo vacio es valido por ser opcional`() {
        assertNull(ValidadorDocumento.errorCorreo(""))
        assertTrue(ValidadorDocumento.correoValido("alguien@dominio.com"))
        assertEquals(
            "Ingresa un correo válido (ejemplo@dominio.com)",
            ValidadorDocumento.errorCorreo("sin-arroba")
        )
    }

    @Test
    fun `telefono vacio es valido y fuera de rango no`() {
        assertNull(ValidadorDocumento.errorTelefono(""))
        assertTrue(ValidadorDocumento.telefonoValido("987654321"))
        assertFalse(ValidadorDocumento.telefonoValido("123"))
    }
}
