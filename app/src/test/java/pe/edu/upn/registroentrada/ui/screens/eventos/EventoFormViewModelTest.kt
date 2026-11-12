package pe.edu.upn.registroentrada.ui.screens.eventos

import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import pe.edu.upn.registroentrada.core.location.Coordenadas
import pe.edu.upn.registroentrada.core.location.ProveedorUbicacion
import pe.edu.upn.registroentrada.domain.usecase.CrearEventoUseCase
import pe.edu.upn.registroentrada.testutil.EventoRepositoryFalso
import pe.edu.upn.registroentrada.testutil.MainDispatcherRule
import pe.edu.upn.registroentrada.testutil.RelojFijo

class EventoFormViewModelTest {

    @get:Rule
    val mainRule = MainDispatcherRule()

    private val eventos = EventoRepositoryFalso()
    private val crearEvento = CrearEventoUseCase(eventos, RelojFijo())

    @Test
    fun `guardar rechaza latitud fuera de rango en formulario`() = runTest {
        val vm = EventoFormViewModel(crearEvento, ProveedorUbicacion { Result.failure(IllegalStateException()) })
        vm.cambiarNombre("Concierto")
        vm.cambiarLugar("Lima")
        vm.cambiarAforo("200")
        vm.cambiarLatitud("95.0")
        vm.cambiarLongitud("-77.0")
        var creado = false

        vm.guardar { creado = true }

        assertFalse(creado)
        assertNotNull(vm.estado.value.errorCoordenadas)
        assertTrue(eventos.eventos.value.isEmpty())
    }

    @Test
    fun `obtenerUbicacionGps actualiza latitud y longitud cuando tiene exito`() = runTest {
        val proveedor = ProveedorUbicacion { Result.success(Coordenadas(-8.1116, -79.0287)) }
        val vm = EventoFormViewModel(crearEvento, proveedor)

        vm.obtenerUbicacionGps()

        assertFalse(vm.estado.value.obteniendoUbicacion)
        assertEquals("-8.111600", vm.estado.value.latitud)
        assertEquals("-79.028700", vm.estado.value.longitud)
        assertNull(vm.estado.value.errorCoordenadas)
    }

    @Test
    fun `obtenerUbicacionGps informa error cuando falla el GPS`() = runTest {
        val proveedor = ProveedorUbicacion { Result.failure(IllegalStateException("GPS desactivado")) }
        val vm = EventoFormViewModel(crearEvento, proveedor)

        vm.obtenerUbicacionGps()

        assertFalse(vm.estado.value.obteniendoUbicacion)
        assertEquals("GPS desactivado", vm.estado.value.errorCoordenadas)
    }
}
