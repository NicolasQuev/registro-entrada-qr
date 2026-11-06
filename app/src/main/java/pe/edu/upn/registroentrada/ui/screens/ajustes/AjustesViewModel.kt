package pe.edu.upn.registroentrada.ui.screens.ajustes

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import pe.edu.upn.registroentrada.domain.model.AjustesUsuario
import pe.edu.upn.registroentrada.domain.model.TemaApp
import pe.edu.upn.registroentrada.domain.repository.AjustesRepository
import pe.edu.upn.registroentrada.domain.usecase.CerrarSesionUseCase
import pe.edu.upn.registroentrada.domain.usecase.GuardarPreferenciasUseCase

@HiltViewModel
class AjustesViewModel @Inject constructor(
    ajustes: AjustesRepository,
    private val guardarPreferencias: GuardarPreferenciasUseCase,
    private val cerrarSesionCaso: CerrarSesionUseCase
) : ViewModel() {

    val estado = ajustes.ajustes.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = AjustesUsuario()
    )

    fun cambiarTema(tema: TemaApp) = viewModelScope.launch {
        guardarPreferencias.tema(tema)
    }

    fun cambiarSonido(activo: Boolean) = viewModelScope.launch {
        guardarPreferencias.sonido(activo)
    }

    fun cambiarVibracion(activa: Boolean) = viewModelScope.launch {
        guardarPreferencias.vibracion(activa)
    }

    fun cerrarSesion(onCerrada: () -> Unit) {
        viewModelScope.launch {
            cerrarSesionCaso()
            onCerrada()
        }
    }
}
