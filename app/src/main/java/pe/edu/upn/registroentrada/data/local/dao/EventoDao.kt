package pe.edu.upn.registroentrada.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow
import pe.edu.upn.registroentrada.data.local.entity.EventoEntity

@Dao
interface EventoDao {

    @Query("SELECT * FROM eventos ORDER BY fecha_inicio DESC")
    fun observarTodos(): Flow<List<EventoEntity>>

    @Query("SELECT * FROM eventos WHERE id = :id LIMIT 1")
    fun observarPorId(id: Long): Flow<EventoEntity?>

    @Query("SELECT * FROM eventos WHERE id = :id LIMIT 1")
    suspend fun buscarPorId(id: Long): EventoEntity?

    @Query(
        "SELECT * FROM eventos WHERE nombre LIKE '%' || :texto || '%' " +
            "ORDER BY fecha_inicio DESC"
    )
    fun buscar(texto: String): Flow<List<EventoEntity>>

    @Query("SELECT * FROM eventos WHERE remote_id = :remoteId LIMIT 1")
    suspend fun buscarPorRemoteId(remoteId: String): EventoEntity?

    @Insert
    suspend fun insertar(evento: EventoEntity): Long

    @Update
    suspend fun actualizar(evento: EventoEntity)

    @Query("UPDATE eventos SET estado = :estado WHERE id = :id")
    suspend fun actualizarEstado(id: Long, estado: String)

    @Query("DELETE FROM eventos WHERE id = :id")
    suspend fun eliminar(id: Long)
}
