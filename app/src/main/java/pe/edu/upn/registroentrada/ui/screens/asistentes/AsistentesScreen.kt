package pe.edu.upn.registroentrada.ui.screens.asistentes

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import pe.edu.upn.registroentrada.domain.model.Asistente
import pe.edu.upn.registroentrada.ui.components.BarraSuperior
import pe.edu.upn.registroentrada.ui.components.EstadoVacio
import pe.edu.upn.registroentrada.ui.components.TarjetaApp
import pe.edu.upn.registroentrada.ui.theme.FuenteMono

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AsistentesScreen(
    onVolver: () -> Unit,
    onNuevoAsistente: () -> Unit,
    viewModel: AsistentesViewModel = hiltViewModel()
) {
    val estado by viewModel.estado.collectAsStateWithLifecycle()

    Scaffold(
        topBar = { BarraSuperior(titulo = "Asistentes", onVolver = onVolver) },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onNuevoAsistente,
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary
            ) {
                Icon(Icons.Filled.Add, contentDescription = "Nuevo asistente")
            }
        }
    ) { relleno ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(relleno)
                .padding(horizontal = 20.dp)
        ) {
            OutlinedTextField(
                value = estado.busqueda,
                onValueChange = viewModel::cambiarBusqueda,
                label = { Text("Buscar por documento o apellido") },
                singleLine = true,
                shape = MaterialTheme.shapes.small,
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(16.dp))
            when {
                estado.asistentes.isEmpty() && estado.busqueda.isNotBlank() ->
                    EstadoVacio(
                        mensaje = "No se encontraron asistentes",
                        textoBoton = "Limpiar búsqueda",
                        onBoton = { viewModel.cambiarBusqueda("") }
                    )
                estado.asistentes.isEmpty() ->
                    EstadoVacio(
                        mensaje = "Todavía no hay asistentes",
                        textoBoton = "Registrar el primero",
                        onBoton = onNuevoAsistente
                    )
                else -> LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    items(estado.asistentes, key = { it.id }) { asistente ->
                        TarjetaAsistente(asistente)
                    }
                }
            }
        }
    }
}

@Composable
private fun TarjetaAsistente(asistente: Asistente) {
    TarjetaApp {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(42.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.secondaryContainer),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = iniciales(asistente.nombreCompleto),
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.onSecondaryContainer
                )
            }
            Spacer(Modifier.size(14.dp))
            Column {
                Text(asistente.nombreCompleto, style = MaterialTheme.typography.titleMedium)
                Text(
                    "DNI ${asistente.documento}",
                    fontFamily = FuenteMono,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

private fun iniciales(nombre: String): String =
    nombre.trim().split(" ").take(2).mapNotNull { it.firstOrNull()?.uppercase() }
        .joinToString("").ifBlank { "?" }
