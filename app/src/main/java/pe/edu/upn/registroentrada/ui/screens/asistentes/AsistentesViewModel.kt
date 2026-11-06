package pe.edu.upn.registroentrada.ui.screens.asistentes

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import pe.edu.upn.registroentrada.domain.model.Asistente
import pe.edu.upn.registroentrada.domain.repository.AsistenteRepository

data class AsistentesUiState(
    val asistentes: List<Asistente> = emptyList(),
    val busqueda: String = ""
)

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class AsistentesViewModel @Inject constructor(
    private val asistentes: AsistenteRepository
) : ViewModel() {

    private val busqueda = MutableStateFlow("")

    private val filtrados = busqueda.flatMapLatest { texto ->
        if (texto.isBlank()) asistentes.observarAsistentes() else asistentes.buscar(texto)
    }

    val estado = combine(busqueda, filtrados) { texto, lista ->
        AsistentesUiState(asistentes = lista, busqueda = texto)
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = AsistentesUiState()
    )

    fun cambiarBusqueda(texto: String) {
        busqueda.value = texto
    }
}
