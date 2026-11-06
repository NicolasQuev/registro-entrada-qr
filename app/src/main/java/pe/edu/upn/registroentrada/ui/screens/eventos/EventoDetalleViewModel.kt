package pe.edu.upn.registroentrada.ui.screens.eventos

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
import pe.edu.upn.registroentrada.core.Resultado
import pe.edu.upn.registroentrada.core.network.EstadoCarga
import pe.edu.upn.registroentrada.domain.model.Evento
import pe.edu.upn.registroentrada.domain.model.TipoEntrada
import pe.edu.upn.registroentrada.domain.repository.EventoRepository
import pe.edu.upn.registroentrada.domain.usecase.EliminarEventoUseCase
import pe.edu.upn.registroentrada.domain.usecase.EliminarTipoEntradaUseCase
import pe.edu.upn.registroentrada.domain.usecase.GuardarTipoEntradaUseCase

data class EventoDetalleUiState(
    val evento: Evento? = null,
    val tipos: List<TipoEntrada> = emptyList(),
    val estadoSyncNube: EstadoCarga<String> = EstadoCarga.Inactivo,
    val mensaje: String? = null
)

@HiltViewModel
class EventoDetalleViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val eventos: EventoRepository,
    private val guardarTipo: GuardarTipoEntradaUseCase,
    private val eliminarEventoCaso: EliminarEventoUseCase,
    private val eliminarTipoCaso: EliminarTipoEntradaUseCase
) : ViewModel() {

    private val eventoId: Long = savedStateHandle.get<Long>("eventoId") ?: -1L

    private val mensaje = MutableStateFlow<String?>(null)
    private val estadoSyncNube = MutableStateFlow<EstadoCarga<String>>(EstadoCarga.Inactivo)

    val estado = combine(
        eventos.observarEvento(eventoId),
        eventos.observarTiposDeEntrada(eventoId),
        estadoSyncNube,
        mensaje
    ) { evento, tipos, sync, aviso ->
        EventoDetalleUiState(
            evento = evento,
            tipos = tipos,
            estadoSyncNube = sync,
            mensaje = aviso
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = EventoDetalleUiState()
    )

    fun sincronizarConNube() {
        viewModelScope.launch {
            estadoSyncNube.value = EstadoCarga.Cargando
            val res = eventos.sincronizarEventoEnNube(eventoId)
            res.fold(
                onSuccess = { remoteId ->
                    estadoSyncNube.value = EstadoCarga.Exito(remoteId)
                    mensaje.value = "Sincronizado en la nube (ID: $remoteId)"
                },
                onFailure = { err ->
                    estadoSyncNube.value = EstadoCarga.Error(err.message ?: "Error al subir a la API REST")
                    mensaje.value = "Error al sincronizar: ${err.message}"
                }
            )
        }
    }

    fun agregarTipo(nombre: String, precio: Double, cupo: Int) {
        viewModelScope.launch {
            when (val resultado = guardarTipo(0, eventoId, nombre, precio, cupo, colorParaNombre())) {
                is Resultado.Exito -> mensaje.value = null
                is Resultado.Error -> mensaje.value = resultado.mensaje
            }
        }
    }

    private var indiceColor = 0
    private fun colorParaNombre(): String {
        val paleta = listOf("#2E74B5", "#1B7F4B", "#9A6700", "#B3261E", "#6A3AB2")
        return paleta[indiceColor++ % paleta.size]
    }

    fun eliminarTipo(tipoId: Long) {
        viewModelScope.launch {
            when (val resultado = eliminarTipoCaso(tipoId)) {
                is Resultado.Exito -> mensaje.value = null
                is Resultado.Error -> mensaje.value = resultado.mensaje
            }
        }
    }

    fun eliminarEvento(onEliminado: () -> Unit) {
        viewModelScope.launch {
            when (val resultado = eliminarEventoCaso(eventoId)) {
                is Resultado.Exito -> onEliminado()
                is Resultado.Error -> mensaje.value = resultado.mensaje
            }
        }
    }
}
