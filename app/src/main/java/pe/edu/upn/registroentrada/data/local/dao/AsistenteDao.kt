package pe.edu.upn.registroentrada.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow
import pe.edu.upn.registroentrada.data.local.entity.AsistenteEntity

@Dao
interface AsistenteDao {

    @Query("SELECT * FROM asistentes ORDER BY apellidos ASC, nombres ASC")
    fun observarTodos(): Flow<List<AsistenteEntity>>

    @Query("SELECT * FROM asistentes WHERE id = :id LIMIT 1")
    fun observarPorId(id: Long): Flow<AsistenteEntity?>

    @Query(
        "SELECT * FROM asistentes WHERE documento LIKE '%' || :texto || '%' " +
            "OR nombres LIKE '%' || :texto || '%' OR apellidos LIKE '%' || :texto || '%' " +
            "ORDER BY apellidos ASC, nombres ASC"
    )
    fun buscar(texto: String): Flow<List<AsistenteEntity>>

    @Query("SELECT * FROM asistentes WHERE documento = :documento LIMIT 1")
    suspend fun buscarPorDocumento(documento: String): AsistenteEntity?

    @Insert
    suspend fun insertar(asistente: AsistenteEntity): Long
}
