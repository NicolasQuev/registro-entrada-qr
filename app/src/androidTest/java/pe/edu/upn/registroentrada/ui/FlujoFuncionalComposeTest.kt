package pe.edu.upn.registroentrada.ui

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performTextInput
import androidx.compose.ui.test.performTextReplacement
import java.time.Instant
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import pe.edu.upn.registroentrada.core.FirmadorQrHmac
import pe.edu.upn.registroentrada.core.Reloj
import pe.edu.upn.registroentrada.core.location.Coordenadas
import pe.edu.upn.registroentrada.core.location.ProveedorUbicacion
import pe.edu.upn.registroentrada.domain.model.Aforo
import pe.edu.upn.registroentrada.domain.model.AjustesUsuario
import pe.edu.upn.registroentrada.domain.model.Entrada
import pe.edu.upn.registroentrada.domain.model.EntradaDetalle
import pe.edu.upn.registroentrada.domain.model.EstadoEntrada
import pe.edu.upn.registroentrada.domain.model.EstadoEvento
import pe.edu.upn.registroentrada.domain.model.Evento
import pe.edu.upn.registroentrada.domain.model.FiltroRegistros
import pe.edu.upn.registroentrada.domain.model.RegistroAcceso
import pe.edu.upn.registroentrada.domain.model.RegistroAccesoDetalle
import pe.edu.upn.registroentrada.domain.model.ResultadoAcceso
import pe.edu.upn.registroentrada.domain.model.TemaApp
import pe.edu.upn.registroentrada.domain.model.TipoEntrada
import pe.edu.upn.registroentrada.domain.repository.AccesoRepository
import pe.edu.upn.registroentrada.domain.repository.AjustesRepository
import pe.edu.upn.registroentrada.domain.repository.EntradaRepository
import pe.edu.upn.registroentrada.domain.repository.EventoRepository
import pe.edu.upn.registroentrada.domain.usecase.CrearEventoUseCase
import pe.edu.upn.registroentrada.domain.usecase.RegistrarAccesoUseCase
import pe.edu.upn.registroentrada.ui.screens.escaner.EscanerScreen
import pe.edu.upn.registroentrada.ui.screens.escaner.EscanerViewModel
import pe.edu.upn.registroentrada.ui.screens.eventos.EventoFormScreen
import pe.edu.upn.registroentrada.ui.screens.eventos.EventoFormViewModel
import pe.edu.upn.registroentrada.ui.theme.RegistroEntradaTheme

/**
 * Pruebas funcionales instrumentadas de interfaz (Compose UI) para PC12:
 * verifican el formulario con geolocalización y el flujo de validación en puerta.
 */
class FlujoFuncionalComposeTest {

    @get:Rule
    val composeRule = createAndroidComposeRule<ComponentActivity>()

    @Test
    fun eventoForm_aplicaCoordenadasPredefinidas_yRechazaLatitudFueraDeRango() {
        val repo = FakeEventoRepository()
        val reloj = Reloj { Instant.parse("2026-11-12T10:00:00Z") }
        val vm = EventoFormViewModel(
            crearEvento = CrearEventoUseCase(repo, reloj),
            proveedorUbicacion = ProveedorUbicacion { Result.success(Coordenadas(-12.0464, -77.0428)) }
        )
        var creadoId: Long? = null

        composeRule.setContent {
            RegistroEntradaTheme(tema = TemaApp.CLARO) {
                EventoFormScreen(
                    onVolver = {},
                    onCreado = { creadoId = it },
                    viewModel = vm
                )
            }
        }

        composeRule.onNodeWithText("Usar mi ubicación GPS").performScrollTo().assertIsDisplayed()
        composeRule.onNodeWithText("Estadio Nacional").performScrollTo().performClick()
        composeRule.onNodeWithText("-12.067300").assertIsDisplayed()
        composeRule.onNodeWithText("-77.033700").assertIsDisplayed()

        composeRule.onNodeWithText("Nombre").performScrollTo().performTextInput("Festival Rock")
        composeRule.onNodeWithText("Lugar / Dirección").performScrollTo().performTextInput("Lima")
        composeRule.onNodeWithText("Aforo máximo").performScrollTo().performTextInput("150")
        composeRule.onNodeWithText("-12.067300").performScrollTo().performTextReplacement("95.0")

        composeRule.onNodeWithText("Guardar evento").performScrollTo().performClick()

        composeRule.onNodeWithText("La latitud debe estar entre -90 y 90")
            .performScrollTo()
            .assertIsDisplayed()
        assertNull(creadoId)
        assertTrue(repo.lista.value.isEmpty())
    }

