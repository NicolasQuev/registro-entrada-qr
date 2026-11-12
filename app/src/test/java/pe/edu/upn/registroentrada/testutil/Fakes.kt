package pe.edu.upn.registroentrada.testutil

import java.time.Instant
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import pe.edu.upn.registroentrada.core.GeneradorId
import pe.edu.upn.registroentrada.core.Reloj
import pe.edu.upn.registroentrada.domain.model.Aforo
import pe.edu.upn.registroentrada.domain.model.AjustesUsuario
import pe.edu.upn.registroentrada.domain.model.Asistente
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
import pe.edu.upn.registroentrada.domain.repository.AccesoRepository
import pe.edu.upn.registroentrada.domain.repository.AjustesRepository
import pe.edu.upn.registroentrada.domain.repository.AsistenteRepository
import pe.edu.upn.registroentrada.domain.repository.EntradaRepository
import pe.edu.upn.registroentrada.domain.repository.EventoRepository
import pe.edu.upn.registroentrada.domain.repository.OperadorRepository

/** Reloj fijo para que las pruebas del dominio sean deterministas. */
class RelojFijo(var instante: Instant = Instant.parse("2026-01-01T12:00:00Z")) : Reloj {
    override fun ahora(): Instant = instante
}

/** Generador de ids predecible (uuid-1, uuid-2, …). */
class GeneradorIdFijo : GeneradorId {
    private var contador = 0
    override fun nuevo(): String = "uuid-${++contador}"
}

class EventoRepositoryFalso : EventoRepository {
    val eventos = MutableStateFlow<List<Evento>>(emptyList())
    val tipos = MutableStateFlow<List<TipoEntrada>>(emptyList())
    var vigentesPorTipo: (Long) -> Int = { 0 }
    private var siguienteEvento = 1L
    private var siguienteTipo = 1L

    override fun observarEventos(): Flow<List<Evento>> = eventos
    override fun observarEvento(id: Long): Flow<Evento?> =
        eventos.map { lista -> lista.firstOrNull { it.id == id } }
    override fun buscarEventos(texto: String): Flow<List<Evento>> =
        eventos.map { lista -> lista.filter { it.nombre.contains(texto, ignoreCase = true) } }

    override suspend fun guardar(evento: Evento): Long {
        val id = siguienteEvento++
        eventos.value = eventos.value + evento.copy(id = id)
        return id
    }

    override suspend fun actualizar(evento: Evento) {
        eventos.value = eventos.value.map { if (it.id == evento.id) evento else it }
    }

    override suspend fun cambiarEstado(id: Long, estado: EstadoEvento) {
        eventos.value = eventos.value.map { if (it.id == id) it.copy(estado = estado) else it }
    }

    override suspend fun eliminarEvento(id: Long) {
        eventos.value = eventos.value.filterNot { it.id == id }
        tipos.value = tipos.value.filterNot { it.eventoId == id }
    }

    var resultadoSync: Result<Int>? = null

    override suspend fun sincronizarConApiRest(): Result<Int> =
        resultadoSync ?: Result.success(eventos.value.size)
    override suspend fun sincronizarEventoEnNube(eventoId: Long): Result<String> = Result.success("REMOTE-$eventoId")
    override suspend fun eliminarEventoEnNube(remoteId: String): Result<Unit> = Result.success(Unit)

    override fun observarTiposDeEntrada(eventoId: Long): Flow<List<TipoEntrada>> =
        tipos.map { lista -> lista.filter { it.eventoId == eventoId } }

    override suspend fun obtenerTipoDeEntrada(tipoId: Long): TipoEntrada? =
        tipos.value.firstOrNull { it.id == tipoId }

    override suspend fun guardarTipoDeEntrada(tipo: TipoEntrada): Long =
        if (tipo.id == 0L) {
            val id = siguienteTipo++
            tipos.value = tipos.value + tipo.copy(id = id)
            id
        } else {
            tipos.value = tipos.value.map { if (it.id == tipo.id) tipo else it }
            tipo.id
        }

