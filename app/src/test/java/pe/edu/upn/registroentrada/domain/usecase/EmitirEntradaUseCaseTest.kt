package pe.edu.upn.registroentrada.domain.usecase

import java.time.Instant
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import pe.edu.upn.registroentrada.core.FirmadorQrHmac
import pe.edu.upn.registroentrada.core.Resultado
import pe.edu.upn.registroentrada.domain.model.EstadoEvento
import pe.edu.upn.registroentrada.domain.model.Evento
import pe.edu.upn.registroentrada.domain.model.TipoEntrada
import pe.edu.upn.registroentrada.testutil.EntradaRepositoryFalso
import pe.edu.upn.registroentrada.testutil.EventoRepositoryFalso
import pe.edu.upn.registroentrada.testutil.GeneradorIdFijo
import pe.edu.upn.registroentrada.testutil.RelojFijo

class EmitirEntradaUseCaseTest {

    private val eventos = EventoRepositoryFalso()
    private val entradas = EntradaRepositoryFalso()
    private val firmador = FirmadorQrHmac("clave-de-prueba".toByteArray())
    private val reloj = RelojFijo()

    private val caso = EmitirEntradaUseCase(eventos, entradas, GeneradorIdFijo(), firmador, reloj)

    private val tipoGeneral = TipoEntrada(
        id = 1,
        eventoId = 1,
        nombre = "General",
        precio = 20.0,
        cupo = 5,
        color = "#2E74B5"
    )

    @Before
    fun preparar() {
        eventos.eventos.value = listOf(
            Evento(
                id = 1,
                nombre = "Concierto",
                lugar = "Arena",
                fechaInicio = Instant.parse("2026-02-01T20:00:00Z"),
                fechaFin = Instant.parse("2026-02-01T23:00:00Z"),
                aforoMaximo = 100,
                estado = EstadoEvento.PROGRAMADO,
                creadoEn = reloj.ahora()
            )
        )
        eventos.tipos.value = listOf(tipoGeneral)
    }

    @Test
    fun `emite varias entradas con codigos unicos y firmados`() = runTest {
        val resultado = caso(eventoId = 1, tipoEntradaId = 1, cantidad = 3)

        assertTrue(resultado is Resultado.Exito)
        assertEquals(3, (resultado as Resultado.Exito).valor)
        assertEquals(3, entradas.entradas.value.size)

        val codigos = entradas.entradas.value.map { it.codigo }
        assertEquals("Los códigos deben ser únicos", 3, codigos.toSet().size)
        assertTrue(entradas.entradas.value.all { firmador.verificar(it.codigo, it.firma) })
        assertTrue(entradas.entradas.value.all { it.asistenteId == null })
    }

    @Test
    fun `no emite si la cantidad es menor a uno`() = runTest {
        val resultado = caso(eventoId = 1, tipoEntradaId = 1, cantidad = 0)

        assertEquals(
            "Indica cuántas entradas vas a emitir",
            (resultado as Resultado.Error).mensaje
        )
    }

    @Test
    fun `no supera el cupo del tipo de entrada`() = runTest {
        eventos.vigentesPorTipo = { 4 }

        val resultado = caso(eventoId = 1, tipoEntradaId = 1, cantidad = 3)

        assertEquals(
            "Queda 1 entrada disponible de este tipo",
            (resultado as Resultado.Error).mensaje
        )
    }

    @Test
    fun `rechaza un tipo de entrada de otro evento`() = runTest {
        eventos.tipos.value = listOf(tipoGeneral.copy(eventoId = 99))

        val resultado = caso(eventoId = 1, tipoEntradaId = 1, cantidad = 1)

        assertEquals(
            "El tipo de entrada no pertenece a este evento",
            (resultado as Resultado.Error).mensaje
        )
    }
}
