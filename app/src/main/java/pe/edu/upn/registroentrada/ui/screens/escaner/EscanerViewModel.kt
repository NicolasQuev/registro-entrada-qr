package pe.edu.upn.registroentrada.ui.screens.escaner

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
import pe.edu.upn.registroentrada.core.Reloj
import pe.edu.upn.registroentrada.domain.repository.AjustesRepository
import pe.edu.upn.registroentrada.domain.repository.EventoRepository
import pe.edu.upn.registroentrada.domain.usecase.RegistrarAccesoUseCase
import pe.edu.upn.registroentrada.domain.usecase.ValidacionAcceso

data class EscanerUiState(
    val eventoActivoId: Long = -1,
    val eventoNombre: String = "",
    val puerta: String = "P1",
    val operadorId: Long = -1,
    val codigoManual: String = "",
    val validacion: ValidacionAcceso? = null,
    val mensaje: String? = null,
    val procesando: Boolean = false
)

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class EscanerViewModel @Inject constructor(
    private val registrarAcceso: RegistrarAccesoUseCase,
    ajustes: AjustesRepository,
    eventos: EventoRepository,
    private val reloj: Reloj
) : ViewModel() {

    private val _estado = MutableStateFlow(EscanerUiState())
    val estado = _estado.asStateFlow()

    private var ultimoCodigoEscaneado: String? = null
    private var ultimoEscaneoMs: Long = 0L

    init {
        viewModelScope.launch {
            ajustes.ajustes.collect { preferencias ->
                _estado.update {
                    it.copy(
                        eventoActivoId = preferencias.eventoActivoId,
                        puerta = preferencias.puertaActiva,
                        operadorId = preferencias.operadorActivoId
                    )
                }
            }
        }
        viewModelScope.launch {
            ajustes.ajustes.map { it.eventoActivoId }.distinctUntilChanged()
                .flatMapLatest { id -> if (id <= 0) flowOf(null) else eventos.observarEvento(id) }
                .collect { evento ->
                    _estado.update { it.copy(eventoNombre = evento?.nombre.orEmpty()) }
                }
        }
    }

    fun cambiarCodigo(valor: String) {
        _estado.update { it.copy(codigoManual = valor.trim(), mensaje = null) }
    }

    /**
     * Procesa códigos detectados por cámara evitando lecturas duplicadas en ráfaga
     * mientras se valida un ticket o dentro de la ventana de enfriamiento.
     */
    fun procesarCodigoEscaneado(codigo: String) {
        val limpio = codigo.trim()
        if (limpio.isBlank()) return
        val actual = _estado.value
        if (actual.procesando || actual.validacion != null) return

        val ahoraMs = reloj.ahora().toEpochMilli()
        if (limpio == ultimoCodigoEscaneado && (ahoraMs - ultimoEscaneoMs) < COOLDOWN_ESCANEO_MS) {
            return
        }
        ultimoCodigoEscaneado = limpio
        ultimoEscaneoMs = ahoraMs
        cambiarCodigo(limpio)
        validar()
    }

    fun validar() {
        val actual = _estado.value
        if (actual.procesando || actual.codigoManual.isBlank()) return
        if (actual.eventoActivoId <= 0) {
            _estado.update { it.copy(mensaje = "Selecciona un evento activo en Inicio") }
            return
        }
        if (actual.operadorId <= 0) {
            _estado.update { it.copy(mensaje = "No hay operador con sesión iniciada") }
            return
        }
        _estado.update { it.copy(procesando = true, mensaje = null) }
        viewModelScope.launch {
            val validacion = registrarAcceso(
                textoQr = actual.codigoManual,
                eventoActivoId = actual.eventoActivoId,
                puerta = actual.puerta,
                operadorId = actual.operadorId
            )
            _estado.update {
                it.copy(procesando = false, validacion = validacion, codigoManual = "")
            }
        }
    }

    fun limpiar() {
        _estado.update { it.copy(validacion = null, codigoManual = "") }
    }

    private companion object {
        const val COOLDOWN_ESCANEO_MS = 2_000L
    }
}
