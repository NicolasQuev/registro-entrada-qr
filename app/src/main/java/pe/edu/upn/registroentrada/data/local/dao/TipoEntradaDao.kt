package pe.edu.upn.registroentrada.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow
import pe.edu.upn.registroentrada.data.local.entity.TipoEntradaEntity

@Dao
interface TipoEntradaDao {

    @Query("SELECT * FROM tipos_entrada WHERE evento_id = :eventoId ORDER BY id ASC")
    fun observarPorEvento(eventoId: Long): Flow<List<TipoEntradaEntity>>

    @Query("SELECT * FROM tipos_entrada WHERE id = :id LIMIT 1")
    suspend fun buscarPorId(id: Long): TipoEntradaEntity?

    @Insert
    suspend fun insertar(tipo: TipoEntradaEntity): Long

    @Update
    suspend fun actualizar(tipo: TipoEntradaEntity)

    @Query("DELETE FROM tipos_entrada WHERE id = :id")
    suspend fun eliminar(id: Long)
}
