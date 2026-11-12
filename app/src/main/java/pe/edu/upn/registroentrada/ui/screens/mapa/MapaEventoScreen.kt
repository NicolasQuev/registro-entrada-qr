package pe.edu.upn.registroentrada.ui.screens.mapa

import android.Manifest
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.MapProperties
import com.google.maps.android.compose.MapUiSettings
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.MarkerState
import com.google.maps.android.compose.rememberCameraPositionState
import pe.edu.upn.registroentrada.domain.model.Evento
import pe.edu.upn.registroentrada.ui.components.BarraSuperior

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun MapaEventoScreen(
    evento: Evento,
    onVolver: () -> Unit
) {
    val permUbicacion = rememberPermissionState(Manifest.permission.ACCESS_FINE_LOCATION)

    val lat = evento.latitud ?: -12.0464
    val lng = evento.longitud ?: -77.0428
    val posicionEvento = remember(lat, lng) { LatLng(lat, lng) }

    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(posicionEvento, 15f)
    }

    LaunchedEffect(posicionEvento) {
        cameraPositionState.position = CameraPosition.fromLatLngZoom(posicionEvento, 15f)
    }

    Scaffold(
        topBar = {
            BarraSuperior(
                titulo = "Ubicación del evento",
                onVolver = onVolver
            )
        }
    ) { relleno ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(relleno)
        ) {
            // Panel informativo de Privacidad y Permiso
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                )
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Filled.LocationOn,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(Modifier.width(8.dp))
                        Text(
                            evento.nombre,
                            style = MaterialTheme.typography.titleMedium
                        )
                    }
                    Spacer(Modifier.height(4.dp))
                    Text(
                        evento.lugar,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        "Coords: ${String.format(java.util.Locale.US, "%.5f, %.5f", lat, lng)}",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(Modifier.height(8.dp))
                    if (!permUbicacion.status.isGranted) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                "Activa los permisos para ver tu ubicación respecto al local",
                                style = MaterialTheme.typography.bodySmall,
                                modifier = Modifier.weight(1f)
                            )
                            Button(onClick = { permUbicacion.launchPermissionRequest() }) {
                                Text("Permitir")
                            }
                        }
                    } else {
                        Text(
                            "✓ Permiso de ubicación concedido (Privacidad respetada)",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }

            // Mapa interactivo con Google Maps Compose
            Box(modifier = Modifier.fillMaxSize()) {
                GoogleMap(
                    modifier = Modifier.fillMaxSize(),
                    cameraPositionState = cameraPositionState,
                    properties = MapProperties(
                        isMyLocationEnabled = permUbicacion.status.isGranted
                    ),
                    uiSettings = MapUiSettings(
                        myLocationButtonEnabled = permUbicacion.status.isGranted,
                        zoomControlsEnabled = true,
                        compassEnabled = true
                    )
                ) {
                    val markerState = remember(posicionEvento) { MarkerState(position = posicionEvento) }
                    Marker(
                        state = markerState,
                        title = evento.nombre,
                        snippet = evento.lugar
                    )
                }
            }
        }
    }
}
