package pe.edu.upn.registroentrada.ui.screens.escaner

import java.time.Instant
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import pe.edu.upn.registroentrada.core.FirmadorQrHmac
import pe.edu.upn.registroentrada.domain.model.Aforo
import pe.edu.upn.registroentrada.domain.model.Entrada
import pe.edu.upn.registroentrada.domain.model.EstadoEvento
import pe.edu.upn.registroentrada.domain.model.Evento
import pe.edu.upn.registroentrada.domain.model.ResultadoAcceso
import pe.edu.upn.registroentrada.domain.usecase.RegistrarAccesoUseCase
import pe.edu.upn.registroentrada.testutil.AccesoRepositoryFalso
import pe.edu.upn.registroentrada.testutil.AjustesRepositoryFalso
import pe.edu.upn.registroentrada.testutil.EntradaRepositoryFalso
import pe.edu.upn.registroentrada.testutil.EventoRepositoryFalso
import pe.edu.upn.registroentrada.testutil.MainDispatcherRule
import pe.edu.upn.registroentrada.testutil.RelojFijo

class EscanerViewModelTest {

    @get:Rule
    val mainRule = MainDispatcherRule()

    private val entradas = EntradaRepositoryFalso()
    private val accesos = AccesoRepositoryFalso()
    private val eventos = EventoRepositoryFalso()
    private val ajustes = AjustesRepositoryFalso()
    private val reloj = RelojFijo(Instant.parse("2026-11-12T12:00:00Z"))
    private val firmador = FirmadorQrHmac("clave-de-prueba".toByteArray())
    private val registrarAcceso = RegistrarAccesoUseCase(entradas, accesos, eventos, firmador, reloj)

    private lateinit var codigoQr: String

    @Before
    fun preparar() = runTest {
        eventos.eventos.value = listOf(
            Evento(
                id = 1,
                nombre = "Concierto",
                lugar = "Lima",
                fechaInicio = reloj.ahora(),
                fechaFin = reloj.ahora().plusSeconds(3600),
                aforoMaximo = 100,
                estado = EstadoEvento.EN_CURSO,
                creadoEn = reloj.ahora()
            )
        )
        accesos.aforo = Aforo(ocupados = 0, maximo = 100)
        accesos.alMarcarUsada = { id, momento -> entradas.marcarUsada(id, momento) }
        ajustes.guardarOperadorActivo(id = 3, nombre = "admin")
        ajustes.guardarEventoActivo(eventoId = 1, puerta = "P1")
        entradas.emitir(
            Entrada(
                codigo = "uuid-1",
                firma = firmador.firmar("uuid-1"),
                eventoId = 1,
                tipoEntradaId = 1,
                asistenteId = 1,
                emitidaEn = reloj.ahora()
            )
        )
        codigoQr = firmador.empaquetar("uuid-1")
    }

    @Test
    fun `procesarCodigoEscaneado ignora lecturas duplicadas consecutivas`() = runTest {
        val vm = EscanerViewModel(registrarAcceso, ajustes, eventos, reloj)

        vm.procesarCodigoEscaneado(codigoQr)
        vm.procesarCodigoEscaneado(codigoQr)

        assertEquals(1, accesos.registros.value.size)
        assertEquals(ResultadoAcceso.PERMITIDO, vm.estado.value.validacion?.registro?.resultado)
    }

    @Test
    fun `procesarCodigoEscaneado aplica cooldown de 2 segundos tras limpiar y luego permite reescanear`() = runTest {
        val vm = EscanerViewModel(registrarAcceso, ajustes, eventos, reloj)

        vm.procesarCodigoEscaneado(codigoQr)
        vm.limpiar()

        reloj.instante = reloj.instante.plusMillis(500)
        vm.procesarCodigoEscaneado(codigoQr)
        assertEquals(1, accesos.registros.value.size)

        reloj.instante = reloj.instante.plusSeconds(3)
        vm.procesarCodigoEscaneado(codigoQr)
        assertEquals(2, accesos.registros.value.size)
        assertNotNull(vm.estado.value.validacion)
    }
}
