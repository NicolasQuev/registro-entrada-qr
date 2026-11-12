package pe.edu.upn.registroentrada.data.repository

import java.time.Instant
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.test.runTest
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import pe.edu.upn.registroentrada.data.local.dao.EntradaDao
import pe.edu.upn.registroentrada.data.local.dao.EventoDao
import pe.edu.upn.registroentrada.data.local.dao.TipoEntradaDao
import pe.edu.upn.registroentrada.data.local.entity.EntradaEntity
import pe.edu.upn.registroentrada.data.local.entity.EventoEntity
import pe.edu.upn.registroentrada.data.local.entity.TipoEntradaEntity
import pe.edu.upn.registroentrada.data.local.relation.EntradaDetalleRow
import pe.edu.upn.registroentrada.data.remote.api.EventosApiService
import pe.edu.upn.registroentrada.data.remote.dto.EventoDocumentDto
import pe.edu.upn.registroentrada.data.remote.dto.EventoFieldsDto
import pe.edu.upn.registroentrada.data.remote.dto.EventoRequestDto
import pe.edu.upn.registroentrada.data.remote.dto.FirestoreListResponse
import pe.edu.upn.registroentrada.data.remote.dto.FirestoreValueDouble
import pe.edu.upn.registroentrada.data.remote.dto.FirestoreValueInt
import pe.edu.upn.registroentrada.data.remote.dto.FirestoreValueString
import retrofit2.Response

class EventoRepositoryImplTest {

    private val eventoDao = EventoDaoEnMemoria()
    private val apiService = EventosApiServiceFalso()
    private val repo = EventoRepositoryImpl(
        eventoDao = eventoDao,
        tipoEntradaDao = TipoEntradaDaoVacio(),
        entradaDao = EntradaDaoVacio(),
        apiService = apiService,
        projectId = "test-project",
        apiKey = "test-key"
    )

    @Test
    fun `sincronizarConApiRest inserta nuevos y actualiza existentes sin duplicar`() = runTest {
        eventoDao.insertar(
            EventoEntity(
                remoteId = "EVT-1",
                nombre = "Nombre antiguo",
                descripcion = null,
                lugar = "Lima",
                latitud = -12.0,
                longitud = -77.0,
                fechaInicio = 1_700_000_000_000L,
                fechaFin = 1_700_003_600_000L,
                aforoMaximo = 100,
                estado = "PROGRAMADO",
                creadoEn = 1_700_000_000_000L
            )
        )
        apiService.respuestaLista = Response.success(
            FirestoreListResponse(
                documents = listOf(
                    docDto("EVT-1", "Nombre actualizado", -12.0673, -77.0337),
                    docDto("EVT-2", "Segundo Evento", -8.1116, -79.0287),
                    docDto("", "Documento sin ID", null, null)
                )
            )
        )

        val res = repo.sincronizarConApiRest()

        assertEquals(2, res.getOrNull())
        assertEquals(2, eventoDao.items.value.size)
        val evt1 = eventoDao.buscarPorRemoteId("EVT-1")
        assertNotNull(evt1)
        assertEquals("Nombre actualizado", evt1?.nombre)
        assertEquals(-12.0673, evt1?.latitud ?: 0.0, 0.0001)
    }

    @Test
    fun `sincronizarConApiRest retorna error en HTTP 500`() = runTest {
        apiService.respuestaLista = Response.error(
            500,
            "error".toResponseBody("application/json".toMediaType())
        )

        val res = repo.sincronizarConApiRest()

        assertTrue(res.isFailure)
        assertTrue(res.exceptionOrNull()?.message?.contains("500") == true)
    }

    @Test
    fun `sincronizarEventoEnNube crea en la nube y persiste remoteId localmente`() = runTest {
        val localId = eventoDao.insertar(
            EventoEntity(
                remoteId = null,
                nombre = "Feria Tech",
                descripcion = "Demo",
                lugar = "Trujillo",
                latitud = -8.1116,
                longitud = -79.0287,
                fechaInicio = Instant.parse("2026-11-20T18:00:00Z").toEpochMilli(),
                fechaFin = Instant.parse("2026-11-20T22:00:00Z").toEpochMilli(),
                aforoMaximo = 300,
                estado = "PROGRAMADO",
                creadoEn = Instant.now().toEpochMilli()
            )
        )

        val res = repo.sincronizarEventoEnNube(localId)

        assertTrue(res.isSuccess)
        assertEquals(1, apiService.creados.size)
        assertEquals(0, apiService.actualizados.size)
        val actualizado = eventoDao.buscarPorId(localId)
        assertEquals(res.getOrNull(), actualizado?.remoteId)
    }

    @Test
    fun `eliminarEventoEnNube ignora 404 y falla en 500`() = runTest {
        apiService.respuestaEliminar = Response.error(
            404,
            "not found".toResponseBody("application/json".toMediaType())
        )
        assertTrue(repo.eliminarEventoEnNube("EVT-404").isSuccess)

        apiService.respuestaEliminar = Response.error(
            500,
            "server error".toResponseBody("application/json".toMediaType())
        )
        assertTrue(repo.eliminarEventoEnNube("EVT-500").isFailure)
    }

