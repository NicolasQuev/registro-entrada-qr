package pe.edu.upn.registroentrada.domain.usecase

import java.time.Instant
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import pe.edu.upn.registroentrada.core.Resultado
import pe.edu.upn.registroentrada.domain.model.EstadoEvento
import pe.edu.upn.registroentrada.domain.model.Evento
import pe.edu.upn.registroentrada.domain.model.TipoEntrada
import pe.edu.upn.registroentrada.testutil.AjustesRepositoryFalso
import pe.edu.upn.registroentrada.testutil.EventoRepositoryFalso

class EliminarEventoUseCaseTest {

    private val eventos = EventoRepositoryFalso()
    private val ajustes = AjustesRepositoryFalso()
    private val caso = EliminarEventoUseCase(eventos, ajustes)

    @Before
    fun preparar() = kotlinx.coroutines.test.runTest {
        eventos.eventos.value = listOf(
            Evento(
                id = 1,
                nombre = "Concierto",
                lugar = "Arena",
                fechaInicio = Instant.parse("2026-02-01T20:00:00Z"),
                fechaFin = Instant.parse("2026-02-01T23:00:00Z"),
                aforoMaximo = 100,
                estado = EstadoEvento.PROGRAMADO,
                creadoEn = Instant.parse("2026-01-01T00:00:00Z")
            )
        )
        eventos.tipos.value = listOf(
            TipoEntrada(id = 1, eventoId = 1, nombre = "General", precio = 10.0, cupo = 50, color = "#2E74B5")
        )
        ajustes.guardarEventoActivo(1, "P1")
    }

    @Test
    fun `elimina el evento y sus tipos`() = runTest {
        val resultado = caso(1)

        assertTrue(resultado is Resultado.Exito)
        assertTrue(eventos.eventos.value.isEmpty())
        assertTrue(eventos.tipos.value.isEmpty())
    }

    @Test
    fun `si era el evento activo lo deselecciona`() = runTest {
        caso(1)

        assertEquals(-1L, ajustes.flujo.value.eventoActivoId)
    }

    @Test
    fun `no falla si el evento no existe`() = runTest {
        val resultado = caso(99)

        assertEquals("El evento ya no existe", (resultado as Resultado.Error).mensaje)
    }
}

class EliminarTipoEntradaUseCaseTest {

    private val eventos = EventoRepositoryFalso()
    private val caso = EliminarTipoEntradaUseCase(eventos)

    @Before
    fun preparar() {
        eventos.tipos.value = listOf(
            TipoEntrada(id = 1, eventoId = 1, nombre = "General", precio = 10.0, cupo = 50, color = "#2E74B5")
        )
    }

    @Test
    fun `elimina un tipo sin entradas emitidas`() = runTest {
        eventos.vigentesPorTipo = { 0 }

        val resultado = caso(1)

        assertTrue(resultado is Resultado.Exito)
        assertTrue(eventos.tipos.value.isEmpty())
    }

    @Test
    fun `no elimina un tipo con entradas emitidas`() = runTest {
        eventos.vigentesPorTipo = { 3 }

        val resultado = caso(1)

        assertEquals(
            "Este tipo tiene entradas emitidas; anúlalas primero",
            (resultado as Resultado.Error).mensaje
        )
        assertEquals(1, eventos.tipos.value.size)
    }
}
