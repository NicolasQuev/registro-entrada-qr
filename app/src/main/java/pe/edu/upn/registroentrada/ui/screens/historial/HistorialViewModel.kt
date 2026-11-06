package pe.edu.upn.registroentrada.ui.screens.historial

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import pe.edu.upn.registroentrada.domain.model.FiltroRegistros
import pe.edu.upn.registroentrada.domain.model.RegistroAccesoDetalle
import pe.edu.upn.registroentrada.domain.model.ResultadoAcceso
import pe.edu.upn.registroentrada.domain.repository.AccesoRepository
import pe.edu.upn.registroentrada.domain.repository.AjustesRepository

data class HistorialUiState(
    val registros: List<RegistroAccesoDetalle> = emptyList(),
    val filtro: ResultadoAcceso? = null
)

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class HistorialViewModel @Inject constructor(
    accesos: AccesoRepository,
    ajustes: AjustesRepository
) : ViewModel() {

    private val filtro = MutableStateFlow<ResultadoAcceso?>(null)

    val estado = combine(
        ajustes.ajustes.map { it.eventoActivoId }.distinctUntilChanged(),
        filtro
    ) { eventoId, resultado -> eventoId to resultado }
        .flatMapLatest { (eventoId, resultado) ->
            if (eventoId <= 0) {
                flowOf(emptyList())
            } else {
                accesos.observarRegistros(eventoId, FiltroRegistros(resultado = resultado))
            }
        }
        .map { registros -> HistorialUiState(registros = registros, filtro = filtro.value) }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = HistorialUiState()
        )

    fun filtrar(resultado: ResultadoAcceso?) {
        filtro.value = resultado
    }
}
