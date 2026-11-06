package pe.edu.upn.registroentrada.data.repository

import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import pe.edu.upn.registroentrada.data.local.dao.OperadorDao
import pe.edu.upn.registroentrada.data.mapper.aDominio
import pe.edu.upn.registroentrada.data.mapper.aEntidad
import pe.edu.upn.registroentrada.domain.model.Operador
import pe.edu.upn.registroentrada.domain.repository.OperadorRepository

@Singleton
class OperadorRepositoryImpl @Inject constructor(
    private val operadorDao: OperadorDao
) : OperadorRepository {

    override fun observarOperadoresActivos(): Flow<List<Operador>> =
        operadorDao.observarActivos().map { filas -> filas.map { it.aDominio() } }

    override fun observarOperadores(): Flow<List<Operador>> =
        operadorDao.observarTodos().map { filas -> filas.map { it.aDominio() } }

    override suspend fun buscarPorUsuario(usuario: String): Operador? =
        operadorDao.buscarPorUsuario(usuario)?.aDominio()

    override suspend fun guardar(operador: Operador): Long =
        operadorDao.insertar(operador.aEntidad())

    override suspend fun actualizarActivo(id: Long, activo: Boolean) =
        operadorDao.actualizarActivo(id, activo)

    override suspend fun contarAdministradoresActivos(): Int =
        operadorDao.contarAdministradoresActivos()
}
