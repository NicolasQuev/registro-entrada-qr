package pe.edu.upn.registroentrada.core.location

import android.annotation.SuppressLint
import android.content.Context
import android.location.Location
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationTokenSource
import com.google.android.gms.tasks.Task
import kotlin.coroutines.resume
import kotlinx.coroutines.suspendCancellableCoroutine

/**
 * Implementación con FusedLocationProviderClient (Google Play Services Location)
 * para obtener la ubicación GPS actual del dispositivo.
 */
class ProveedorUbicacionGps(private val context: Context) : ProveedorUbicacion {

    private val cliente by lazy { LocationServices.getFusedLocationProviderClient(context) }

    @SuppressLint("MissingPermission")
    override suspend fun obtenerUbicacionActual(): Result<Coordenadas> = try {
        val cancelacion = CancellationTokenSource()
        val actual = cliente
            .getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, cancelacion.token)
            .esperar(cancelacion)
        val ubicacion = actual ?: cliente.lastLocation.esperar(null)
        if (ubicacion != null) {
            Result.success(Coordenadas(ubicacion.latitude, ubicacion.longitude))
        } else {
            Result.failure(IllegalStateException("No se pudo obtener la ubicación GPS actual"))
        }
    } catch (_: SecurityException) {
        Result.failure(IllegalStateException("Permiso de ubicación requerido"))
    } catch (e: Exception) {
        Result.failure(IllegalStateException(e.message ?: "Error al consultar GPS"))
    }

    private suspend fun Task<Location>.esperar(
        cancelacion: CancellationTokenSource?
    ): Location? = suspendCancellableCoroutine { cont ->
        addOnSuccessListener { loc ->
            if (cont.isActive) cont.resume(loc)
        }
        addOnFailureListener {
            if (cont.isActive) cont.resume(null)
        }
        addOnCanceledListener {
            if (cont.isActive) cont.resume(null)
        }
        cont.invokeOnCancellation { cancelacion?.cancel() }
    }
}
