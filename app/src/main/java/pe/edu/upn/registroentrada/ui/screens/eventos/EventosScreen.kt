package pe.edu.upn.registroentrada.ui.screens.eventos

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.CloudOff
import androidx.compose.material.icons.filled.CloudSync
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.WifiOff
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.coroutines.flow.collectLatest
import pe.edu.upn.registroentrada.core.network.EstadoCarga
import pe.edu.upn.registroentrada.domain.model.EstadoEvento
import pe.edu.upn.registroentrada.domain.model.Evento
import pe.edu.upn.registroentrada.ui.components.BarraSuperior
import pe.edu.upn.registroentrada.ui.components.ChipEstado
import pe.edu.upn.registroentrada.ui.components.EstadoVacio
import pe.edu.upn.registroentrada.ui.components.TarjetaApp
import pe.edu.upn.registroentrada.ui.theme.FuenteMono
import pe.edu.upn.registroentrada.ui.theme.coloresSenal
import pe.edu.upn.registroentrada.ui.util.Formato

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EventosScreen(
    onNuevoEvento: () -> Unit,
    onEvento: (Long) -> Unit,
    viewModel: EventosViewModel = hiltViewModel()
) {
    val estado by viewModel.estado.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(Unit) {
        viewModel.mensajes.collectLatest { msg ->
            snackbarHostState.showSnackbar(msg)
        }
    }

    Scaffold(
        topBar = {
            BarraSuperior(
                titulo = "Eventos",
                acciones = {
                    if (estado.estadoSincronizacion is EstadoCarga.Cargando) {
                        CircularProgressIndicator(
                            modifier = Modifier
                                .padding(end = 12.dp)
                                .size(24.dp),
                            strokeWidth = 2.dp,
                            color = MaterialTheme.colorScheme.primary
                        )
                    } else {
                        IconButton(onClick = viewModel::sincronizarConApiRest) {
                            Icon(
                                imageVector = Icons.Filled.Refresh,
                                contentDescription = "Sincronizar API REST"
                            )
                        }
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onNuevoEvento,
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary
            ) {
                Icon(Icons.Filled.Add, contentDescription = "Nuevo evento")
            }
        }
    ) { relleno ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(relleno)
        ) {
            // Banner de conectividad
            AnimatedVisibility(visible = !estado.hayConexion) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(MaterialTheme.colorScheme.errorContainer)
                        .padding(horizontal = 16.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Filled.WifiOff,
                        contentDescription = "Sin conexión",
                        tint = MaterialTheme.colorScheme.onErrorContainer,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(
                        "Sin conexión a Internet — Modo local activo",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onErrorContainer
                    )
                }
            }

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 20.dp, vertical = 12.dp)
            ) {
                OutlinedTextField(
                    value = estado.busqueda,
                    onValueChange = viewModel::cambiarBusqueda,
                    label = { Text("Buscar por nombre") },
                    singleLine = true,
                    shape = MaterialTheme.shapes.small,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(Modifier.height(16.dp))

                when {
                    estado.eventos.isEmpty() && estado.estadoSincronizacion is EstadoCarga.Cargando -> {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                CircularProgressIndicator()
                                Spacer(Modifier.height(12.dp))
                                Text("Cargando eventos desde API REST...")
                            }
                        }
                    }
                    estado.eventos.isEmpty() && estado.busqueda.isNotBlank() ->
                        EstadoVacio(
                            mensaje = "No se encontraron eventos",
                            textoBoton = "Limpiar búsqueda",
                            onBoton = { viewModel.cambiarBusqueda("") }
                        )
                    estado.eventos.isEmpty() ->
                        EstadoVacio(
                            mensaje = "Todavía no hay eventos",
                            textoBoton = "Crear o sincronizar",
                            onBoton = viewModel::sincronizarConApiRest
                        )
                    else -> LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        items(estado.eventos, key = { it.id }) { evento ->
                            TarjetaEvento(evento = evento, onClick = { onEvento(evento.id) })
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun TarjetaEvento(evento: Evento, onClick: () -> Unit) {
    val senal = coloresSenal()
    val (etiqueta, color) = when (evento.estado) {
        EstadoEvento.CANCELADO -> "Cancelado" to senal.rechazo
        EstadoEvento.FINALIZADO -> "Finalizado" to senal.atencion
        EstadoEvento.EN_CURSO -> "En curso" to senal.acceso
        EstadoEvento.PROGRAMADO -> "Programado" to senal.acceso
        EstadoEvento.BORRADOR -> "Borrador" to senal.atencion
    }
    TarjetaApp(
        modifier = Modifier.clickable(onClick = onClick),
        colorBanda = color
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = evento.nombre,
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier
                    .weight(1f)
                    .padding(end = 8.dp)
            )
            Row(verticalAlignment = Alignment.CenterVertically) {
                if (evento.remoteId != null) {
                    Icon(
                        Icons.Filled.CloudDone,
                        contentDescription = "Sincronizado en la nube",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier
                            .padding(end = 6.dp)
                            .size(16.dp)
                    )
                }
                ChipEstado(texto = etiqueta, color = color)
            }
        }
        Spacer(Modifier.height(4.dp))
        Text(
            evento.lugar,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(Modifier.height(10.dp))
        Text(
            "${Formato.fecha(evento.fechaInicio)}  ·  aforo ${evento.aforoMaximo}",
            fontFamily = FuenteMono,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}
