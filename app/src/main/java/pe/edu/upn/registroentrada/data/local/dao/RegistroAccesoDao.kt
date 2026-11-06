package pe.edu.upn.registroentrada.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow
import pe.edu.upn.registroentrada.data.local.entity.RegistroAccesoEntity
import pe.edu.upn.registroentrada.data.local.relation.RegistroAccesoDetalleRow

@Dao
interface RegistroAccesoDao {

    @Insert
    suspend fun insertar(registro: RegistroAccesoEntity): Long

    @Query(
        """
        SELECT r.id AS id, r.entrada_id AS entradaId, r.evento_id AS eventoId,
               r.codigo_escaneado AS codigoEscaneado, r.resultado AS resultado,
               r.puerta AS puerta, r.operador_id AS operadorId,
               r.fecha_hora AS fechaHora, r.observacion AS observacion,
               o.nombre AS nombreOperador,
               (a.nombres || ' ' || a.apellidos) AS nombreAsistente
        FROM registros_acceso r
        INNER JOIN operadores o ON o.id = r.operador_id
        LEFT JOIN entradas e ON e.id = r.entrada_id
        LEFT JOIN asistentes a ON a.id = e.asistente_id
        WHERE r.evento_id = :eventoId
        ORDER BY r.fecha_hora DESC
        """
    )
    fun observarPorEvento(eventoId: Long): Flow<List<RegistroAccesoDetalleRow>>

    @Query(
        "SELECT COUNT(*) FROM registros_acceso " +
            "WHERE evento_id = :eventoId AND resultado = :resultado"
    )
    suspend fun contarPorResultado(eventoId: Long, resultado: String): Int

    @Query(
        "SELECT COUNT(*) FROM registros_acceso " +
            "WHERE evento_id = :eventoId AND resultado = 'PERMITIDO'"
    )
    fun observarPermitidos(eventoId: Long): Flow<Int>
}
