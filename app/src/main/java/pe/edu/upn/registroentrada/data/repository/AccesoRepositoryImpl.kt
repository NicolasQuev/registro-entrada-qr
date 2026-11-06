package pe.edu.upn.registroentrada.data.repository

import androidx.room.withTransaction
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import pe.edu.upn.registroentrada.data.local.AppDatabase
import pe.edu.upn.registroentrada.data.local.aEpochMillis
import pe.edu.upn.registroentrada.data.local.dao.EntradaDao
import pe.edu.upn.registroentrada.data.local.dao.EventoDao
import pe.edu.upn.registroentrada.data.local.dao.RegistroAccesoDao
import pe.edu.upn.registroentrada.data.mapper.aDominio
import pe.edu.upn.registroentrada.data.mapper.aEntidad
import pe.edu.upn.registroentrada.domain.model.Aforo
import pe.edu.upn.registroentrada.domain.model.FiltroRegistros
import pe.edu.upn.registroentrada.domain.model.RegistroAcceso
import pe.edu.upn.registroentrada.domain.model.RegistroAccesoDetalle
import pe.edu.upn.registroentrada.domain.model.ResultadoAcceso
import pe.edu.upn.registroentrada.domain.repository.AccesoRepository

@Singleton
class AccesoRepositoryImpl @Inject constructor(
    private val baseDeDatos: AppDatabase,
    private val registroAccesoDao: RegistroAccesoDao,
    private val entradaDao: EntradaDao,
    private val eventoDao: EventoDao
) : AccesoRepository {

    override fun observarTodosRegistros(eventoId: Long): Flow<List<RegistroAccesoDetalle>> =
        registroAccesoDao.observarPorEvento(eventoId).map { filas -> filas.map { it.aDominio() } }

    override fun observarRegistros(
        eventoId: Long,
        filtro: FiltroRegistros
    ): Flow<List<RegistroAccesoDetalle>> =
        observarTodosRegistros(eventoId).map { registros ->
            registros.filter { detalle ->
                val registro = detalle.registro
                (filtro.fechaDesde == null || !registro.fechaHora.isBefore(filtro.fechaDesde)) &&
                    (filtro.fechaHasta == null || !registro.fechaHora.isAfter(filtro.fechaHasta)) &&
                    (filtro.puerta.isNullOrBlank() ||
                        registro.puerta.equals(filtro.puerta, ignoreCase = true)) &&
                    (filtro.resultado == null || registro.resultado == filtro.resultado) &&
                    (filtro.operadorId == null || registro.operadorId == filtro.operadorId)
            }
        }

    override suspend fun contarPorResultado(eventoId: Long, resultado: ResultadoAcceso): Int =
        registroAccesoDao.contarPorResultado(eventoId, resultado.name)

    override fun observarAforo(eventoId: Long): Flow<Aforo> = combine(
        eventoDao.observarPorId(eventoId),
        registroAccesoDao.observarPermitidos(eventoId)
    ) { evento, permitidos ->
        Aforo(ocupados = permitidos, maximo = evento?.aforoMaximo ?: 0)
    }

    override suspend fun registrarAcceso(
        registro: RegistroAcceso,
        marcarEntradaUsada: Boolean
    ): Long = baseDeDatos.withTransaction {
        // RN-01: leer/actualizar el estado de la entrada y registrar el
        // intento dentro de la MISMA transacción.
        if (marcarEntradaUsada && registro.entradaId != null) {
            entradaDao.marcarUsada(registro.entradaId, registro.fechaHora.aEpochMillis())
        }
        registroAccesoDao.insertar(registro.aEntidad())
    }
}
