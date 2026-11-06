package pe.edu.upn.registroentrada.ui.screens.entradas

import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import pe.edu.upn.registroentrada.ui.components.BarraSuperior
import pe.edu.upn.registroentrada.ui.components.BotonPrimario
import pe.edu.upn.registroentrada.ui.components.CampoTextoValidado
import pe.edu.upn.registroentrada.ui.components.EstadoVacio
import pe.edu.upn.registroentrada.ui.components.TituloSenal
import pe.edu.upn.registroentrada.ui.theme.FuenteMono
import pe.edu.upn.registroentrada.ui.util.Formato

@Composable
fun EmitirEntradaScreen(
    onVolver: () -> Unit,
    onEmitidas: (Long) -> Unit,
    viewModel: EmitirEntradaViewModel = hiltViewModel()
) {
    val estado by viewModel.estado.collectAsStateWithLifecycle()

    Scaffold(
        topBar = { BarraSuperior(titulo = "Emitir entradas", onVolver = onVolver) }
    ) { relleno ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(relleno)
                .padding(horizontal = 20.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            item {
                Spacer(Modifier.height(4.dp))
                Text(
                    "Genera entradas con código QR único, sin registrar personas.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(Modifier.height(12.dp))
                TituloSenal("Tipo de entrada")
                Spacer(Modifier.height(4.dp))
            }
            if (estado.tipos.isEmpty()) {
                item { EstadoVacio("Este evento no tiene tipos de entrada") }
            } else {
                items(estado.tipos, key = { it.id }) { tipo ->
                    FilaSeleccion(
                        titulo = tipo.nombre,
                        subtitulo = "${Formato.moneda(tipo.precio)} · cupo ${tipo.cupo}",
                        seleccionado = estado.tipoSeleccionado?.id == tipo.id,
                        onClick = { viewModel.seleccionarTipo(tipo) }
                    )
                }
            }

            item {
                Spacer(Modifier.height(12.dp))
                TituloSenal("¿Cuántas?")
                Spacer(Modifier.height(8.dp))
                CampoTextoValidado(
                    valor = estado.cantidad,
                    onValorChange = viewModel::cambiarCantidad,
                    etiqueta = "Cantidad de entradas",
                    soporte = "Se generará un QR único por cada una",
                    soloNumeros = true
                )
                estado.mensaje?.let {
                    Spacer(Modifier.height(10.dp))
                    Text(it, color = MaterialTheme.colorScheme.error)
                }
                Spacer(Modifier.height(20.dp))
                BotonPrimario(
                    texto = "Emitir ${estado.cantidad.ifBlank { "0" }} entrada(s)",
                    onClick = { viewModel.emitir(onEmitidas) },
                    cargando = estado.emitiendo,
                    habilitado = estado.tipoSeleccionado != null && !estado.emitiendo
                )
                Spacer(Modifier.height(24.dp))
            }
        }
    }
}

@Composable
private fun FilaSeleccion(
    titulo: String,
    subtitulo: String,
    seleccionado: Boolean,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .border(
                width = if (seleccionado) 2.dp else 1.dp,
                color = if (seleccionado) {
                    MaterialTheme.colorScheme.primary
                } else {
                    MaterialTheme.colorScheme.outlineVariant
                },
                shape = MaterialTheme.shapes.small
            )
            .clickable(onClick = onClick),
        shape = MaterialTheme.shapes.small,
        color = if (seleccionado) {
            MaterialTheme.colorScheme.surface
        } else {
            MaterialTheme.colorScheme.surfaceVariant
        }
    ) {
        Column(Modifier.padding(14.dp)) {
            Text(titulo, style = MaterialTheme.typography.titleMedium)
            Text(
                subtitulo,
                fontFamily = FuenteMono,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