    @Test
    fun eventoForm_guardaEventoValidoConCoordenadas_eInvocaOnCreado() {
        val repo = FakeEventoRepository()
        val reloj = Reloj { Instant.parse("2026-11-12T10:00:00Z") }
        val vm = EventoFormViewModel(
            crearEvento = CrearEventoUseCase(repo, reloj),
            proveedorUbicacion = ProveedorUbicacion { Result.success(Coordenadas(-12.0464, -77.0428)) }
        )
        var creadoId = -1L

        composeRule.setContent {
            RegistroEntradaTheme(tema = TemaApp.OSCURO) {
                EventoFormScreen(
                    onVolver = {},
                    onCreado = { creadoId = it },
                    viewModel = vm
                )
            }
        }

        composeRule.onNodeWithText("Nombre").performScrollTo().performTextInput("Congreso Tech UPN")
        composeRule.onNodeWithText("Lugar / Dirección").performScrollTo().performTextInput("San Borja")
        composeRule.onNodeWithText("Convenciones Lima").performScrollTo().performClick()
        composeRule.onNodeWithText("Aforo máximo").performScrollTo().performTextInput("500")
        composeRule.onNodeWithText("Guardar evento").performScrollTo().performClick()

        composeRule.waitUntil(timeoutMillis = 5_000) { creadoId > 0L }
        assertEquals(1, repo.lista.value.size)
        val guardado = repo.lista.value.first()
        assertEquals("Congreso Tech UPN", guardado.nombre)
        assertEquals(-12.0864, guardado.latitud ?: 0.0, 0.0001)
        assertEquals(-77.0016, guardado.longitud ?: 0.0, 0.0001)
    }

    @Test
    fun escanerScreen_permitePrimerIngreso_yRechazaUsoDuplicadoEnPuerta() {
        val reloj = object : Reloj {
            var actual: Instant = Instant.parse("2026-11-12T12:00:00Z")
            override fun ahora(): Instant = actual
        }
        val firmador = FirmadorQrHmac("clave-test-ui-pc12".toByteArray())
        val eventos = FakeEventoRepository().apply {
            lista.value = listOf(
                Evento(
                    id = 1L,
                    nombre = "Festival Trujillo",
                    lugar = "Explanada",
                    latitud = -8.1116,
                    longitud = -79.0287,
                    fechaInicio = reloj.ahora().minusSeconds(3600),
                    fechaFin = reloj.ahora().plusSeconds(3600),
                    aforoMaximo = 200,
                    estado = EstadoEvento.EN_CURSO,
                    creadoEn = reloj.ahora()
                )
            )
        }
        val entradas = FakeEntradaRepository()
        val codigo = "uuid-pc12-001"
        val firma = firmador.firmar(codigo)
        val qrTexto = "RE|1|$codigo|$firma"
        entradas.entradas.value = listOf(
            EntradaDetalle(
                entrada = Entrada(
                    id = 10L,
                    codigo = codigo,
                    firma = firma,
                    eventoId = 1L,
                    tipoEntradaId = 1L,
                    asistenteId = 7L,
                    estado = EstadoEntrada.VALIDA,
                    emitidaEn = reloj.ahora()
                ),
                nombreAsistente = "Ana Pérez",
                documentoAsistente = "71234567",
                nombreTipoEntrada = "VIP",
                colorTipoEntrada = "#2F80ED",
                nombreEvento = "Festival Trujillo",
                lugarEvento = "Explanada"
            )
        )
        val accesos = FakeAccesoRepository(maximo = 200) { entradaId, usadaEn ->
            entradas.marcarUsadaSync(entradaId, usadaEn)
        }
        val ajustes = FakeAjustesRepository(
            AjustesUsuario(
                operadorActivoId = 3L,
                operadorActivoNombre = "Operador P1",
                eventoActivoId = 1L,
                puertaActiva = "P1"
            )
        )
        val registrarAcceso = RegistrarAccesoUseCase(entradas, accesos, eventos, firmador, reloj)
        val vm = EscanerViewModel(registrarAcceso, ajustes, eventos, reloj)

        composeRule.setContent {
            RegistroEntradaTheme(tema = TemaApp.CLARO) {
                EscanerScreen(viewModel = vm)
            }
        }

        composeRule.onNodeWithText("PUERTA P1").assertIsDisplayed()
        composeRule.onNodeWithText("Festival Trujillo").assertIsDisplayed()

        composeRule.onNodeWithText("Código de la entrada").performTextInput(qrTexto)
        composeRule.onNodeWithText("Validar").performClick()

        composeRule.waitUntil(timeoutMillis = 5_000) {
            vm.estado.value.validacion != null
        }
        composeRule.onNodeWithText("PERMITIDO").assertIsDisplayed()
        composeRule.onNodeWithText("Puede ingresar").assertIsDisplayed()
        composeRule.onNodeWithText("Ana Pérez").assertIsDisplayed()

        composeRule.onNodeWithText("Siguiente escaneo").performClick()
        composeRule.onNodeWithText("Código de la entrada").performTextInput(qrTexto)
        composeRule.onNodeWithText("Validar").performClick()

        composeRule.waitUntil(timeoutMillis = 5_000) {
            vm.estado.value.validacion != null
        }
        composeRule.onNodeWithText("RECHAZADO").assertIsDisplayed()
        composeRule.onNodeWithText("La entrada ya fue usada").assertIsDisplayed()
    }
}

private class FakeEventoRepository : EventoRepository {
    val lista = MutableStateFlow<List<Evento>>(emptyList())
    private var siguienteId = 1L

