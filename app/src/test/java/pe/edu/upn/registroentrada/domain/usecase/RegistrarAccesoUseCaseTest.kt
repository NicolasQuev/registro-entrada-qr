package pe.edu.upn.registroentrada.domain.usecase

import java.time.Instant
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import pe.edu.upn.registroentrada.core.FirmadorQrHmac
import pe.edu.upn.registroentrada.domain.model.Aforo
import pe.edu.upn.registroentrada.domain.model.Entrada
import pe.edu.upn.registroentrada.domain.model.EstadoEntrada
import pe.edu.upn.registroentrada.domain.model.EstadoEvento
import pe.edu.upn.registroentrada.domain.model.Evento
import pe.edu.upn.registroentrada.domain.model.ResultadoAcceso
import pe.edu.upn.registroentrada.testutil.AccesoRepositoryFalso
import pe.edu.upn.registroentrada.testutil.EntradaRepositoryFalso
import pe.edu.upn.registroentrada.testutil.EventoRepositoryFalso
import pe.edu.upn.registroentrada.testutil.RelojFijo

class RegistrarAccesoUseCaseTest {

    private val entradas = EntradaRepositoryFalso()
    private val accesos = AccesoRepositoryFalso()
    private val eventos = EventoRepositoryFalso()
    private val firmador = FirmadorQrHmac("clave-de-prueba".toByteArray())
    private val reloj = RelojFijo()

    private val caso = RegistrarAccesoUseCase(entradas, accesos, eventos, firmador, reloj)

    private var entradaId: Long = 0

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
                creadoEn = reloj.ahora()
            )
        )
        accesos.aforo = Aforo(ocupados = 0, maximo = 100)
        accesos.alMarcarUsada = { id, momento -> entradas.marcarUsada(id, momento) }
        entradaId = entradas.emitir(
            Entrada(
                codigo = "uuid-1",
                firma = firmador.firmar("uuid-1"),
                eventoId = 1,
                tipoEntradaId = 1,
                asistenteId = 5,
                emitidaEn = reloj.ahora()
            )
        )
    }

    @Test
    fun `un QR valido permite el ingreso y marca la entrada como usada`() = runTest {
        val texto = firmador.empaquetar("uuid-1")

        val validacion = caso(texto, eventoActivoId = 1, puerta = "P1", operadorId = 3)

        assertTrue(validacion.permitido)
        assertEquals(ResultadoAcceso.PERMITIDO, validacion.registro.resultado)
        assertTrue(accesos.entradasMarcadasUsadas.contains(entradaId))
        assertEquals(EstadoEntrada.USADA, entradas.entradas.value.single().estado)
    }

    @Test
    fun `un QR con firma alterada se rechaza como firma invalida`() = runTest {
        val textoFalso = "RE|1|uuid-1|deadbeefdeadbeef"

        val validacion = caso(textoFalso, eventoActivoId = 1, puerta = "P1", operadorId = 3)

        assertFalse(validacion.permitido)
        assertEquals(ResultadoAcceso.FIRMA_INVALIDA, validacion.registro.resultado)
    }

    @Test
    fun `una entrada ya usada se rechaza sin marcarla otra vez`() = runTest {
        entradas.marcarUsada(entradaId, reloj.ahora())

        val validacion = caso(firmador.empaquetar("uuid-1"), 1, "P1", 3)

        assertFalse(validacion.permitido)
        assertEquals(ResultadoAcceso.YA_USADA, validacion.registro.resultado)
        assertTrue(accesos.entradasMarcadasUsadas.isEmpty())
    }

    @Test
    fun `una entrada de otro evento se rechaza`() = runTest {
        entradas.entradas.value = entradas.entradas.value.map { it.copy(eventoId = 99) }

        val validacion = caso(firmador.empaquetar("uuid-1"), 1, "P1", 3)

        assertEquals(ResultadoAcceso.OTRO_EVENTO, validacion.registro.resultado)
    }

    @Test
    fun `con el aforo completo se rechaza y la entrada sigue valida`() = runTest {
        accesos.aforo = Aforo(ocupados = 100, maximo = 100)

        val validacion = caso(firmador.empaquetar("uuid-1"), 1, "P1", 3)

        assertEquals(ResultadoAcceso.AFORO_LLENO, validacion.registro.resultado)
        assertEquals(EstadoEntrada.VALIDA, entradas.entradas.value.single().estado)
        assertTrue(accesos.entradasMarcadasUsadas.isEmpty())
    }

    @Test
    fun `el codigo escrito a mano se valida igual que el QR`() = runTest {
        val validacion = caso("uuid-1", eventoActivoId = 1, puerta = "P1", operadorId = 3)

        assertTrue(validacion.permitido)
    }

    @Test
    fun `todo intento queda registrado en el historial`() = runTest {
        caso("codigo-inexistente", eventoActivoId = 1, puerta = "P1", operadorId = 3)

        assertEquals(1, accesos.registros.value.size)
        assertEquals(ResultadoAcceso.NO_EXISTE, accesos.registros.value.single().resultado)
    }
}
