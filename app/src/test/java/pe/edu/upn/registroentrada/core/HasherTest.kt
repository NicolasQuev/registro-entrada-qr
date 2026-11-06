package pe.edu.upn.registroentrada.core

import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class HasherTest {

    @Test
    fun `el pin correcto se verifica y el incorrecto no`() {
        val sal = Hasher.generarSal()
        val hash = Hasher.pinHash("1234", sal)
        assertTrue(Hasher.verificarPin("1234", sal, hash))
        assertFalse(Hasher.verificarPin("0000", sal, hash))
    }

    @Test
    fun `la misma clave con distinta sal produce distinto hash`() {
        val hash1 = Hasher.pinHash("1234", Hasher.generarSal())
        val hash2 = Hasher.pinHash("1234", Hasher.generarSal())
        assertNotEquals(hash1, hash2)
    }

    @Test
    fun `sha256 es estable`() {
        assertTrue(Hasher.pareceHash(Hasher.sha256("hola")))
        assertFalse(Hasher.pareceHash("no-es-hex"))
    }
}
