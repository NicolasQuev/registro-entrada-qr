package pe.edu.upn.registroentrada.ui.screens.eventos

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CloudUpload
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Map
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import pe.edu.upn.registroentrada.core.network.EstadoCarga
import pe.edu.upn.registroentrada.domain.model.TipoEntrada
import pe.edu.upn.registroentrada.ui.components.BarraSuperior
import pe.edu.upn.registroentrada.ui.components.CampoTextoValidado
import pe.edu.upn.registroentrada.ui.components.DialogoConfirmacion
import pe.edu.upn.registroentrada.ui.components.TarjetaApp
import pe.edu.upn.registroentrada.ui.components.TituloSenal
import pe.edu.upn.registroentrada.ui.theme.FuenteMono
import pe.edu.upn.registroentrada.ui.util.Formato

@Composable
fun EventoDetalleScreen(
    onVolver: () -> Unit,
    onEmitirEntrada: (Long) -> Unit,
    onVerEntradas: (Long) -> Unit,
    onVerMapa: (Long) -> Unit,
    viewModel: EventoDetalleViewModel = hiltViewModel()
) {
    val estado by viewModel.estado.collectAsStateWithLifecycle()
    var mostrarDialogoTipo by remember { mutableStateOf(false) }
    var confirmarEliminarEvento by remember { mutableStateOf(false) }
    var tipoAEliminar by remember { mutableStateOf<TipoEntrada?>(null) }

    Scaffold(
        topBar = {
            BarraSuperior(
                titulo = "Evento",
                onVolver = onVolver,
                acciones = {
                    IconButton(onClick = { confirmarEliminarEvento = true }) {
                        Icon(
                            Icons.Filled.Delete,
                            contentDescription = "Eliminar evento",
                            tint = MaterialTheme.colorScheme.error
                        )
                    }
                }
            )
        }
    ) { relleno ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(relleno)
                .padding(20.dp)
                .verticalScroll(rememberScrollState())
        ) {
            val evento = estado.evento
            if (evento == null) {
                Text("Cargando evento…")
                return@Column
            }

            TarjetaApp {
                Text(evento.nombre, style = MaterialTheme.typography.headlineSmall)
                Spacer(Modifier.height(4.dp))
                Text(
                    evento.lugar,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(Modifier.height(10.dp))
                Text(
                    "${Formato.fecha(evento.fechaInicio)}  ·  ${Formato.hora(evento.fechaInicio)}–${Formato.hora(evento.fechaFin)}",
                    fontFamily = FuenteMono,
                    style = MaterialTheme.typography.bodySmall
                )
                Text(
                    "Aforo máximo ${evento.aforoMaximo}",
                    fontFamily = FuenteMono,
                    style = MaterialTheme.typography.bodySmall
                )
                if (evento.remoteId != null) {
                    Spacer(Modifier.height(6.dp))
                    Text(
                        "Cloud ID: ${evento.remoteId}",
                        fontFamily = FuenteMono,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }

            estado.mensaje?.let {
                Spacer(Modifier.height(10.dp))
                Text(it, color = MaterialTheme.colorScheme.primary)
            }

            Spacer(Modifier.height(16.dp))
            // Botones de API REST y Google Maps
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedButton(
                    onClick = { onVerMapa(evento.id) },
                    modifier = Modifier.weight(1f).height(48.dp)
                ) {
                    Icon(Icons.Filled.Map, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.size(6.dp))
                    Text("Ver en Mapa")
                }

                OutlinedButton(
                    onClick = viewModel::sincronizarConNube,
                    enabled = estado.estadoSyncNube !is EstadoCarga.Cargando,
                    modifier = Modifier.weight(1f).height(48.dp)
                ) {
                    if (estado.estadoSyncNube is EstadoCarga.Cargando) {
                        CircularProgressIndicator(modifier = Modifier.size(18.dp), strokeWidth = 2.dp)
                    } else {
                        Icon(Icons.Filled.CloudUpload, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.size(6.dp))
                        Text(if (evento.remoteId != null) "Actualizar Nube" else "Subir a Nube")
                    }
                }
            }

            Spacer(Modifier.height(24.dp))
            TituloSenal("Tipos de entrada")
            Spacer(Modifier.height(12.dp))

            if (estado.tipos.isEmpty()) {
                Text(
                    "Todavía no definiste tipos de entrada",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            } else {
                estado.tipos.forEach { tipo ->
                    TarjetaTipo(tipo, onEliminar = { tipoAEliminar = tipo })
                    Spacer(Modifier.height(8.dp))
                }
            }

            Spacer(Modifier.height(4.dp))
            OutlinedButton(
                onClick = { mostrarDialogoTipo = true },
                shape = MaterialTheme.shapes.small,
                modifier = Modifier.fillMaxWidth().height(48.dp)
            ) { Text("Agregar tipo de entrada", style = MaterialTheme.typography.labelLarge) }

            Spacer(Modifier.height(24.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                Button(
                    onClick = { onEmitirEntrada(evento.id) },
                    shape = MaterialTheme.shapes.small,
                    modifier = Modifier.weight(1f).height(52.dp)
                ) { Text("Emitir entrada", style = MaterialTheme.typography.labelLarge) }
                OutlinedButton(
                    onClick = { onVerEntradas(evento.id) },
                    shape = MaterialTheme.shapes.small,
                    modifier = Modifier.weight(1f).height(52.dp)
                ) { Text("Ver entradas", style = MaterialTheme.typography.labelLarge) }
            }
            Spacer(Modifier.height(24.dp))
        }
    }

    if (mostrarDialogoTipo) {
        DialogoTipoEntrada(
            onCancelar = { mostrarDialogoTipo = false },
            onGuardar = { nombre, precio, cupo ->
                viewModel.agregarTipo(nombre, precio, cupo)
                mostrarDialogoTipo = false
            }
        )
    }

    if (confirmarEliminarEvento) {
        DialogoConfirmacion(
            titulo = "Eliminar evento",
            mensaje = "Se eliminará el evento local y remotamente de la nube (API REST) junto con sus entradas asociadas.",
            textoConfirmar = "Eliminar",
            onConfirmar = {
                confirmarEliminarEvento = false
                viewModel.eliminarEvento(onVolver)
            },
            onCancelar = { confirmarEliminarEvento = false }
        )
    }

    tipoAEliminar?.let { tipo ->
        DialogoConfirmacion(
            titulo = "Eliminar tipo de entrada",
            mensaje = "¿Seguro que quieres eliminar \"${tipo.nombre}\"?",
            textoConfirmar = "Eliminar",
            onConfirmar = {
                viewModel.eliminarTipo(tipo.id)
                tipoAEliminar = null
            },
            onCancelar = { tipoAEliminar = null }
        )
    }
}

@Composable
private fun TarjetaTipo(tipo: TipoEntrada, onEliminar: () -> Unit) {
    TarjetaApp(colorBanda = colorDesdeHex(tipo.color)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(Modifier.weight(1f)) {
                Text(tipo.nombre, style = MaterialTheme.typography.titleMedium)
                Text(
                    "Cupo ${tipo.cupo}",
                    fontFamily = FuenteMono,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Text(
                Formato.moneda(tipo.precio),
                fontFamily = FuenteMono,
                style = MaterialTheme.typography.titleSmall
            )
            IconButton(onClick = onEliminar) {
                Icon(
                    Icons.Filled.Delete,
                    contentDescription = "Eliminar tipo ${tipo.nombre}",
                    tint = MaterialTheme.colorScheme.error
                )
            }
        }
    }
}

@Composable
private fun DialogoTipoEntrada(
    onCancelar: () -> Unit,
    onGuardar: (String, Double, Int) -> Unit
) {
    var nombre by remember { mutableStateOf("") }
    var precio by remember { mutableStateOf("") }
    var cupo by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onCancelar,
        shape = MaterialTheme.shapes.large,
        title = { Text("Nuevo tipo de entrada", style = MaterialTheme.typography.titleLarge) },
        text = {
            Column {
                CampoTextoValidado(valor = nombre, onValorChange = { nombre = it }, etiqueta = "Nombre")
                Spacer(Modifier.height(8.dp))
                CampoTextoValidado(
                    valor = precio,
                    onValorChange = { precio = it.filter { c -> c.isDigit() || c == '.' } },
                    etiqueta = "Precio (S/)"
                )
                Spacer(Modifier.height(8.dp))
                CampoTextoValidado(
                    valor = cupo,
                    onValorChange = { cupo = it.filter { c -> c.isDigit() } },
                    etiqueta = "Cupo",
                    soloNumeros = true
                )
            }
        },
        confirmButton = {
            TextButton(onClick = {
                onGuardar(nombre, precio.toDoubleOrNull() ?: 0.0, cupo.toIntOrNull() ?: 0)
            }) { Text("Guardar", style = MaterialTheme.typography.labelLarge) }
        },
        dismissButton = {
            TextButton(onClick = onCancelar) { Text("Cancelar", style = MaterialTheme.typography.labelLarge) }
        }
    )
}

private fun colorDesdeHex(hex: String): Color = runCatching {
    Color(("FF" + hex.removePrefix("#")).toLong(16))
}.getOrDefault(Color(0xFF2E74B5))
