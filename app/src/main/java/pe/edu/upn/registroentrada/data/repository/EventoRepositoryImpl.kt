package pe.edu.upn.registroentrada.data.repository

import javax.inject.Inject
import javax.inject.Named
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import pe.edu.upn.registroentrada.data.local.dao.EntradaDao
import pe.edu.upn.registroentrada.data.local.dao.EventoDao
import pe.edu.upn.registroentrada.data.local.dao.TipoEntradaDao
import pe.edu.upn.registroentrada.data.mapper.aDominio
import pe.edu.upn.registroentrada.data.mapper.aEntidad
import pe.edu.upn.registroentrada.data.mapper.aRequestDto
import pe.edu.upn.registroentrada.data.remote.api.EventosApiService
import pe.edu.upn.registroentrada.domain.model.EstadoEvento
import pe.edu.upn.registroentrada.domain.model.Evento
import pe.edu.upn.registroentrada.domain.model.TipoEntrada
import pe.edu.upn.registroentrada.domain.repository.EventoRepository
import java.io.IOException

@Singleton
class EventoRepositoryImpl @Inject constructor(
    private val eventoDao: EventoDao,
    private val tipoEntradaDao: TipoEntradaDao,
    private val entradaDao: EntradaDao,
    private val apiService: EventosApiService,
    @Named("projectId") private val projectId: String,
    @Named("apiKey") private val apiKey: String
) : EventoRepository {

    override fun observarEventos(): Flow<List<Evento>> =
        eventoDao.observarTodos().map { filas -> filas.map { it.aDominio() } }

    override fun observarEvento(id: Long): Flow<Evento?> =
        eventoDao.observarPorId(id).map { it?.aDominio() }

    override fun buscarEventos(texto: String): Flow<List<Evento>> =
        eventoDao.buscar(texto).map { filas -> filas.map { it.aDominio() } }

    override suspend fun guardar(evento: Evento): Long {
        val idLocal = eventoDao.insertar(evento.aEntidad())
        // Intentar registrar de forma asíncrona en la nube si hay red
        runCatching {
            sincronizarEventoEnNube(idLocal)
        }
        return idLocal
    }

    override suspend fun actualizar(evento: Evento) {
        eventoDao.actualizar(evento.aEntidad())
        if (evento.remoteId != null) {
            runCatching {
                apiService.actualizarEvento(
                    projectId = projectId,
                    documentId = evento.remoteId,
                    apiKey = apiKey,
                    request = evento.aRequestDto()
                )
            }
        }
    }

    override suspend fun cambiarEstado(id: Long, estado: EstadoEvento) {
        eventoDao.actualizarEstado(id, estado.name)
        val evento = eventoDao.buscarPorId(id)?.aDominio()
        if (evento?.remoteId != null) {
            runCatching {
                apiService.actualizarEvento(
                    projectId = projectId,
                    documentId = evento.remoteId,
                    apiKey = apiKey,
                    request = evento.copy(estado = estado).aRequestDto()
                )
            }
        }
    }

    override suspend fun eliminarEvento(id: Long) {
        val entidad = eventoDao.buscarPorId(id)
        eventoDao.eliminar(id)
        if (entidad?.remoteId != null) {
            runCatching {
                eliminarEventoEnNube(entidad.remoteId)
            }
        }
    }

    /**
     * Operación READ & SYNC de API REST:
     * Descarga los eventos desde la nube mediante Retrofit y los integra/actualiza en Room.
     */
    override suspend fun sincronizarConApiRest(): Result<Int> = runCatching {
        val respuesta = apiService.listarEventos(projectId, apiKey)
        if (!respuesta.isSuccessful) {
            throw IOException("Error en API REST HTTP ${respuesta.code()}: ${respuesta.message()}")
        }

        val documentos = respuesta.body()?.documents ?: emptyList()
        var sincronizados = 0

        for (doc in documentos) {
            if (doc.documentId.isBlank()) continue
            val eventoRemoto = doc.aDominio()
            val existente = doc.documentId.let { eventoDao.buscarPorRemoteId(it) }

            if (existente == null) {
                eventoDao.insertar(eventoRemoto.aEntidad())
                sincronizados++
            } else {
                eventoDao.actualizar(
                    eventoRemoto.copy(id = existente.id).aEntidad()
                )
                sincronizados++
            }
        }

        sincronizados
    }

    /**
     * Operación CREATE / UPDATE en API REST:
     * Sube un evento local a Cloud Firestore mediante Retrofit.
     */
    override suspend fun sincronizarEventoEnNube(eventoId: Long): Result<String> = runCatching {
        val local = eventoDao.buscarPorId(eventoId)?.aDominio()
            ?: throw IllegalArgumentException("Evento local #$eventoId no existe")

        val remoteId = local.remoteId ?: "EVT-${local.id}-${System.currentTimeMillis()}"
        val eventoParaSubir = local.copy(remoteId = remoteId)

        val respuesta = if (local.remoteId == null) {
            apiService.crearEvento(
                projectId = projectId,
                documentId = remoteId,
                apiKey = apiKey,
                request = eventoParaSubir.aRequestDto()
            )
        } else {
            apiService.actualizarEvento(
                projectId = projectId,
                documentId = local.remoteId,
                apiKey = apiKey,
                request = eventoParaSubir.aRequestDto()
            )
        }

        if (!respuesta.isSuccessful) {
            throw IOException("Fallo al guardar en la nube HTTP ${respuesta.code()}: ${respuesta.message()}")
        }

        // Guardar el remoteId asignado en la BD local
        eventoDao.actualizar(eventoParaSubir.aEntidad())
        remoteId
    }

    /**
     * Operación DELETE en API REST:
     * Elimina el documento en la nube mediante Retrofit.
     */
    override suspend fun eliminarEventoEnNube(remoteId: String): Result<Unit> = runCatching {
        val respuesta = apiService.eliminarEvento(projectId, remoteId, apiKey)
        if (!respuesta.isSuccessful && respuesta.code() != 404) {
            throw IOException("Fallo al eliminar en la nube HTTP ${respuesta.code()}: ${respuesta.message()}")
        }
    }

    override fun observarTiposDeEntrada(eventoId: Long): Flow<List<TipoEntrada>> =
        tipoEntradaDao.observarPorEvento(eventoId).map { filas -> filas.map { it.aDominio() } }

    override suspend fun obtenerTipoDeEntrada(tipoId: Long): TipoEntrada? =
        tipoEntradaDao.buscarPorId(tipoId)?.aDominio()

    override suspend fun guardarTipoDeEntrada(tipo: TipoEntrada): Long =
        if (tipo.id == 0L) {
            tipoEntradaDao.insertar(tipo.aEntidad())
        } else {
            tipoEntradaDao.actualizar(tipo.aEntidad())
            tipo.id
        }

    override suspend fun eliminarTipoDeEntrada(tipoId: Long) =
        tipoEntradaDao.eliminar(tipoId)

    override suspend fun contarEntradasVigentesPorTipo(tipoId: Long): Int =
        entradaDao.contarVigentesPorTipo(tipoId)
}
