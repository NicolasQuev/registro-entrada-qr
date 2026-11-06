package pe.edu.upn.registroentrada.data.repository

import java.time.Instant
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import pe.edu.upn.registroentrada.data.local.aEpochMillis
import pe.edu.upn.registroentrada.data.local.dao.EntradaDao
import pe.edu.upn.registroentrada.data.mapper.aDominio
import pe.edu.upn.registroentrada.data.mapper.aEntidad
import pe.edu.upn.registroentrada.domain.model.Entrada
import pe.edu.upn.registroentrada.domain.model.EntradaDetalle
import pe.edu.upn.registroentrada.domain.model.EstadoEntrada
import pe.edu.upn.registroentrada.domain.repository.EntradaRepository

@Singleton
class EntradaRepositoryImpl @Inject constructor(
    private val entradaDao: EntradaDao
) : EntradaRepository {

    override fun observarEntradasDeEvento(eventoId: Long): Flow<List<EntradaDetalle>> =
        entradaDao.observarPorEvento(eventoId).map { filas -> filas.map { it.aDominio() } }

    override fun observarEntrada(id: Long): Flow<EntradaDetalle?> =
        entradaDao.observarPorId(id).map { it?.aDominio() }

    override suspend fun buscarPorCodigo(codigo: String): EntradaDetalle? =
        entradaDao.buscarPorCodigo(codigo)?.aDominio()

    override suspend fun emitir(entrada: Entrada): Long =
        entradaDao.insertar(entrada.aEntidad())

    override suspend fun marcarUsada(entradaId: Long, momento: Instant) =
        entradaDao.marcarUsada(entradaId, momento.aEpochMillis())

    override suspend fun anular(entradaId: Long) =
        entradaDao.anular(entradaId)

    override suspend fun existeEntradaValida(asistenteId: Long, eventoId: Long): Boolean =
        entradaDao.existeValida(asistenteId, eventoId)

    override suspend fun contarPorEventoYEstado(eventoId: Long, estado: EstadoEntrada): Int =
        entradaDao.contarPorEventoYEstado(eventoId, estado.name)
}