    override suspend fun eliminarTipoDeEntrada(tipoId: Long) {
        tipos.value = tipos.value.filterNot { it.id == tipoId }
    }

    override suspend fun contarEntradasVigentesPorTipo(tipoId: Long): Int = vigentesPorTipo(tipoId)
}

class AsistenteRepositoryFalso : AsistenteRepository {
    val asistentes = MutableStateFlow<List<Asistente>>(emptyList())
    private var siguienteId = 1L

    override fun observarAsistentes(): Flow<List<Asistente>> = asistentes
    override fun observarAsistente(id: Long): Flow<Asistente?> =
        asistentes.map { lista -> lista.firstOrNull { it.id == id } }
    override fun buscar(texto: String): Flow<List<Asistente>> = asistentes
    override suspend fun buscarPorDocumento(documento: String): Asistente? =
        asistentes.value.firstOrNull { it.documento == documento }
    override suspend fun guardar(asistente: Asistente): Long {
        val id = siguienteId++
        asistentes.value = asistentes.value + asistente.copy(id = id)
        return id
    }
}

class EntradaRepositoryFalso : EntradaRepository {
    val entradas = MutableStateFlow<List<Entrada>>(emptyList())
    private var siguienteId = 1L

    private fun detalle(entrada: Entrada) = EntradaDetalle(
        entrada = entrada,
        nombreAsistente = "Asistente ${entrada.asistenteId}",
        documentoAsistente = "0000000${entrada.asistenteId}",
        nombreTipoEntrada = "General",
        colorTipoEntrada = "#2E74B5",
        nombreEvento = "Evento ${entrada.eventoId}",
        lugarEvento = "Lugar"
    )

    override fun observarEntradasDeEvento(eventoId: Long): Flow<List<EntradaDetalle>> =
        entradas.map { lista -> lista.filter { it.eventoId == eventoId }.map(::detalle) }

    override fun observarEntrada(id: Long): Flow<EntradaDetalle?> =
        entradas.map { lista -> lista.firstOrNull { it.id == id }?.let(::detalle) }

    override suspend fun buscarPorCodigo(codigo: String): EntradaDetalle? =
        entradas.value.firstOrNull { it.codigo == codigo }?.let(::detalle)

    override suspend fun emitir(entrada: Entrada): Long {
        val id = siguienteId++
        entradas.value = entradas.value + entrada.copy(id = id)
        return id
    }

    override suspend fun marcarUsada(entradaId: Long, momento: Instant) {
        entradas.value = entradas.value.map {
            if (it.id == entradaId) it.copy(estado = EstadoEntrada.USADA, usadaEn = momento) else it
        }
    }

    override suspend fun anular(entradaId: Long) {
        entradas.value = entradas.value.map {
            if (it.id == entradaId) it.copy(estado = EstadoEntrada.ANULADA) else it
        }
    }

    override suspend fun existeEntradaValida(asistenteId: Long, eventoId: Long): Boolean =
        entradas.value.any {
            it.asistenteId == asistenteId && it.eventoId == eventoId &&
                it.estado == EstadoEntrada.VALIDA
        }

    override suspend fun contarPorEventoYEstado(eventoId: Long, estado: EstadoEntrada): Int =
        entradas.value.count { it.eventoId == eventoId && it.estado == estado }
}

class AccesoRepositoryFalso : AccesoRepository {
    val registros = MutableStateFlow<List<RegistroAcceso>>(emptyList())
    val entradasMarcadasUsadas = mutableListOf<Long>()
    var aforo: Aforo = Aforo(ocupados = 0, maximo = 100)

    /**
     * Simula el efecto de la transacción real: marcar la entrada como USADA
     * dentro del mismo registro de acceso (ver AccesoRepositoryImpl).
     */
    var alMarcarUsada: suspend (Long, Instant) -> Unit = { _, _ -> }

    override fun observarRegistros(
        eventoId: Long,
        filtro: FiltroRegistros
    ): Flow<List<RegistroAccesoDetalle>> = observarTodosRegistros(eventoId)

