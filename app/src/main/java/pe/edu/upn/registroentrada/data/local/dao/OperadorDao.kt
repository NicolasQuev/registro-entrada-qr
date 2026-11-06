package pe.edu.upn.registroentrada.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow
import pe.edu.upn.registroentrada.data.local.entity.OperadorEntity

@Dao
interface OperadorDao {

    @Query("SELECT * FROM operadores WHERE activo = 1 ORDER BY nombre ASC")
    fun observarActivos(): Flow<List<OperadorEntity>>

    @Query("SELECT * FROM operadores ORDER BY nombre ASC")
    fun observarTodos(): Flow<List<OperadorEntity>>

    @Query("SELECT * FROM operadores WHERE usuario = :usuario LIMIT 1")
    suspend fun buscarPorUsuario(usuario: String): OperadorEntity?

    @Insert
    suspend fun insertar(operador: OperadorEntity): Long

    @Query("UPDATE operadores SET activo = :activo WHERE id = :id")
    suspend fun actualizarActivo(id: Long, activo: Boolean)

    @Query("SELECT COUNT(*) FROM operadores WHERE rol = 'ADMINISTRADOR' AND activo = 1")
    suspend fun contarAdministradoresActivos(): Int
}
