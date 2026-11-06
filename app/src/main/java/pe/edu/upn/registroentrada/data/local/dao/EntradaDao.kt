package pe.edu.upn.registroentrada.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow
import pe.edu.upn.registroentrada.data.local.entity.EntradaEntity
import pe.edu.upn.registroentrada.data.local.relation.EntradaDetalleRow

@Dao
interface EntradaDao {

    @Query(
        """
        SELECT e.id AS entradaId, e.codigo AS codigo, e.firma AS firma,
               e.evento_id AS eventoId, e.tipo_entrada_id AS tipoEntradaId,
               e.asistente_id AS asistenteId, e.estado AS estado,
               e.emitida_en AS emitidaEn, e.usada_en AS usadaEn,
               a.nombres || ' ' || a.apellidos AS nombreAsistente,
               a.documento AS documentoAsistente,
               t.nombre AS nombreTipoEntrada, t.color AS colorTipoEntrada,
               ev.nombre AS nombreEvento, ev.lugar AS lugarEvento
        FROM entradas e
        LEFT JOIN asistentes a ON a.id = e.asistente_id
        INNER JOIN tipos_entrada t ON t.id = e.tipo_entrada_id
        INNER JOIN eventos ev ON ev.id = e.evento_id
        WHERE e.evento_id = :eventoId
        ORDER BY e.emitida_en DESC
        """
    )
    fun observarPorEvento(eventoId: Long): Flow<List<EntradaDetalleRow>>

    @Query(
        """
        SELECT e.id AS entradaId, e.codigo AS codigo, e.firma AS firma,
               e.evento_id AS eventoId, e.tipo_entrada_id AS tipoEntradaId,
               e.asistente_id AS asistenteId, e.estado AS estado,
               e.emitida_en AS emitidaEn, e.usada_en AS usadaEn,
               a.nombres || ' ' || a.apellidos AS nombreAsistente,
               a.documento AS documentoAsistente,
               t.nombre AS nombreTipoEntrada, t.color AS colorTipoEntrada,
               ev.nombre AS nombreEvento, ev.lugar AS lugarEvento
        FROM entradas e
        LEFT JOIN asistentes a ON a.id = e.asistente_id
        INNER JOIN tipos_entrada t ON t.id = e.tipo_entrada_id
        INNER JOIN eventos ev ON ev.id = e.evento_id
        WHERE e.id = :entradaId
        LIMIT 1
        """
    )
    fun observarPorId(entradaId: Long): Flow<EntradaDetalleRow?>

    @Query(
        """
        SELECT e.id AS entradaId, e.codigo AS codigo, e.firma AS firma,
               e.evento_id AS eventoId, e.tipo_entrada_id AS tipoEntradaId,
               e.asistente_id AS asistenteId, e.estado AS estado,
               e.emitida_en AS emitidaEn, e.usada_en AS usadaEn,
               a.nombres || ' ' || a.apellidos AS nombreAsistente,
               a.documento AS documentoAsistente,
               t.nombre AS nombreTipoEntrada, t.color AS colorTipoEntrada,
               ev.nombre AS nombreEvento, ev.lugar AS lugarEvento
        FROM entradas e
        LEFT JOIN asistentes a ON a.id = e.asistente_id
        INNER JOIN tipos_entrada t ON t.id = e.tipo_entrada_id
        INNER JOIN eventos ev ON ev.id = e.evento_id
        WHERE e.codigo = :codigo
        LIMIT 1
        """
    )
    suspend fun buscarPorCodigo(codigo: String): EntradaDetalleRow?

    @Insert
    suspend fun insertar(entrada: EntradaEntity): Long

    @Query("UPDATE entradas SET estado = 'USADA', usada_en = :momento WHERE id = :id")
    suspend fun marcarUsada(id: Long, momento: Long)

    @Query("UPDATE entradas SET estado = 'ANULADA' WHERE id = :id")
    suspend fun anular(id: Long)

    @Query(
        "SELECT EXISTS(SELECT 1 FROM entradas WHERE asistente_id = :asistenteId " +
            "AND evento_id = :eventoId AND estado = 'VALIDA')"
    )
    suspend fun existeValida(asistenteId: Long, eventoId: Long): Boolean
    @Query("SELECT COUNT(*) FROM entradas WHERE evento_id = :eventoId AND estado = :estado")
    suspend fun contarPorEventoYEstado(eventoId: Long, estado: String): Int

    @Query("SELECT COUNT(*) FROM entradas WHERE tipo_entrada_id = :tipoId AND estado != 'ANULADA'")
    suspend fun contarVigentesPorTipo(tipoId: Long): Int
}
