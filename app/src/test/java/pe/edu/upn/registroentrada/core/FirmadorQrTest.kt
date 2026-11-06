package pe.edu.upn.registroentrada.core

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class FirmadorQrTest {

    private val firmador = FirmadorQrHmac("clave-de-prueba".toByteArray())

    @Test
    fun `la firma tiene 8 bytes en hexadecimal`() {
        val firma = firmador.firmar("codigo-1")
        assertEquals(16, firma.length)
        assertTrue(firma.matches(Regex("^[0-9a-f]+$")))
    }

    @Test
    fun `verify acepta la firma correcta y rechaza una alterada`() {
        val firma = firmador.firmar("codigo-1")
        assertTrue(firmador.verificar("codigo-1", firma))
        assertFalse(firmador.verificar("codigo-2", firma))
    }

    @Test
    fun `empaquetar y desempaquetar son inversos`() {
        val texto = firmador.empaquetar("uuid-abc")
        assertTrue(texto.startsWith("RE|1|"))
        val contenido = firmador.desempaquetar(texto)
        assertEquals("uuid-abc", contenido?.codigo)
        assertTrue(firmador.verificar(contenido!!.codigo, contenido.firma))
    }

    @Test
    fun `un texto con formato distinto no se puede desempaquetar`() {
        assertNull(firmador.desempaquetar("codigo-suelto"))
        assertNull(firmador.desempaquetar("RE|2|uuid|firma"))
        assertNull(firmador.desempaquetar("XX|1|uuid|firma"))
    }
}
