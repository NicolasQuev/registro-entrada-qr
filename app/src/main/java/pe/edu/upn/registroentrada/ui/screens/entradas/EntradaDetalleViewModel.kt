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
import kotlinx.coroutines.launch
import pe.edu.upn.registroentrada.core.FirmadorQr
import pe.edu.upn.registroentrada.core.Resultado
import pe.edu.upn.registroentrada.domain.model.EntradaDetalle
import pe.edu.upn.registroentrada.domain.repository.EntradaRepository
import pe.edu.upn.registroentrada.domain.usecase.AnularEntradaUseCase

data class EntradaDetalleUiState(
    val detalle: EntradaDetalle? = null,
    val qrTexto: String? = null,
    val mensaje: String? = null
)

@HiltViewModel
class EntradaDetalleViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val entradas: EntradaRepository,
    private val anularEntrada: AnularEntradaUseCase,
    private val firmador: FirmadorQr
) : ViewModel() {

    private val entradaId: Long = savedStateHandle.get<Long>("entradaId") ?: -1L
    private val mensaje = MutableStateFlow<String?>(null)

    val estado = combine(
        entradas.observarEntrada(entradaId),
        mensaje
    ) { detalle, aviso ->
        EntradaDetalleUiState(
            detalle = detalle,
            qrTexto = detalle?.let { firmador.empaquetar(it.entrada.codigo) },
            mensaje = aviso
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = EntradaDetalleUiState()
    )

    fun anular() {
        viewModelScope.launch {
            when (val resultado = anularEntrada(entradaId)) {
                is Resultado.Exito -> mensaje.value = "Entrada anulada"
                is Resultado.Error -> mensaje.value = resultado.mensaje
            }
        }
    }
}
