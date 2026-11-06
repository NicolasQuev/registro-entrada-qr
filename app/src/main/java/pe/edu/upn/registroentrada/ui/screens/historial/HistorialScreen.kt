package pe.edu.upn.registroentrada.ui.screens.historial

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import pe.edu.upn.registroentrada.domain.model.RegistroAccesoDetalle
import pe.edu.upn.registroentrada.domain.model.ResultadoAcceso
import pe.edu.upn.registroentrada.ui.components.BarraSuperior
import pe.edu.upn.registroentrada.ui.components.ChipEstado
import pe.edu.upn.registroentrada.ui.components.EstadoVacio
import pe.edu.upn.registroentrada.ui.components.TarjetaApp
import pe.edu.upn.registroentrada.ui.theme.FuenteMono
import pe.edu.upn.registroentrada.ui.theme.coloresSenal
import pe.edu.upn.registroentrada.ui.util.Formato

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistorialScreen(viewModel: HistorialViewModel = hiltViewModel()) {
    val estado by viewModel.estado.collectAsStateWithLifecycle()

    Scaffold(topBar = { BarraSuperior(titulo = "Historial") }) { relleno ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(relleno)
                .padding(horizontal = 20.dp)
        ) {
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                item {
                    FilterChip(
                        selected = estado.filtro == null,
                        onClick = { viewModel.filtrar(null) },
                        label = { Text("Todos") }
                    )
                }
                items(ResultadoAcceso.entries.toList()) { resultado ->
                    FilterChip(
                        selected = estado.filtro == resultado,
                        onClick = { viewModel.filtrar(resultado) },
                        label = { Text(etiqueta(resultado)) }
                    )
                }
            }
            Spacer(Modifier.height(16.dp))
            if (estado.registros.isEmpty()) {
                EstadoVacio("No hay registros para estos filtros")
            } else {
                LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    items(estado.registros, key = { it.registro.id }) { detalle ->
                        TarjetaRegistro(detalle)
                    }
                }
            }
        }
    }
}

@Composable
private fun TarjetaRegistro(detalle: RegistroAccesoDetalle) {
    val senal = coloresSenal()
    val color = if (detalle.registro.resultado.permitido) senal.acceso else senal.rechazo
    TarjetaApp(colorBanda = color) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                Formato.hora(detalle.registro.fechaHora),
                fontFamily = FuenteMono,
                style = MaterialTheme.typography.titleMedium
            )
            ChipEstado(texto = etiqueta(detalle.registro.resultado), color = color)
        }
        Spacer(Modifier.height(6.dp))
        Text(
            "Puerta ${detalle.registro.puerta} · ${detalle.nombreOperador}",
            style = MaterialTheme.typography.bodyMedium
        )
        detalle.nombreAsistente?.let {
            Text(it, style = MaterialTheme.typography.bodyMedium)
        }
        Spacer(Modifier.height(6.dp))
        Text(
            detalle.registro.codigoEscaneado,
            fontFamily = FuenteMono,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

private fun etiqueta(resultado: ResultadoAcceso): String = when (resultado) {
    ResultadoAcceso.PERMITIDO -> "Permitido"
    ResultadoAcceso.YA_USADA -> "Ya usada"
    ResultadoAcceso.NO_EXISTE -> "No existe"
    ResultadoAcceso.OTRO_EVENTO -> "Otro evento"
    ResultadoAcceso.ANULADA -> "Anulada"
    ResultadoAcceso.FIRMA_INVALIDA -> "Firma inválida"
    ResultadoAcceso.AFORO_LLENO -> "Aforo lleno"
    ResultadoAcceso.EVENTO_CANCELADO -> "Evento cancelado"
}
