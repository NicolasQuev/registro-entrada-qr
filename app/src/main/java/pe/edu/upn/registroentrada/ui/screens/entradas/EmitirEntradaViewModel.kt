package pe.edu.upn.registroentrada.ui.screens.entradas

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import pe.edu.upn.registroentrada.core.Resultado
import pe.edu.upn.registroentrada.domain.model.TipoEntrada
import pe.edu.upn.registroentrada.domain.repository.EventoRepository
import pe.edu.upn.registroentrada.domain.usecase.EmitirEntradaUseCase

data class EmitirEntradaUiState(
    val eventoId: Long = -1,
    val tipos: List<TipoEntrada> = emptyList(),
    val tipoSeleccionado: TipoEntrada? = null,
    val cantidad: String = "10",
    val mensaje: String? = null,
    val emitiendo: Boolean = false
)

@HiltViewModel
class EmitirEntradaViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    eventosRepository: EventoRepository,
    private val emitirEntrada: EmitirEntradaUseCase
) : ViewModel() {

    private val eventoId: Long = savedStateHandle.get<Long>("eventoId") ?: -1L

    private val seleccion = MutableStateFlow(EmitirEntradaUiState(eventoId = eventoId))

    val estado = combine(
        eventosRepository.observarTiposDeEntrada(eventoId),
        seleccion
    ) { tipos, actual ->
        actual.copy(tipos = tipos)
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = EmitirEntradaUiState(eventoId = eventoId)
    )

    fun seleccionarTipo(tipo: TipoEntrada) {
        seleccion.update { it.copy(tipoSeleccionado = tipo, mensaje = null) }
    }

    fun cambiarCantidad(valor: String) {
        seleccion.update { it.copy(cantidad = valor.filter { c -> c.isDigit() }.take(4), mensaje = null) }
    }

    fun emitir(onEmitidas: (Long) -> Unit) {
        val actual = seleccion.value
        val tipo = actual.tipoSeleccionado ?: return
        val cantidad = actual.cantidad.toIntOrNull() ?: 0
        if (cantidad < 1) {
            seleccion.update { it.copy(mensaje = "Indica cuántas entradas vas a emitir") }
            return
        }
        seleccion.update { it.copy(emitiendo = true, mensaje = null) }
        viewModelScope.launch {
            when (val resultado = emitirEntrada(eventoId, tipo.id, cantidad)) {
                is Resultado.Exito -> {
                    seleccion.update { it.copy(emitiendo = false) }
                    onEmitidas(eventoId)
                }
                is Resultado.Error -> seleccion.update {
                    it.copy(emitiendo = false, mensaje = resultado.mensaje)
                }
            }
        }
    }
}
