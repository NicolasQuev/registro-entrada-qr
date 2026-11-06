package pe.edu.upn.registroentrada.ui.screens.login

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import pe.edu.upn.registroentrada.core.Resultado
import pe.edu.upn.registroentrada.domain.model.Operador
import pe.edu.upn.registroentrada.domain.repository.OperadorRepository
import pe.edu.upn.registroentrada.domain.usecase.IniciarSesionUseCase

data class LoginUiState(
    val operadores: List<Operador> = emptyList(),
    val operadorSeleccionado: Operador? = null,
    val pin: String = "",
    val error: String? = null,
    val cargando: Boolean = false,
    val sesionIniciada: Boolean = false
)

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val operadores: OperadorRepository,
    private val iniciarSesion: IniciarSesionUseCase
) : ViewModel() {

    private val _estado = MutableStateFlow(LoginUiState())
    val estado = _estado.asStateFlow()

    init {
        viewModelScope.launch {
            _estado.update { it.copy(cargando = true) }
            operadores.observarOperadoresActivos().collect { lista ->
                _estado.update { actual ->
                    val seleccion = actual.operadorSeleccionado
                        ?.let { elegido -> lista.firstOrNull { it.id == elegido.id } }
                    actual.copy(operadores = lista, operadorSeleccionado = seleccion, cargando = false)
                }
            }
        }
    }

    fun seleccionar(operador: Operador) {
        _estado.update { it.copy(operadorSeleccionado = operador, pin = "", error = null) }
    }

    fun cambiarPin(pin: String) {
        _estado.update { it.copy(pin = pin.filter { c -> c.isDigit() }.take(6), error = null) }
    }

    fun iniciarSesion() {
        val operador = _estado.value.operadorSeleccionado ?: return
        if (_estado.value.pin.length < 4) {
            _estado.update { it.copy(error = "El PIN debe tener al menos 4 dígitos") }
            return
        }
        _estado.update { it.copy(cargando = true, error = null) }
        viewModelScope.launch {
            when (val resultado = iniciarSesion(operador.usuario, _estado.value.pin)) {
                is Resultado.Exito -> _estado.update { it.copy(cargando = false, sesionIniciada = true) }
                is Resultado.Error -> _estado.update {
                    it.copy(cargando = false, error = resultado.mensaje, pin = "")
                }
            }
        }
    }
}
