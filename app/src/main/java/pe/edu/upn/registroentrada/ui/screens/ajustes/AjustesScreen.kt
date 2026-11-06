package pe.edu.upn.registroentrada.ui.screens.ajustes

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import pe.edu.upn.registroentrada.domain.model.TemaApp
import pe.edu.upn.registroentrada.ui.components.BarraSuperior
import pe.edu.upn.registroentrada.ui.components.TarjetaApp
import pe.edu.upn.registroentrada.ui.components.TituloSenal
import pe.edu.upn.registroentrada.ui.theme.FuenteMono

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AjustesScreen(
    onSesionCerrada: () -> Unit,
    viewModel: AjustesViewModel = hiltViewModel()
) {
    val estado by viewModel.estado.collectAsStateWithLifecycle()

    Scaffold(topBar = { BarraSuperior(titulo = "Ajustes") }) { relleno ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(relleno)
                .padding(20.dp)
                .verticalScroll(rememberScrollState())
        ) {
            TituloSenal("Sesión")
            Spacer(Modifier.height(12.dp))
            TarjetaApp {
                Text(
                    estado.operadorActivoNombre.ifBlank { "Sin sesión" },
                    style = MaterialTheme.typography.titleMedium
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    "PUERTA ${estado.puertaActiva}",
                    fontFamily = FuenteMono,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(Modifier.height(14.dp))
                OutlinedButton(
                    onClick = { viewModel.cerrarSesion(onSesionCerrada) },
                    shape = MaterialTheme.shapes.small,
                    modifier = Modifier.fillMaxWidth().height(48.dp)
                ) { Text("Cerrar sesión", style = MaterialTheme.typography.labelLarge) }
            }

            Spacer(Modifier.height(24.dp))
            TituloSenal("Tema")
            Spacer(Modifier.height(12.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                TemaApp.entries.forEach { tema ->
                    FilterChip(
                        selected = estado.tema == tema,
                        onClick = { viewModel.cambiarTema(tema) },
                        label = { Text(etiquetaTema(tema)) }
                    )
                }
            }

            Spacer(Modifier.height(24.dp))
            TituloSenal("Escaneo")
            Spacer(Modifier.height(4.dp))
            Interruptor(
                titulo = "Confirmación sonora",
                activo = estado.sonidoEscaneo,
                onCambio = viewModel::cambiarSonido
            )
            Interruptor(
                titulo = "Vibración en rechazos",
                activo = estado.vibracionEscaneo,
                onCambio = viewModel::cambiarVibracion
            )
            Spacer(Modifier.height(24.dp))
        }
    }
}

@Composable
private fun Interruptor(titulo: String, activo: Boolean, onCambio: (Boolean) -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(titulo, style = MaterialTheme.typography.bodyLarge)
        Switch(checked = activo, onCheckedChange = onCambio)
    }
}

private fun etiquetaTema(tema: TemaApp): String = when (tema) {
    TemaApp.SISTEMA -> "Sistema"
    TemaApp.CLARO -> "Claro"
    TemaApp.OSCURO -> "Oscuro"
}
