package pe.edu.upn.registroentrada.ui.screens.entradas

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import pe.edu.upn.registroentrada.domain.model.EntradaDetalle
import pe.edu.upn.registroentrada.domain.repository.EntradaRepository

data class EntradasUiState(
    val entradas: List<EntradaDetalle> = emptyList()
)

@HiltViewModel
class EntradasViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    entradas: EntradaRepository
) : ViewModel() {

    private val eventoId: Long = savedStateHandle.get<Long>("eventoId") ?: -1L

    val estado = entradas.observarEntradasDeEvento(eventoId)
        .map { EntradasUiState(entradas = it) }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = EntradasUiState()
        )
}
