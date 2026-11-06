package pe.edu.upn.registroentrada.ui.screens.inicio

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import pe.edu.upn.registroentrada.core.Resultado
import pe.edu.upn.registroentrada.domain.model.Aforo
import pe.edu.upn.registroentrada.domain.model.Evento
import pe.edu.upn.registroentrada.domain.repository.AccesoRepository
import pe.edu.upn.registroentrada.domain.repository.AjustesRepository
import pe.edu.upn.registroentrada.domain.repository.EventoRepository
import pe.edu.upn.registroentrada.domain.usecase.SeleccionarEventoActivoUseCase

data class InicioUiState(
    val operadorNombre: String = "",
    val puerta: String = "P1",
    val eventoActivoId: Long = -1,
    val evento: Evento? = null,
    val aforo: Aforo = Aforo(0, 0),
    val eventosDisponibles: List<Evento> = emptyList(),
    val mensaje: String? = null
)

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class InicioViewModel @Inject constructor(
    private val eventos: EventoRepository,
    private val accesos: AccesoRepository,
    private val ajustes: AjustesRepository,
    private val seleccionarEventoActivo: SeleccionarEventoActivoUseCase
) : ViewModel() {

    private val _estado = MutableStateFlow(InicioUiState())
    val estado = _estado.asStateFlow()

    init {
        viewModelScope.launch {
            ajustes.ajustes.collect { preferencias ->
                _estado.update {
                    it.copy(
                        operadorNombre = preferencias.operadorActivoNombre,
                        puerta = preferencias.puertaActiva,
                        eventoActivoId = preferencias.eventoActivoId
                    )
                }
            }
        }
        val idActivo = ajustes.ajustes.map { it.eventoActivoId }.distinctUntilChanged()
        viewModelScope.launch {
            idActivo.flatMapLatest { id ->
                if (id <= 0) flowOf(null) else eventos.observarEvento(id)
            }.collect { evento -> _estado.update { it.copy(evento = evento) } }
        }
        viewModelScope.launch {
            idActivo.flatMapLatest { id ->
                if (id <= 0) flowOf(Aforo(0, 0)) else accesos.observarAforo(id)
            }.collect { aforo -> _estado.update { it.copy(aforo = aforo) } }
        }
        viewModelScope.launch {
            eventos.observarEventos().collect { lista ->
                _estado.update { it.copy(eventosDisponibles = lista.filter { e -> e.admiteIngresos }) }
            }
        }
    }

    fun cambiarPuerta(valor: String) {
        _estado.update { it.copy(puerta = valor.take(10)) }
    }

    fun seleccionarEvento(eventoId: Long) {
        viewModelScope.launch {
            when (val resultado = seleccionarEventoActivo(eventoId, _estado.value.puerta)) {
                is Resultado.Exito -> _estado.update { it.copy(mensaje = "Evento seleccionado") }
                is Resultado.Error -> _estado.update { it.copy(mensaje = resultado.mensaje) }
            }
        }
    }

    fun olvidarEvento() {
        viewModelScope.launch {
            ajustes.guardarEventoActivo(-1, _estado.value.puerta)
        }
    }
}
