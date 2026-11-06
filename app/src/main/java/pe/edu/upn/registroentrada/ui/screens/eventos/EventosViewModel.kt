package pe.edu.upn.registroentrada.ui.screens.eventos

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import pe.edu.upn.registroentrada.core.Resultado
import pe.edu.upn.registroentrada.core.network.EstadoCarga
import pe.edu.upn.registroentrada.core.network.MonitorConectividad
import pe.edu.upn.registroentrada.domain.model.Evento
import pe.edu.upn.registroentrada.domain.repository.EventoRepository
import pe.edu.upn.registroentrada.domain.usecase.SincronizarEventosRestUseCase

data class EventosUiState(
    val eventos: List<Evento> = emptyList(),
    val busqueda: String = "",
    val estadoSincronizacion: EstadoCarga<Int> = EstadoCarga.Inactivo,
    val hayConexion: Boolean = true
)

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class EventosViewModel @Inject constructor(
    private val eventos: EventoRepository,
    private val sincronizarRest: SincronizarEventosRestUseCase,
    private val monitorConectividad: MonitorConectividad
) : ViewModel() {

    private val busqueda = MutableStateFlow("")
    private val estadoSync = MutableStateFlow<EstadoCarga<Int>>(EstadoCarga.Inactivo)

    private val _mensajes = MutableSharedFlow<String>()
    val mensajes: SharedFlow<String> = _mensajes.asSharedFlow()

    private val eventosFiltrados = busqueda.flatMapLatest { texto ->
        if (texto.isBlank()) eventos.observarEventos() else eventos.buscarEventos(texto)
    }

    val estado = combine(
        busqueda,
        eventosFiltrados,
        estadoSync,
        monitorConectividad.estaConectado
    ) { texto, lista, sync, conectado ->
        EventosUiState(
            eventos = lista,
            busqueda = texto,
            estadoSincronizacion = sync,
            hayConexion = conectado
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = EventosUiState()
    )

    init {
        // Intentar sincronización automática inicial si hay red
        sincronizarConApiRest()
    }

    fun cambiarBusqueda(texto: String) {
        busqueda.value = texto
    }

    fun sincronizarConApiRest() {
        viewModelScope.launch {
            if (!monitorConectividad.hayInternetActual()) {
                estadoSync.value = EstadoCarga.Error("Sin conexión a Internet")
                _mensajes.emit("Sin conexión: mostrando datos locales")
                return@launch
            }

            estadoSync.value = EstadoCarga.Cargando
            when (val res = sincronizarRest()) {
                is Resultado.Exito -> {
                    estadoSync.value = EstadoCarga.Exito(res.valor)
                    _mensajes.emit("Sincronizados ${res.valor} eventos desde la API REST")
                }
                is Resultado.Error -> {
                    estadoSync.value = EstadoCarga.Error(res.mensaje)
                    _mensajes.emit("Error API: ${res.mensaje}")
                }
            }
        }
    }
}