    override fun observarTodosRegistros(eventoId: Long): Flow<List<RegistroAccesoDetalle>> =
        registros.map { lista ->
            lista.filter { it.eventoId == eventoId }
                .map { RegistroAccesoDetalle(it, "Operador", null) }
        }

    override suspend fun contarPorResultado(eventoId: Long, resultado: ResultadoAcceso): Int =
        registros.value.count { it.eventoId == eventoId && it.resultado == resultado }

    override fun observarAforo(eventoId: Long): Flow<Aforo> = MutableStateFlow(aforo)

    override suspend fun registrarAcceso(
        registro: RegistroAcceso,
        marcarEntradaUsada: Boolean
    ): Long {
        val id = (registros.value.maxOfOrNull { it.id } ?: 0L) + 1
        registros.value = registros.value + registro.copy(id = id)
        if (marcarEntradaUsada && registro.entradaId != null) {
            entradasMarcadasUsadas += registro.entradaId
            alMarcarUsada(registro.entradaId, registro.fechaHora)
        }
        return id
    }
}

class OperadorRepositoryFalso : OperadorRepository {
    val operadores = MutableStateFlow<List<Operador>>(emptyList())

    override fun observarOperadoresActivos(): Flow<List<Operador>> =
        operadores.map { lista -> lista.filter { it.activo } }

    override fun observarOperadores(): Flow<List<Operador>> = operadores

    override suspend fun buscarPorUsuario(usuario: String): Operador? =
        operadores.value.firstOrNull { it.usuario == usuario }

    override suspend fun guardar(operador: Operador): Long {
        operadores.value = operadores.value + operador
        return operador.id
    }

    override suspend fun actualizarActivo(id: Long, activo: Boolean) {
        operadores.value = operadores.value.map { if (it.id == id) it.copy(activo = activo) else it }
    }

    override suspend fun contarAdministradoresActivos(): Int =
        operadores.value.count { it.rol == pe.edu.upn.registroentrada.domain.model.RolOperador.ADMINISTRADOR && it.activo }
}

class AjustesRepositoryFalso : AjustesRepository {
    val flujo = MutableStateFlow(AjustesUsuario())

    override val ajustes: Flow<AjustesUsuario> = flujo

    override suspend fun guardarOperadorActivo(id: Long, nombre: String) {
        flujo.value = flujo.value.copy(operadorActivoId = id, operadorActivoNombre = nombre)
    }

    override suspend fun limpiarOperadorActivo() {
        flujo.value = flujo.value.copy(
            operadorActivoId = AjustesUsuario.SIN_SESION,
            operadorActivoNombre = ""
        )
    }

    override suspend fun guardarEventoActivo(eventoId: Long, puerta: String) {
        flujo.value = flujo.value.copy(eventoActivoId = eventoId, puertaActiva = puerta)
    }

    override suspend fun guardarTema(tema: TemaApp) {
        flujo.value = flujo.value.copy(tema = tema)
    }

    override suspend fun guardarSonido(activo: Boolean) {
        flujo.value = flujo.value.copy(sonidoEscaneo = activo)
    }

    override suspend fun guardarVibracion(activa: Boolean) {
        flujo.value = flujo.value.copy(vibracionEscaneo = activa)
    }

    override suspend fun registrarIntentoFallido(): Int {
        val total = flujo.value.intentosPinFallidos + 1
        flujo.value = flujo.value.copy(intentosPinFallidos = total)
        return total
    }

    override suspend fun reiniciarIntentos() {
        flujo.value = flujo.value.copy(intentosPinFallidos = 0, bloqueoHasta = 0L)
    }

    override suspend fun bloquearHasta(momento: Instant) {
        flujo.value = flujo.value.copy(bloqueoHasta = momento.toEpochMilli())
    }

    override suspend fun marcarRespaldo(momento: Instant) {
        flujo.value = flujo.value.copy(ultimoRespaldo = momento.toEpochMilli())
    }
}