    private fun docDto(id: String, nombre: String, lat: Double?, lng: Double?): EventoDocumentDto {
        return EventoDocumentDto(
            name = if (id.isBlank()) null else "projects/test/databases/(default)/documents/eventos/$id",
            fields = EventoFieldsDto(
                remoteId = if (id.isBlank()) null else FirestoreValueString(id),
                nombre = FirestoreValueString(nombre),
                lugar = FirestoreValueString("Lima"),
                latitud = lat?.let { FirestoreValueDouble(it) },
                longitud = lng?.let { FirestoreValueDouble(it) },
                aforoMaximo = FirestoreValueInt("250"),
                estado = FirestoreValueString("PROGRAMADO"),
                fechaInicio = FirestoreValueString("2026-11-20T18:00:00Z"),
                fechaFin = FirestoreValueString("2026-11-20T22:00:00Z")
            ),
            createTime = "2026-11-06T20:00:00Z"
        )
    }
}

private class EventosApiServiceFalso : EventosApiService {
    var respuestaLista: Response<FirestoreListResponse<EventoDocumentDto>> =
        Response.success(FirestoreListResponse(emptyList()))
    var respuestaEliminar: Response<Unit> = Response.success(Unit)
    val creados = mutableListOf<String?>()
    val actualizados = mutableListOf<String>()

    override suspend fun listarEventos(
        projectId: String,
        apiKey: String
    ): Response<FirestoreListResponse<EventoDocumentDto>> = respuestaLista

    override suspend fun obtenerEvento(
        projectId: String,
        documentId: String,
        apiKey: String
    ): Response<EventoDocumentDto> = Response.success(EventoDocumentDto())

    override suspend fun crearEvento(
        projectId: String,
        documentId: String?,
        apiKey: String,
        request: EventoRequestDto
    ): Response<EventoDocumentDto> {
        creados += documentId
        return Response.success(EventoDocumentDto(name = "projects/$projectId/databases/(default)/documents/eventos/$documentId"))
    }

    override suspend fun actualizarEvento(
        projectId: String,
        documentId: String,
        apiKey: String,
        request: EventoRequestDto
    ): Response<EventoDocumentDto> {
        actualizados += documentId
        return Response.success(EventoDocumentDto(name = "projects/$projectId/databases/(default)/documents/eventos/$documentId"))
    }

    override suspend fun eliminarEvento(
        projectId: String,
        documentId: String,
        apiKey: String
    ): Response<Unit> = respuestaEliminar
}

private class EventoDaoEnMemoria : EventoDao {
    val items = MutableStateFlow<List<EventoEntity>>(emptyList())
    private var seq = 1L

    override fun observarTodos(): Flow<List<EventoEntity>> = items
    override fun observarPorId(id: Long): Flow<EventoEntity?> =
        items.map { lista -> lista.firstOrNull { it.id == id } }
    override suspend fun buscarPorId(id: Long): EventoEntity? =
        items.value.firstOrNull { it.id == id }
    override fun buscar(texto: String): Flow<List<EventoEntity>> = items
    override suspend fun buscarPorRemoteId(remoteId: String): EventoEntity? =
        items.value.firstOrNull { it.remoteId == remoteId }

    override suspend fun insertar(evento: EventoEntity): Long {
        val id = seq++
        items.value = items.value + evento.copy(id = id)
        return id
    }

    override suspend fun actualizar(evento: EventoEntity) {
        items.value = items.value.map { if (it.id == evento.id) evento else it }
    }

    override suspend fun actualizarEstado(id: Long, estado: String) {
        items.value = items.value.map { if (it.id == id) it.copy(estado = estado) else it }
    }

    override suspend fun eliminar(id: Long) {
        items.value = items.value.filterNot { it.id == id }
    }
}

private class TipoEntradaDaoVacio : TipoEntradaDao {
    override fun observarPorEvento(eventoId: Long): Flow<List<TipoEntradaEntity>> = MutableStateFlow(emptyList())
    override suspend fun buscarPorId(id: Long): TipoEntradaEntity? = null
    override suspend fun insertar(tipo: TipoEntradaEntity): Long = 0L
    override suspend fun actualizar(tipo: TipoEntradaEntity) {}
    override suspend fun eliminar(id: Long) {}
}

private class EntradaDaoVacio : EntradaDao {
    override fun observarPorEvento(eventoId: Long): Flow<List<EntradaDetalleRow>> = MutableStateFlow(emptyList())
    override fun observarPorId(entradaId: Long): Flow<EntradaDetalleRow?> = MutableStateFlow(null)
    override suspend fun buscarPorCodigo(codigo: String): EntradaDetalleRow? = null
    override suspend fun insertar(entrada: EntradaEntity): Long = 0L
    override suspend fun marcarUsada(id: Long, momento: Long) {}
    override suspend fun anular(id: Long) {}
    override suspend fun existeValida(asistenteId: Long, eventoId: Long): Boolean = false
    override suspend fun contarPorEventoYEstado(eventoId: Long, estado: String): Int = 0
    override suspend fun contarVigentesPorTipo(tipoId: Long): Int = 0
}
