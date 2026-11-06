package pe.edu.upn.registroentrada.ui.screens.inicio

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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import pe.edu.upn.registroentrada.domain.model.Evento
import pe.edu.upn.registroentrada.ui.components.BarraAforo
import pe.edu.upn.registroentrada.ui.components.BarraSuperior
import pe.edu.upn.registroentrada.ui.components.BotonPrimario
import pe.edu.upn.registroentrada.ui.components.CampoTextoValidado
import pe.edu.upn.registroentrada.ui.components.EstadoVacio
import pe.edu.upn.registroentrada.ui.components.TarjetaApp
import pe.edu.upn.registroentrada.ui.components.TituloSenal
import pe.edu.upn.registroentrada.ui.theme.FuenteMono
import pe.edu.upn.registroentrada.ui.util.Formato

@Composable
fun InicioScreen(
    onEscanear: () -> Unit,
    onHistorial: () -> Unit,
    onAsistentes: () -> Unit,
    onNuevoEvento: () -> Unit,
    onVerEntradas: (Long) -> Unit,
    viewModel: InicioViewModel = hiltViewModel()
) {
    val estado by viewModel.estado.collectAsStateWithLifecycle()
    val evento = estado.evento

    Scaffold(
        topBar = {
            BarraSuperior(
                titulo = "Inicio",
                subtitulo = estado.operadorNombre,
                acciones = {
                    IconButton(onClick = onAsistentes) {
                        Icon(Icons.Filled.Person, contentDescription = "Asistentes")
                    }
                    Text(
                        "PUERTA ${estado.puerta}",
                        fontFamily = FuenteMono,
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(end = 16.dp)
                    )
                }
            )
        }
    ) { relleno ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(relleno)
                .padding(horizontal = 20.dp)
                .verticalScroll(rememberScrollState())
        ) {
            estado.mensaje?.let {
                Spacer(Modifier.height(8.dp))
                Text(it, color = MaterialTheme.colorScheme.error)
            }

            Spacer(Modifier.height(12.dp))
            if (evento != null && estado.eventoActivoId > 0) {
                Text(
                    "Evento activo",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(Modifier.height(8.dp))
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
                }
                Spacer(Modifier.height(24.dp))
                TituloSenal("Aforo en vivo")
                Spacer(Modifier.height(14.dp))
                BarraAforo(estado.aforo)
                Spacer(Modifier.height(28.dp))
                BotonPrimario(texto = "Escanear en puerta", onClick = onEscanear)
                Spacer(Modifier.height(10.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedButton(
                        onClick = { onVerEntradas(evento.id) },
                        shape = MaterialTheme.shapes.small,
                        modifier = Modifier.weight(1f).height(48.dp)
                    ) { Text("Entradas", style = MaterialTheme.typography.labelLarge) }
                    OutlinedButton(
                        onClick = onHistorial,
                        shape = MaterialTheme.shapes.small,
                        modifier = Modifier.weight(1f).height(48.dp)
                    ) { Text("Historial", style = MaterialTheme.typography.labelLarge) }
                }
                Spacer(Modifier.height(10.dp))
                OutlinedButton(
                    onClick = viewModel::olvidarEvento,
                    shape = MaterialTheme.shapes.small,
                    modifier = Modifier.fillMaxWidth().height(48.dp)
                ) { Text("Cambiar de evento", style = MaterialTheme.typography.labelLarge) }
            } else if (estado.eventosDisponibles.isEmpty()) {
                TituloSenal("Empieza aquí")
                Spacer(Modifier.height(4.dp))
                EstadoVacio(
                    mensaje = "Todavía no hay eventos.\nCrea el primero para poder registrar ingresos.",
                    textoBoton = "Crear evento",
                    onBoton = onNuevoEvento
                )
            } else {
                TituloSenal("Elige el evento")
                Spacer(Modifier.height(12.dp))
                CampoTextoValidado(
                    valor = estado.puerta,
                    onValorChange = viewModel::cambiarPuerta,
                    etiqueta = "Identificador de puerta",
                    soporte = "Ej.: P1, VIP, puerta lateral"
                )
                Spacer(Modifier.height(16.dp))
                estado.eventosDisponibles.forEach { disponible ->
                    TarjetaEventoDisponible(
                        evento = disponible,
                        onAtender = { viewModel.seleccionarEvento(disponible.id) }
                    )
                    Spacer(Modifier.height(10.dp))
                }
            }
            Spacer(Modifier.height(24.dp))
        }
    }
}

@Composable
private fun TarjetaEventoDisponible(evento: Evento, onAtender: () -> Unit) {
    TarjetaApp {
        Text(evento.nombre, style = MaterialTheme.typography.titleLarge)
        Spacer(Modifier.height(4.dp))
        Text(
            evento.lugar,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(Modifier.height(8.dp))
        Text(
            "${Formato.fecha(evento.fechaInicio)}  ·  aforo ${evento.aforoMaximo}",
            fontFamily = FuenteMono,
            style = MaterialTheme.typography.bodySmall
        )
        Spacer(Modifier.height(14.dp))
        BotonPrimario(texto = "Atender este evento", onClick = onAtender)
    }
}
