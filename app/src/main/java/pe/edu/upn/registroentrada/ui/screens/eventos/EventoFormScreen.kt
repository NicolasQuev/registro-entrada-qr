package pe.edu.upn.registroentrada.ui.screens.eventos

import android.Manifest
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.rememberMultiplePermissionsState
import pe.edu.upn.registroentrada.ui.components.BarraSuperior
import pe.edu.upn.registroentrada.ui.components.BotonPrimario
import pe.edu.upn.registroentrada.ui.components.CampoTextoValidado
import pe.edu.upn.registroentrada.ui.components.TituloSenal

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun EventoFormScreen(
    onVolver: () -> Unit,
    onCreado: (Long) -> Unit,
    viewModel: EventoFormViewModel = hiltViewModel()
) {
    val estado by viewModel.estado.collectAsStateWithLifecycle()
    var solicitarUbicacionPendiente by remember { mutableStateOf(false) }
    val permUbicacion = rememberMultiplePermissionsState(
        listOf(
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION
        )
    )
    val tienePermisoUbicacion = permUbicacion.permissions.any {
        it.status == com.google.accompanist.permissions.PermissionStatus.Granted
    }

    LaunchedEffect(tienePermisoUbicacion, solicitarUbicacionPendiente) {
        if (tienePermisoUbicacion && solicitarUbicacionPendiente) {
            solicitarUbicacionPendiente = false
            viewModel.obtenerUbicacionGps()
        }
    }

    Scaffold(
        topBar = { BarraSuperior(titulo = "Nuevo evento", onVolver = onVolver) }
    ) { relleno ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(relleno)
                .padding(20.dp)
                .verticalScroll(rememberScrollState())
        ) {
            TituloSenal("Datos del evento")
            Spacer(Modifier.height(16.dp))
            CampoTextoValidado(
                valor = estado.nombre,
                onValorChange = viewModel::cambiarNombre,
                etiqueta = "Nombre",
                error = estado.errorNombre
            )
            Spacer(Modifier.height(12.dp))
            CampoTextoValidado(
                valor = estado.lugar,
                onValorChange = viewModel::cambiarLugar,
                etiqueta = "Lugar / Dirección",
                error = estado.errorLugar
            )
            Spacer(Modifier.height(12.dp))
            CampoTextoValidado(
                valor = estado.descripcion,
                onValorChange = viewModel::cambiarDescripcion,
                etiqueta = "Descripción (opcional)"
            )

            Spacer(Modifier.height(20.dp))
            TituloSenal("Geolocalización (Google Maps)")
            Spacer(Modifier.height(16.dp))
            Row2(
                primer = {
                    CampoTextoValidado(
                        valor = estado.latitud,
                        onValorChange = viewModel::cambiarLatitud,
                        etiqueta = "Latitud",
                        soporte = "Ej. -12.0673",
                        error = estado.errorCoordenadas
                    )
                },
                segundo = {
                    CampoTextoValidado(
                        valor = estado.longitud,
                        onValorChange = viewModel::cambiarLongitud,
                        etiqueta = "Longitud",
                        soporte = "Ej. -77.0337"
                    )
                }
            )
            Spacer(Modifier.height(8.dp))
            OutlinedButton(
                onClick = {
                    if (tienePermisoUbicacion) {
                        viewModel.obtenerUbicacionGps()
                    } else {
                        solicitarUbicacionPendiente = true
                        permUbicacion.launchMultiplePermissionRequest()
                    }
                },
                enabled = !estado.obteniendoUbicacion,
                modifier = Modifier.fillMaxWidth()
            ) {
                if (estado.obteniendoUbicacion) {
                    CircularProgressIndicator(modifier = Modifier.size(16.dp), strokeWidth = 2.dp)
                    Spacer(Modifier.width(8.dp))
                    Text("Obteniendo ubicación GPS…", style = MaterialTheme.typography.labelMedium)
                } else {
                    Icon(Icons.Filled.MyLocation, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(8.dp))
                    Text("Usar mi ubicación GPS", style = MaterialTheme.typography.labelMedium)
                }
            }
            Spacer(Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedButton(
                    onClick = { viewModel.fijarCoordenadas(-12.0673, -77.0337) },
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Estadio Nacional", style = MaterialTheme.typography.labelSmall)
                }
                OutlinedButton(
                    onClick = { viewModel.fijarCoordenadas(-12.0864, -77.0016) },
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Convenciones Lima", style = MaterialTheme.typography.labelSmall)
                }
            }

            Spacer(Modifier.height(20.dp))
            TituloSenal("Fechas y aforo")
            Spacer(Modifier.height(16.dp))
            Row2(
                primer = {
                    CampoTextoValidado(
                        valor = estado.fechaInicio,
                        onValorChange = viewModel::cambiarFechaInicio,
                        etiqueta = "Inicio",
                        error = estado.errorFechaInicio,
                        soporte = "dd/MM/yyyy HH:mm"
                    )
                },
                segundo = {
                    CampoTextoValidado(
                        valor = estado.fechaFin,
                        onValorChange = viewModel::cambiarFechaFin,
                        etiqueta = "Fin",
                        error = estado.errorFechaFin,
                        soporte = "dd/MM/yyyy HH:mm"
                    )
                }
            )
            Spacer(Modifier.height(12.dp))
            CampoTextoValidado(
                valor = estado.aforo,
                onValorChange = viewModel::cambiarAforo,
                etiqueta = "Aforo máximo",
                error = estado.errorAforo,
                soloNumeros = true
            )
            estado.mensaje?.let {
                Spacer(Modifier.height(10.dp))
                Text(it, color = MaterialTheme.colorScheme.error)
            }
            Spacer(Modifier.height(24.dp))
            BotonPrimario(
                texto = "Guardar evento",
                onClick = { viewModel.guardar(onCreado) },
                cargando = estado.guardando,
                habilitado = !estado.guardando
            )
        }
    }
}

@Composable
private fun Row2(
    primer: @Composable () -> Unit,
    segundo: @Composable () -> Unit
) {
    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        Box(Modifier.weight(1f)) { primer() }
        Box(Modifier.weight(1f)) { segundo() }
    }
}
