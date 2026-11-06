package pe.edu.upn.registroentrada

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.material3.Surface
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dagger.hilt.android.AndroidEntryPoint
import pe.edu.upn.registroentrada.ui.MainViewModel
import pe.edu.upn.registroentrada.ui.navigation.GrafoNavegacion
import pe.edu.upn.registroentrada.ui.theme.RegistroEntradaTheme

/**
 * Única Activity de la aplicación (7.7 del diseño): aloja el NavHost de
 * Compose y aplica el tema elegido por el usuario.
 */
@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val viewModel: MainViewModel = hiltViewModel()
            val ajustes by viewModel.ajustes.collectAsStateWithLifecycle()
            RegistroEntradaTheme(tema = ajustes.tema) {
                Surface {
                    GrafoNavegacion(haySesion = ajustes.haySesion)
                }
            }
        }
    }
}
