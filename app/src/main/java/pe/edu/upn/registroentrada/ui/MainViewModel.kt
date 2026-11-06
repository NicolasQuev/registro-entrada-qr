package pe.edu.upn.registroentrada.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import pe.edu.upn.registroentrada.domain.model.AjustesUsuario
import pe.edu.upn.registroentrada.domain.repository.AjustesRepository

/**
 * ViewModel de nivel de aplicación: expone el tema y la sesión activa para
 * que la Activity aplique el tema de Material 3 (HU-23) y decida si mostrar
 * el login.
 */
@HiltViewModel
class MainViewModel @Inject constructor(
    ajustes: AjustesRepository
) : ViewModel() {

    val ajustes = ajustes.ajustes.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = AjustesUsuario()
    )
}
