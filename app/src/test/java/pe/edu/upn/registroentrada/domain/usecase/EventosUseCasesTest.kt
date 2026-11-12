package pe.edu.upn.registroentrada.domain.usecase

import java.time.Instant
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import pe.edu.upn.registroentrada.core.Resultado
import pe.edu.upn.registroentrada.testutil.EventoRepositoryFalso
import pe.edu.upn.registroentrada.testutil.RelojFijo

class EventosUseCasesTest {

    private val eventos = EventoRepositoryFalso()
    private val reloj = RelojFijo()
    private val crearEvento = CrearEventoUseCase(eventos, reloj)
    private val sincronizarEventos = SincronizarEventosRestUseCase(eventos)

    private val inicio = Instant.parse("2026-11-20T18:00:00Z")
    private val fin = Instant.parse("2026-11-20T22:00:00Z")

    @Test
    fun `crear evento rechaza latitud fuera de rango`() = runTest {
        val resultado = crearEvento(
            nombre = "Festival",
            descripcion = null,
            lugar = "Lima",
            fechaInicio = inicio,
            fechaFin = fin,
            aforoMaximo = 100,
            latitud = 95.0,
            longitud = -77.0
        )

        assertTrue(resultado is Resultado.Error)
        assertTrue(eventos.eventos.value.isEmpty())
    }

    @Test
    fun `crear evento rechaza longitud fuera de rango`() = runTest {
        val resultado = crearEvento(
            nombre = "Festival",
            descripcion = null,
            lugar = "Lima",
            fechaInicio = inicio,
            fechaFin = fin,
            aforoMaximo = 100,
            latitud = -12.0,
            longitud = -185.0
        )

        assertTrue(resultado is Resultado.Error)
        assertTrue(eventos.eventos.value.isEmpty())
    }

    @Test
    fun `crear evento rechaza coordenada incompleta cuando falta longitud`() = runTest {
        val resultado = crearEvento(
            nombre = "Festival",
            descripcion = null,
            lugar = "Lima",
            fechaInicio = inicio,
            fechaFin = fin,
            aforoMaximo = 100,
            latitud = -12.0464,
            longitud = null
        )

        assertTrue(resultado is Resultado.Error)
        assertTrue(eventos.eventos.value.isEmpty())
    }

    @Test
    fun `crear evento guarda coordenadas validas`() = runTest {
        val resultado = crearEvento(
            nombre = "Festival",
            descripcion = "Noche de rock",
            lugar = "Estadio Nacional",
            fechaInicio = inicio,
            fechaFin = fin,
            aforoMaximo = 500,
            latitud = -12.0673,
            longitud = -77.0337
        )

        assertTrue(resultado is Resultado.Exito)
        val guardado = eventos.eventos.value.single()
        assertEquals(-12.0673, guardado.latitud ?: 0.0, 0.0001)
        assertEquals(-77.0337, guardado.longitud ?: 0.0, 0.0001)
    }

    @Test
    fun `sincronizar eventos devuelve cantidad cuando api responde ok`() = runTest {
        eventos.resultadoSync = Result.success(4)

        val resultado = sincronizarEventos()

        assertEquals(Resultado.Exito(4), resultado)
    }

    @Test
    fun `sincronizar eventos devuelve error cuando falla la red`() = runTest {
        eventos.resultadoSync = Result.failure(IllegalStateException("Error HTTP 503"))

        val resultado = sincronizarEventos()

        assertEquals("Error HTTP 503", (resultado as Resultado.Error).mensaje)
    }
}
