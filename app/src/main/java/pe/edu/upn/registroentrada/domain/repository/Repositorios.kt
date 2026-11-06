package pe.edu.upn.registroentrada.domain.repository

import java.time.Instant
import kotlinx.coroutines.flow.Flow
import pe.edu.upn.registroentrada.domain.model.AjustesUsuario
import pe.edu.upn.registroentrada.domain.model.Asistente
import pe.edu.upn.registroentrada.domain.model.Aforo
import pe.edu.upn.registroentrada.domain.model.Entrada
import pe.edu.upn.registroentrada.domain.model.EntradaDetalle
import pe.edu.upn.registroentrada.domain.model.EstadoEntrada
import pe.edu.upn.registroentrada.domain.model.EstadoEvento
import pe.edu.upn.registroentrada.domain.model.Evento
import pe.edu.upn.registroentrada.domain.model.FiltroRegistros
import pe.edu.upn.registroentrada.domain.model.Operador
import pe.edu.upn.registroentrada.domain.model.RegistroAcceso
import pe.edu.upn.registroentrada.domain.model.RegistroAccesoDetalle
import pe.edu.upn.registroentrada.domain.model.ResultadoAcceso
import pe.edu.upn.registroentrada.domain.model.TemaApp
import pe.edu.upn.registroentrada.domain.model.TipoEntrada

/**
 * Contratos de acceso a datos. La capa `data/` los implementa y la capa
 * `ui/` nunca conoce Room ni DataStore. Al vivir en `domain/` no importan
 * nada de Android (RNF-30).
 */
interface EventoRepository {
    fun observarEventos(): Flow<List<Evento>>
    fun observarEvento(id: Long): Flow<Evento?>
    fun buscarEventos(texto: String): Flow<List<Evento>>
    suspend fun guardar(evento: Evento): Long
    suspend fun actualizar(evento: Evento)
    suspend fun cambiarEstado(id: Long, estado: EstadoEvento)
    suspend fun eliminarEvento(id: Long)
    suspend fun sincronizarConApiRest(): Result<Int>
    suspend fun sincronizarEventoEnNube(eventoId: Long): Result<String>
    suspend fun eliminarEventoEnNube(remoteId: String): Result<Unit>
    fun observarTiposDeEntrada(eventoId: Long): Flow<List<TipoEntrada>>
    suspend fun obtenerTipoDeEntrada(tipoId: Long): TipoEntrada?
    suspend fun guardarTipoDeEntrada(tipo: TipoEntrada): Long
    suspend fun eliminarTipoDeEntrada(tipoId: Long)
    suspend fun contarEntradasVigentesPorTipo(tipoId: Long): Int
}

interface AsistenteRepository {
    fun observarAsistentes(): Flow<List<Asistente>>
    fun observarAsistente(id: Long): Flow<Asistente?>
    fun buscar(texto: String): Flow<List<Asistente>>
    suspend fun buscarPorDocumento(documento: String): Asistente?
    suspend fun guardar(asistente: Asistente): Long
}

interface EntradaRepository {
    fun observarEntradasDeEvento(eventoId: Long): Flow<List<EntradaDetalle>>
    fun observarEntrada(id: Long): Flow<EntradaDetalle?>
    suspend fun buscarPorCodigo(codigo: String): EntradaDetalle?
    suspend fun emitir(entrada: Entrada): Long
    suspend fun marcarUsada(entradaId: Long, momento: Instant)
    suspend fun anular(entradaId: Long)
    suspend fun existeEntradaValida(asistenteId: Long, eventoId: Long): Boolean
    suspend fun contarPorEventoYEstado(eventoId: Long, estado: EstadoEntrada): Int
}

interface AccesoRepository {
    fun observarRegistros(eventoId: Long, filtro: FiltroRegistros): Flow<List<RegistroAccesoDetalle>>
    fun observarTodosRegistros(eventoId: Long): Flow<List<RegistroAccesoDetalle>>
    suspend fun contarPorResultado(eventoId: Long, resultado: ResultadoAcceso): Int
    fun observarAforo(eventoId: Long): Flow<Aforo>
    /**
     * Persiste el intento de acceso. Si [marcarEntradaUsada] es true, marca
     * la entrada como USADA en la MISMA transacción (RN-01, HT-03).
     */
    suspend fun registrarAcceso(registro: RegistroAcceso, marcarEntradaUsada: Boolean): Long
}

interface OperadorRepository {
    fun observarOperadoresActivos(): Flow<List<Operador>>
    fun observarOperadores(): Flow<List<Operador>>
    suspend fun buscarPorUsuario(usuario: String): Operador?
    suspend fun guardar(operador: Operador): Long
    suspend fun actualizarActivo(id: Long, activo: Boolean)
    suspend fun contarAdministradoresActivos(): Int
}

interface AjustesRepository {
    val ajustes: Flow<AjustesUsuario>
    suspend fun guardarOperadorActivo(id: Long, nombre: String)
    suspend fun limpiarOperadorActivo()
    suspend fun guardarEventoActivo(eventoId: Long, puerta: String)
    suspend fun guardarTema(tema: TemaApp)
    suspend fun guardarSonido(activo: Boolean)
    suspend fun guardarVibracion(activa: Boolean)
    suspend fun registrarIntentoFallido(): Int
    suspend fun reiniciarIntentos()
    suspend fun bloquearHasta(momento: Instant)
    suspend fun marcarRespaldo(momento: Instant)
}
