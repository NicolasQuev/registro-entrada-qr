package pe.edu.upn.registroentrada.ui.screens.entradas

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import pe.edu.upn.registroentrada.domain.model.EntradaDetalle
import pe.edu.upn.registroentrada.domain.model.EstadoEntrada
import pe.edu.upn.registroentrada.scanner.GeneradorQr
import pe.edu.upn.registroentrada.ui.components.BarraSuperior
import pe.edu.upn.registroentrada.ui.components.ChipEstado
import pe.edu.upn.registroentrada.ui.components.EstadoVacio
import pe.edu.upn.registroentrada.ui.components.TarjetaApp
import pe.edu.upn.registroentrada.ui.theme.FuenteMono
import pe.edu.upn.registroentrada.ui.theme.coloresSenal
import pe.edu.upn.registroentrada.ui.util.Formato

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EntradasScreen(
    onVolver: () -> Unit,
    onEntrada: (Long) -> Unit,
    viewModel: EntradasViewModel = hiltViewModel()
) {
    val estado by viewModel.estado.collectAsStateWithLifecycle()

    Scaffold(
        topBar = { BarraSuperior(titulo = "Entradas", onVolver = onVolver) }
    ) { relleno ->
        if (estado.entradas.isEmpty()) {
            EstadoVacio(
                mensaje = "Todavía no se emitió ninguna entrada",
                modifier = Modifier.padding(relleno)
            )
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(relleno)
                    .padding(horizontal = 20.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(estado.entradas, key = { it.entrada.id }) { detalle ->
                    TarjetaEntradaLista(
                        detalle = detalle,
                        onClick = { onEntrada(detalle.entrada.id) }
                    )
                }
            }
        }
    }
}

private fun textoQr(detalle: EntradaDetalle): String =
    "RE|1|${detalle.entrada.codigo}|${detalle.entrada.firma}"

@Composable
private fun TarjetaEntradaLista(detalle: EntradaDetalle, onClick: () -> Unit) {
    val senal = coloresSenal()
    val (texto, color) = when (detalle.entrada.estado) {
        EstadoEntrada.VALIDA -> "Válida" to senal.acceso
        EstadoEntrada.USADA -> "Usada" to senal.atencion
        EstadoEntrada.ANULADA -> "Anulada" to senal.rechazo
    }
    TarjetaApp(
        modifier = Modifier.clickable(onClick = onClick),
        colorBanda = color
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Surface(
                color = Color.White,
                shape = RoundedCornerShape(6.dp),
                modifier = Modifier.size(64.dp)
            ) {
                val imagen = remember(detalle.entrada.codigo) {
                    GeneradorQr.generar(textoQr(detalle), 160).asImageBitmap()
                }
                Image(
                    bitmap = imagen,
                    contentDescription = "QR de la entrada",
                    modifier = Modifier.padding(5.dp)
                )
            }
            Spacer(Modifier.width(14.dp))
            Column(Modifier.weight(1f)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(detalle.nombreTipoEntrada, style = MaterialTheme.typography.titleMedium)
                    ChipEstado(texto = texto, color = color)
                }
                Spacer(Modifier.height(4.dp))
                Text(
                    detalle.entrada.codigo,
                    fontFamily = FuenteMono,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    "Emitida ${Formato.fechaHora(detalle.entrada.emitidaEn)}",
                    fontFamily = FuenteMono,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