    override fun observarEventos(): Flow<List<Evento>> = lista
    override fun observarEvento(id: Long): Flow<Evento?> = lista.map { it.firstOrNull { e -> e.id == id } }
    override fun buscarEventos(texto: String): Flow<List<Evento>> = lista
    override suspend fun guardar(evento: Evento): Long {
        val nuevo = if (evento.id == 0L) evento.copy(id = siguienteId++) else evento
        lista.value = lista.value.filterNot { it.id == nuevo.id } + nuevo
        return nuevo.id
    }
    override suspend fun actualizar(evento: Evento) { guardar(evento) }
    override suspend fun cambiarEstado(id: Long, estado: EstadoEvento) {}
    override suspend fun eliminarEvento(id: Long) {}
    override suspend fun sincronizarConApiRest(): Result<Int> = Result.success(0)
    override suspend fun sincronizarEventoEnNube(eventoId: Long): Result<String> = Result.success("fake")
    override suspend fun eliminarEventoEnNube(remoteId: String): Result<Unit> = Result.success(Unit)
    override fun observarTiposDeEntrada(eventoId: Long): Flow<List<TipoEntrada>> = MutableStateFlow(emptyList())
    override suspend fun obtenerTipoDeEntrada(tipoId: Long): TipoEntrada? = null
    override suspend fun guardarTipoDeEntrada(tipo: TipoEntrada): Long = 1L
    override suspend fun eliminarTipoDeEntrada(tipoId: Long) {}
    override suspend fun contarEntradasVigentesPorTipo(tipoId: Long): Int = 0
}

private class FakeEntradaRepository : EntradaRepository {
    val entradas = MutableStateFlow<List<EntradaDetalle>>(emptyList())

    override fun observarEntradasDeEvento(eventoId: Long): Flow<List<EntradaDetalle>> = entradas
    override fun observarEntrada(id: Long): Flow<EntradaDetalle?> = entradas.map { it.firstOrNull { d -> d.entrada.id == id } }
    override suspend fun buscarPorCodigo(codigo: String): EntradaDetalle? =
        entradas.value.firstOrNull { it.entrada.codigo == codigo }
    override suspend fun emitir(entrada: Entrada): Long = entrada.id
    override suspend fun marcarUsada(entradaId: Long, momento: Instant) { marcarUsadaSync(entradaId, momento) }
    fun marcarUsadaSync(entradaId: Long, momento: Instant) {
        entradas.value = entradas.value.map {
            if (it.entrada.id == entradaId) {
                it.copy(entrada = it.entrada.copy(estado = EstadoEntrada.USADA, usadaEn = momento))
            } else {
                it
            }
        }
    }
    override suspend fun anular(entradaId: Long) {}
    override suspend fun existeEntradaValida(asistenteId: Long, eventoId: Long): Boolean = false
    override suspend fun contarPorEventoYEstado(eventoId: Long, estado: EstadoEntrada): Int = 0
}

private class FakeAccesoRepository(
    private val maximo: Int,
    private val alMarcarUsada: (Long, Instant) -> Unit
) : AccesoRepository {
    private val registros = MutableStateFlow<List<RegistroAcceso>>(emptyList())

    override fun observarRegistros(eventoId: Long, filtro: FiltroRegistros): Flow<List<RegistroAccesoDetalle>> =
        MutableStateFlow(emptyList())
    override fun observarTodosRegistros(eventoId: Long): Flow<List<RegistroAccesoDetalle>> =
        MutableStateFlow(emptyList())
    override suspend fun contarPorResultado(eventoId: Long, resultado: ResultadoAcceso): Int =
        registros.value.count { it.eventoId == eventoId && it.resultado == resultado }
    override fun observarAforo(eventoId: Long): Flow<Aforo> = registros.map { lista ->
        Aforo(ocupados = lista.count { it.eventoId == eventoId && it.resultado == ResultadoAcceso.PERMITIDO }, maximo = maximo)
    }
    override suspend fun registrarAcceso(registro: RegistroAcceso, marcarEntradaUsada: Boolean): Long {
        if (marcarEntradaUsada && registro.entradaId != null) {
            alMarcarUsada(registro.entradaId, registro.fechaHora)
        }
        val nuevo = registro.copy(id = registros.value.size + 1L)
        registros.value += nuevo
        return nuevo.id
    }
}

private class FakeAjustesRepository(inicial: AjustesUsuario) : AjustesRepository {
    private val estado = MutableStateFlow(inicial)
    override val ajustes: Flow<AjustesUsuario> = estado
    override suspend fun guardarOperadorActivo(id: Long, nombre: String) {}
    override suspend fun limpiarOperadorActivo() {}
    override suspend fun guardarEventoActivo(eventoId: Long, puerta: String) {}
    override suspend fun guardarTema(tema: TemaApp) {}
    override suspend fun guardarSonido(activo: Boolean) {}
    override suspend fun guardarVibracion(activa: Boolean) {}
    override suspend fun registrarIntentoFallido(): Int = 0
    override suspend fun reiniciarIntentos() {}
    override suspend fun bloquearHasta(momento: Instant) {}
    override suspend fun marcarRespaldo(momento: Instant) {}
}
