package pe.edu.upn.registroentrada.ui.screens.entradas

import androidx.compose.foundation.Image
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import android.widget.Toast
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import pe.edu.upn.registroentrada.domain.model.EntradaDetalle
import pe.edu.upn.registroentrada.domain.model.EstadoEntrada
import pe.edu.upn.registroentrada.scanner.GeneradorQr
import pe.edu.upn.registroentrada.ui.components.BarraSuperior
import pe.edu.upn.registroentrada.ui.components.ChipEstado
import pe.edu.upn.registroentrada.ui.components.DialogoConfirmacion
import pe.edu.upn.registroentrada.ui.components.EtiquetaMono
import pe.edu.upn.registroentrada.ui.components.LineaPerforada
import pe.edu.upn.registroentrada.ui.theme.FuenteMono
import pe.edu.upn.registroentrada.ui.theme.coloresSenal
import pe.edu.upn.registroentrada.ui.util.ExportadorQr
import pe.edu.upn.registroentrada.ui.util.Formato

@Composable
fun EntradaDetalleScreen(
    onVolver: () -> Unit,
    viewModel: EntradaDetalleViewModel = hiltViewModel()
) {
    val estado by viewModel.estado.collectAsStateWithLifecycle()
    var confirmarAnulacion by remember { mutableStateOf(false) }

    Scaffold(
        topBar = { BarraSuperior(titulo = "Entrada", onVolver = onVolver) }
    ) { relleno ->
        val detalle = estado.detalle
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(relleno)
                .padding(20.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            if (detalle == null) {
                Text("Cargando entrada…")
                return@Column
            }
            TarjetaEntrada(detalle = detalle, qrTexto = estado.qrTexto)
            estado.mensaje?.let {
                Spacer(Modifier.height(12.dp))
                Text(it, color = MaterialTheme.colorScheme.error)
            }
            val contexto = LocalContext.current
            if (estado.qrTexto != null) {
                Spacer(Modifier.height(20.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    Button(
                        onClick = {
                            ExportadorQr.compartir(
                                context = contexto,
                                textoQr = estado.qrTexto!!,
                                nombreArchivo = nombreArchivo(detalle),
                                texto = textoCompartir(detalle)
                            )
                        },
                        shape = MaterialTheme.shapes.small,
                        modifier = Modifier.weight(1f).height(50.dp)
                    ) { Text("Compartir QR", style = MaterialTheme.typography.labelLarge) }
                    OutlinedButton(
                        onClick = {
                            val ruta = ExportadorQr.guardarEnDescargas(
                                contexto,
                                estado.qrTexto!!,
                                nombreArchivo(detalle)
                            )
                            Toast.makeText(
                                contexto,
                                if (ruta != null) "Guardado en $ruta" else "No se pudo guardar",
                                Toast.LENGTH_LONG
                            ).show()
                        },
                        shape = MaterialTheme.shapes.small,
                        modifier = Modifier.weight(1f).height(50.dp)
                    ) { Text("Guardar imagen", style = MaterialTheme.typography.labelLarge) }
                }
            }
            Spacer(Modifier.height(20.dp))
            if (detalle.entrada.estado == EstadoEntrada.VALIDA) {
                OutlinedButton(
                    onClick = { confirmarAnulacion = true },
                    shape = MaterialTheme.shapes.small,
                    modifier = Modifier.fillMaxWidth().height(50.dp)
                ) {
                    Text(
                        "Anular entrada",
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.labelLarge
                    )
                }
            }
        }
    }

    if (confirmarAnulacion) {
        DialogoConfirmacion(
            titulo = "Anular entrada",
            mensaje = "¿Seguro que quieres anular esta entrada? El QR dejará de permitir el ingreso.",
            textoConfirmar = "Anular",
            onConfirmar = {
                viewModel.anular()
                confirmarAnulacion = false
            },
            onCancelar = { confirmarAnulacion = false }
        )
    }
}

@Composable
private fun TarjetaEntrada(detalle: EntradaDetalle, qrTexto: String?) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.large,
        color = MaterialTheme.colorScheme.surface,
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            MaterialTheme.colorScheme.outlineVariant
        )
    ) {
        Column {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        detalle.nombreEvento.uppercase(),
                        fontFamily = FuenteMono,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    EstadoChip(detalle)
                }
                Spacer(Modifier.height(20.dp))
                if (qrTexto != null) {
                    val imagen = remember(qrTexto) {
                        GeneradorQr.generar(qrTexto, 720).asImageBitmap()
                    }
                    Surface(
                        color = androidx.compose.ui.graphics.Color.White,
                        shape = MaterialTheme.shapes.small
                    ) {
                        Image(
                            bitmap = imagen,
                            contentDescription = "Código QR de la entrada",
                            modifier = Modifier.padding(12.dp).size(232.dp)
                        )
                    }
                }
                Spacer(Modifier.height(12.dp))
                Text(
                    "PRESENTA ESTE CÓDIGO EN LA PUERTA",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )
            }

            Perforacion()

            Column(Modifier.fillMaxWidth().padding(20.dp)) {
                Text(
                    detalle.nombreAsistente ?: detalle.nombreTipoEntrada,
                    style = MaterialTheme.typography.headlineSmall
                )
                Spacer(Modifier.height(2.dp))
                Text(
                    detalle.documentoAsistente?.let { "DNI $it · ${detalle.nombreTipoEntrada}" }
                        ?: "Entrada de tipo ${detalle.nombreTipoEntrada}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(Modifier.height(16.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    DatoEtiquetado("EMITIDA", Formato.fechaHora(detalle.entrada.emitidaEn))
                    detalle.entrada.usadaEn?.let {
                        DatoEtiquetado("USADA", Formato.fechaHora(it), alineacionFinal = true)
                    }
                }
                Spacer(Modifier.height(16.dp))
                EtiquetaMono(qrTexto.orEmpty())
            }
        }
    }
}

@Composable
private fun Perforacion() {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            Modifier
                .size(16.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.background)
        )
        LineaPerforada(
            color = MaterialTheme.colorScheme.outline,
            modifier = Modifier.weight(1f)
        )
        Box(
            Modifier
                .size(16.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.background)
        )
    }
}

@Composable
private fun DatoEtiquetado(etiqueta: String, valor: String, alineacionFinal: Boolean = false) {
    Column(horizontalAlignment = if (alineacionFinal) Alignment.End else Alignment.Start) {
        Text(
            etiqueta,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(valor, fontFamily = FuenteMono, style = MaterialTheme.typography.bodySmall)
    }
}

@Composable
private fun EstadoChip(detalle: EntradaDetalle) {
    val senal = coloresSenal()
    val (texto, color) = when (detalle.entrada.estado) {
        EstadoEntrada.VALIDA -> "Válida" to senal.acceso
        EstadoEntrada.USADA -> "Usada" to senal.atencion
        EstadoEntrada.ANULADA -> "Anulada" to senal.rechazo
    }
    ChipEstado(texto = texto, color = color)
}

private fun nombreArchivo(detalle: EntradaDetalle): String =
    "entrada_${detalle.entrada.codigo.take(8)}.png"

private fun textoCompartir(detalle: EntradaDetalle): String =
    "Entrada ${detalle.nombreTipoEntrada} — ${detalle.nombreEvento}. " +
        "Código: ${detalle.entrada.codigo}"
