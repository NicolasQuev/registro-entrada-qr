package pe.edu.upn.registroentrada.ui.screens.escaner

import android.Manifest
import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Keyboard
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import pe.edu.upn.registroentrada.domain.model.ResultadoAcceso
import pe.edu.upn.registroentrada.scanner.CamaraEscanerQr
import pe.edu.upn.registroentrada.ui.components.BarraSuperior
import pe.edu.upn.registroentrada.ui.components.BotonPrimario
import pe.edu.upn.registroentrada.ui.components.CampoTextoValidado
import pe.edu.upn.registroentrada.ui.components.TituloSenal
import pe.edu.upn.registroentrada.ui.theme.FuenteMono
import pe.edu.upn.registroentrada.ui.theme.coloresSenal

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun EscanerScreen(viewModel: EscanerViewModel = hiltViewModel()) {
    val estado by viewModel.estado.collectAsStateWithLifecycle()
    val senal = coloresSenal()

    var modoCamaraActivo by remember { mutableStateOf(false) }
    val permCamara = rememberPermissionState(Manifest.permission.CAMERA)

    Scaffold(
        topBar = {
            BarraSuperior(
                titulo = "Puerta",
                acciones = {
                    Text(
                        if (estado.eventoActivoId > 0) "PUERTA ${estado.puerta}" else "SIN EVENTO",
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
        ) {
            Text(
                estado.eventoNombre.ifBlank { "Selecciona un evento en Inicio" },
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(Modifier.height(16.dp))

            val validacion = estado.validacion
            if (validacion != null) {
                val color = if (validacion.permitido) senal.acceso else senal.rechazo
                val sobre = if (validacion.permitido) senal.sobreAcceso else senal.sobreRechazo
                Surface(
                    color = color,
                    shape = MaterialTheme.shapes.large,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(Modifier.padding(24.dp)) {
                        Text(
                            text = if (validacion.permitido) "PERMITIDO" else "RECHAZADO",
                            style = MaterialTheme.typography.displayMedium,
                            color = sobre
                        )
                        Spacer(Modifier.height(4.dp))
                        Text(
                            text = mensajeResultado(validacion.registro.resultado),
                            style = MaterialTheme.typography.bodyLarge,
                            color = sobre
                        )
                        validacion.detalleEntrada?.let { detalle ->
                            Spacer(Modifier.height(18.dp))
                            Text(
                                detalle.nombreAsistente ?: detalle.nombreTipoEntrada,
                                style = MaterialTheme.typography.titleLarge,
                                color = sobre
                            )
                            Text(
                                detalle.documentoAsistente?.let { "DNI $it · ${detalle.nombreTipoEntrada}" }
                                    ?: "Entrada de tipo ${detalle.nombreTipoEntrada}",
                                style = MaterialTheme.typography.bodyMedium,
                                color = sobre.copy(alpha = 0.85f)
                            )
                        }
                        Spacer(Modifier.height(14.dp))
                        Text(
                            validacion.registro.codigoEscaneado,
                            fontFamily = FuenteMono,
                            style = MaterialTheme.typography.bodySmall,
                            color = sobre.copy(alpha = 0.8f)
                        )
                    }
                }
                Spacer(Modifier.height(16.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedButton(
                        onClick = {
                            viewModel.limpiar()
                        },
                        shape = MaterialTheme.shapes.small,
                        modifier = Modifier.fillMaxWidth().height(50.dp)
                    ) { Text("Siguiente escaneo", style = MaterialTheme.typography.labelLarge) }
                }
            } else {
                // Selector de Modo: Cámara o Manual
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = {
                            if (!permCamara.status.isGranted) {
                                permCamara.launchPermissionRequest()
                            }
                            modoCamaraActivo = true
                        },
                        shape = MaterialTheme.shapes.small,
                        modifier = Modifier.weight(1f).height(48.dp)
                    ) {
                        Icon(Icons.Filled.CameraAlt, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.size(8.dp))
                        Text("Activar Cámara")
                    }

                    OutlinedButton(
                        onClick = { modoCamaraActivo = false },
                        shape = MaterialTheme.shapes.small,
                        modifier = Modifier.weight(1f).height(48.dp)
                    ) {
                        Icon(Icons.Filled.Keyboard, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.size(8.dp))
                        Text("Manual")
                    }
                }

                Spacer(Modifier.height(16.dp))

                if (modoCamaraActivo) {
                    if (permCamara.status.isGranted) {
                        Card(
                            shape = RoundedCornerShape(16.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(380.dp)
                                .clip(RoundedCornerShape(16.dp))
                        ) {
                            CamaraEscanerQr(
                                onCodigoDetectado = { codigoLeido ->
                                    viewModel.cambiarCodigo(codigoLeido)
                                    viewModel.validar()
                                },
                                modifier = Modifier.fillMaxSize()
                            )
                        }
                    } else {
                        Card(
                            modifier = Modifier.fillMaxWidth().padding(8.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                        ) {
                            Column(Modifier.padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("Se requiere permiso de cámara para escanear el código QR")
                                Spacer(Modifier.height(8.dp))
                                Button(onClick = { permCamara.launchPermissionRequest() }) {
                                    Text("Conceder permiso de Cámara")
                                }
                            }
                        }
                    }
                } else {
                    TituloSenal("Validar código manualmente")
                    Spacer(Modifier.height(12.dp))
                    CampoTextoValidado(
                        valor = estado.codigoManual,
                        onValorChange = viewModel::cambiarCodigo,
                        etiqueta = "Código de la entrada",
                        soporte = "RE|1|código|firma, o solo el código"
                    )
                    Spacer(Modifier.height(16.dp))
                    BotonPrimario(
                        texto = "Validar",
                        onClick = viewModel::validar,
                        cargando = estado.procesando,
                        habilitado = estado.codigoManual.isNotBlank() && estado.eventoActivoId > 0
                    )
                }

                estado.mensaje?.let {
                    Spacer(Modifier.height(14.dp))
                    Text(it, color = MaterialTheme.colorScheme.error)
                }
            }
        }
    }
}

private fun mensajeResultado(resultado: ResultadoAcceso): String = when (resultado) {
    ResultadoAcceso.PERMITIDO -> "Puede ingresar"
    ResultadoAcceso.YA_USADA -> "La entrada ya fue usada"
    ResultadoAcceso.NO_EXISTE -> "Entrada no encontrada en este dispositivo"
    ResultadoAcceso.OTRO_EVENTO -> "La entrada pertenece a otro evento"
    ResultadoAcceso.ANULADA -> "La entrada está anulada"
    ResultadoAcceso.FIRMA_INVALIDA -> "Código falsificado o de otro dispositivo"
    ResultadoAcceso.AFORO_LLENO -> "Aforo completo"
    ResultadoAcceso.EVENTO_CANCELADO -> "El evento está cancelado"
}
