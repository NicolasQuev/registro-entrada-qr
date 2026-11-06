package pe.edu.upn.registroentrada.data.repository

import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import pe.edu.upn.registroentrada.data.local.dao.AsistenteDao
import pe.edu.upn.registroentrada.data.mapper.aDominio
import pe.edu.upn.registroentrada.data.mapper.aEntidad
import pe.edu.upn.registroentrada.domain.model.Asistente
import pe.edu.upn.registroentrada.domain.repository.AsistenteRepository

@Singleton
class AsistenteRepositoryImpl @Inject constructor(
    private val asistenteDao: AsistenteDao
) : AsistenteRepository {

    override fun observarAsistentes(): Flow<List<Asistente>> =
        asistenteDao.observarTodos().map { filas -> filas.map { it.aDominio() } }

    override fun observarAsistente(id: Long): Flow<Asistente?> =
        asistenteDao.observarPorId(id).map { it?.aDominio() }

    override fun buscar(texto: String): Flow<List<Asistente>> =
        asistenteDao.buscar(texto).map { filas -> filas.map { it.aDominio() } }

    override suspend fun buscarPorDocumento(documento: String): Asistente? =
        asistenteDao.buscarPorDocumento(documento)?.aDominio()

    override suspend fun guardar(asistente: Asistente): Long =
        asistenteDao.insertar(asistente.aEntidad())
}
