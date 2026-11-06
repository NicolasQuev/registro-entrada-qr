package pe.edu.upn.registroentrada.domain.usecase

import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import pe.edu.upn.registroentrada.core.Hasher
import pe.edu.upn.registroentrada.core.Resultado
import pe.edu.upn.registroentrada.domain.model.Operador
import pe.edu.upn.registroentrada.domain.model.RolOperador
import pe.edu.upn.registroentrada.testutil.AjustesRepositoryFalso
import pe.edu.upn.registroentrada.testutil.OperadorRepositoryFalso
import pe.edu.upn.registroentrada.testutil.RelojFijo

class IniciarSesionUseCaseTest {

    private val operadores = OperadorRepositoryFalso()
    private val ajustes = AjustesRepositoryFalso()
    private val reloj = RelojFijo()
    private val caso = IniciarSesionUseCase(operadores, ajustes, reloj)

    @Before
    fun preparar() {
        val sal = Hasher.generarSal()
        operadores.operadores.value = listOf(
            Operador(
                id = 1,
                nombre = "Administrador",
                usuario = "admin",
                pinHash = Hasher.pinHash("1234", sal),
                pinSal = sal,
                rol = RolOperador.ADMINISTRADOR,
                activo = true,
                creadoEn = reloj.ahora()
            )
        )
    }

    @Test
    fun `con el pin correcto inicia sesion y guarda al operador activo`() = runTest {
        val resultado = caso("admin", "1234")

        assertTrue(resultado is Resultado.Exito)
        assertEquals(1L, ajustes.flujo.value.operadorActivoId)
        assertEquals("Administrador", ajustes.flujo.value.operadorActivoNombre)
    }

    @Test
    fun `con el pin incorrecto no inicia sesion e informa el intento`() = runTest {
        val resultado = caso("admin", "0000")

        assertEquals("PIN incorrecto, intento 1 de 3", (resultado as Resultado.Error).mensaje)
        assertEquals(-1L, ajustes.flujo.value.operadorActivoId)
    }

    @Test
    fun `tras tres intentos fallidos bloquea el acceso`() = runTest {
        caso("admin", "0000")
        caso("admin", "0000")
        val tercero = caso("admin", "0000")

        assertEquals(
            "Demasiados intentos fallidos. Acceso bloqueado 60 segundos",
            (tercero as Resultado.Error).mensaje
        )
        assertNotEquals(0L, ajustes.flujo.value.bloqueoHasta)

        // Con el bloqueo activo, ni el PIN correcto entra.
        val bloqueado = caso("admin", "1234")
        assertTrue(bloqueado is Resultado.Error)
    }

    @Test
    fun `un operador desactivado no puede iniciar sesion`() = runTest {
        operadores.operadores.value = operadores.operadores.value.map { it.copy(activo = false) }

        val resultado = caso("admin", "1234")

        assertEquals("El operador está desactivado", (resultado as Resultado.Error).mensaje)
    }
}
