package pe.edu.upn.registroentrada.ui.screens.asistentes

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import pe.edu.upn.registroentrada.core.Resultado
import pe.edu.upn.registroentrada.core.ValidadorDocumento
import pe.edu.upn.registroentrada.domain.usecase.RegistrarAsistenteUseCase

data class AsistenteFormUiState(
    val documento: String = "",
    val nombres: String = "",
    val apellidos: String = "",
    val correo: String = "",
    val telefono: String = "",
    val errorDocumento: String? = null,
    val errorNombres: String? = null,
    val errorApellidos: String? = null,
    val errorCorreo: String? = null,
    val errorTelefono: String? = null,
    val mensaje: String? = null,
    val guardando: Boolean = false
)

@HiltViewModel
class AsistenteFormViewModel @Inject constructor(
    private val registrarAsistente: RegistrarAsistenteUseCase
) : ViewModel() {

    private val _estado = MutableStateFlow(AsistenteFormUiState())
    val estado = _estado.asStateFlow()

    fun cambiarDocumento(v: String) = _estado.update {
        it.copy(documento = v.filter { c -> c.isDigit() }.take(8), errorDocumento = null)
    }
    fun cambiarNombres(v: String) = _estado.update { it.copy(nombres = v, errorNombres = null) }
    fun cambiarApellidos(v: String) = _estado.update { it.copy(apellidos = v, errorApellidos = null) }
    fun cambiarCorreo(v: String) = _estado.update { it.copy(correo = v, errorCorreo = null) }
    fun cambiarTelefono(v: String) = _estado.update {
        it.copy(telefono = v.filter { c -> c.isDigit() }.take(15), errorTelefono = null)
    }

    fun guardar(onCreado: () -> Unit) {
        val actual = _estado.value
        _estado.update {
            it.copy(
                errorDocumento = ValidadorDocumento.errorDocumento(actual.documento),
                errorNombres = if (actual.nombres.isBlank()) "Los nombres son obligatorios" else null,
                errorApellidos = if (actual.apellidos.isBlank()) "Los apellidos son obligatorios" else null,
                errorCorreo = ValidadorDocumento.errorCorreo(actual.correo),
                errorTelefono = ValidadorDocumento.errorTelefono(actual.telefono)
            )
        }
        val conError = _estado.value.let {
            it.errorDocumento != null || it.errorNombres != null || it.errorApellidos != null ||
                it.errorCorreo != null || it.errorTelefono != null
        }
        if (conError) return

        _estado.update { it.copy(guardando = true, mensaje = null) }
        viewModelScope.launch {
            val resultado = registrarAsistente(
                documento = actual.documento,
                nombres = actual.nombres,
                apellidos = actual.apellidos,
                correo = actual.correo,
                telefono = actual.telefono
            )
            when (resultado) {
                is Resultado.Exito -> {
                    _estado.update { it.copy(guardando = false) }
                    onCreado()
                }
                is Resultado.Error -> _estado.update {
                    it.copy(guardando = false, mensaje = resultado.mensaje)
                }
            }
        }
    }
}
