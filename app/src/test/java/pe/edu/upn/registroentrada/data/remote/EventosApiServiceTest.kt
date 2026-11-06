package pe.edu.upn.registroentrada.data.remote

import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import pe.edu.upn.registroentrada.data.mapper.aDominio
import pe.edu.upn.registroentrada.data.mapper.aRequestDto
import pe.edu.upn.registroentrada.data.remote.api.EventosApiService
import pe.edu.upn.registroentrada.di.NetworkModule
import pe.edu.upn.registroentrada.domain.model.EstadoEvento
import pe.edu.upn.registroentrada.domain.model.Evento
import java.time.Instant

/**
 * Pruebas unitarias de integración del contrato JSON y consumo REST de la API con Retrofit.
 */
class EventosApiServiceTest {

    private val okHttpClient = NetworkModule.provideOkHttpClient(NetworkModule.provideHttpLoggingInterceptor())
    private val retrofit = NetworkModule.provideRetrofit(okHttpClient)
    private val apiService: EventosApiService = NetworkModule.provideEventosApiService(retrofit)

    private val projectId = NetworkModule.FIREBASE_PROJECT_ID
    private val apiKey = NetworkModule.FIREBASE_API_KEY

    @Test
    fun contratoJson_mapeoEventoADto_esCorrecto() {
        val evento = Evento(
            id = 10,
            remoteId = "TEST-01",
            nombre = "Evento Test",
            descripcion = "Desc Test",
            lugar = "Lima",
            latitud = -12.0464,
            longitud = -77.0428,
            fechaInicio = Instant.now(),
            fechaFin = Instant.now().plusSeconds(3600),
            aforoMaximo = 500,
            estado = EstadoEvento.PROGRAMADO,
            creadoEn = Instant.now()
        )

        val dto = evento.aRequestDto()
        assertEquals("Evento Test", dto.fields.nombre.stringValue)
        assertEquals("Lima", dto.fields.lugar.stringValue)
        assertEquals(-12.0464, dto.fields.latitud?.doubleValue ?: 0.0, 0.0001)
        assertEquals(-77.0428, dto.fields.longitud?.doubleValue ?: 0.0, 0.0001)
        assertEquals("500", dto.fields.aforoMaximo.integerValue)
    }

    @Test
    fun apiRest_listarEventos_retornaExitoso() = runBlocking {
        val respuesta = apiService.listarEventos(projectId, apiKey)
        assertTrue("La llamada HTTP debe ser exitosa", respuesta.isSuccessful)
        assertNotNull(respuesta.body())
        val docs = respuesta.body()?.documents ?: emptyList()
        assertTrue("Debe contener al menos los eventos sembrados en Firestore", docs.isNotEmpty())
    }
}
