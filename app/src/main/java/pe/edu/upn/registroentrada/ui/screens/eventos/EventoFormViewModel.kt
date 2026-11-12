package pe.edu.upn.registroentrada.ui.screens.eventos

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import pe.edu.upn.registroentrada.core.Resultado
import pe.edu.upn.registroentrada.core.ValidadorCoordenadas
import pe.edu.upn.registroentrada.core.location.ProveedorUbicacion
import pe.edu.upn.registroentrada.domain.usecase.CrearEventoUseCase
import pe.edu.upn.registroentrada.ui.util.Formato

data class EventoFormUiState(
    val nombre: String = "",
    val descripcion: String = "",
    val lugar: String = "",
    val latitud: String = "-12.0464", // Coordenadas predeterminadas de Lima
    val longitud: String = "-77.0428",
    val fechaInicio: String = Formato.fechaHora(Instant.now()),
    val fechaFin: String = Formato.fechaHora(Instant.now().plusSeconds(4 * 3600)),
    val aforo: String = "",
    val errorNombre: String? = null,
    val errorLugar: String? = null,
    val errorFechaInicio: String? = null,
    val errorFechaFin: String? = null,
    val errorAforo: String? = null,
    val errorCoordenadas: String? = null,
    val mensaje: String? = null,
    val obteniendoUbicacion: Boolean = false,
    val guardando: Boolean = false
)

@HiltViewModel
class EventoFormViewModel @Inject constructor(
    private val crearEvento: CrearEventoUseCase,
    private val proveedorUbicacion: ProveedorUbicacion
) : ViewModel() {

    private val _estado = MutableStateFlow(EventoFormUiState())
    val estado = _estado.asStateFlow()

    fun cambiarNombre(v: String) = _estado.update { it.copy(nombre = v, errorNombre = null) }
    fun cambiarDescripcion(v: String) = _estado.update { it.copy(descripcion = v) }
    fun cambiarLugar(v: String) = _estado.update { it.copy(lugar = v, errorLugar = null) }
    fun cambiarLatitud(v: String) = _estado.update { it.copy(latitud = v, errorCoordenadas = null) }
    fun cambiarLongitud(v: String) = _estado.update { it.copy(longitud = v, errorCoordenadas = null) }
    fun fijarCoordenadas(lat: Double, lng: Double) = _estado.update {
        it.copy(
            latitud = String.format(java.util.Locale.US, "%.6f", lat),
            longitud = String.format(java.util.Locale.US, "%.6f", lng),
            errorCoordenadas = null
        )
    }
    fun cambiarFechaInicio(v: String) = _estado.update { it.copy(fechaInicio = v, errorFechaInicio = null) }
    fun cambiarFechaFin(v: String) = _estado.update { it.copy(fechaFin = v, errorFechaFin = null) }
    fun cambiarAforo(v: String) = _estado.update { it.copy(aforo = v.filter { c -> c.isDigit() }, errorAforo = null) }

    fun obtenerUbicacionGps() {
        if (_estado.value.obteniendoUbicacion) return
        _estado.update { it.copy(obteniendoUbicacion = true, errorCoordenadas = null) }
        viewModelScope.launch {
            proveedorUbicacion.obtenerUbicacionActual().fold(
                onSuccess = { coords ->
                    _estado.update {
                        it.copy(
                            obteniendoUbicacion = false,
                            latitud = String.format(java.util.Locale.US, "%.6f", coords.latitud),
                            longitud = String.format(java.util.Locale.US, "%.6f", coords.longitud),
                            errorCoordenadas = null
                        )
                    }
                },
                onFailure = { err ->
                    _estado.update {
                        it.copy(
                            obteniendoUbicacion = false,
                            errorCoordenadas = err.message ?: "No se pudo obtener la ubicación GPS"
                        )
                    }
                }
            )
        }
    }

    fun guardar(onCreado: (Long) -> Unit) {
        val actual = _estado.value
        val inicio = parsearFecha(actual.fechaInicio)
        val fin = parsearFecha(actual.fechaFin)
        val aforo = actual.aforo.toIntOrNull()
        val lat = actual.latitud.toDoubleOrNull()
        val lng = actual.longitud.toDoubleOrNull()
        val errorCoord = when {
            lat == null || lng == null -> "Coordenadas geográficas inválidas"
            else -> ValidadorCoordenadas.validar(lat, lng)
        }

        _estado.update {
            it.copy(
                errorNombre = if (actual.nombre.isBlank()) "El nombre del evento es obligatorio" else null,
                errorLugar = if (actual.lugar.isBlank()) "El lugar del evento es obligatorio" else null,
                errorFechaInicio = if (inicio == null) "Usa el formato dd/MM/yyyy HH:mm" else null,
                errorFechaFin = if (fin == null) "Usa el formato dd/MM/yyyy HH:mm" else null,
                errorAforo = if (aforo == null || aforo <= 0) "El aforo debe ser un número mayor a 0" else null,
                errorCoordenadas = errorCoord
            )
        }
        val hayError = _estado.value.let {
            it.errorNombre != null || it.errorLugar != null || it.errorFechaInicio != null ||
                it.errorFechaFin != null || it.errorAforo != null || it.errorCoordenadas != null
        }
        if (hayError || inicio == null || fin == null || aforo == null) return

        _estado.update { it.copy(guardando = true, mensaje = null) }
        viewModelScope.launch {
            when (val resultado = crearEvento(
                nombre = actual.nombre,
                descripcion = actual.descripcion,
                lugar = actual.lugar,
                fechaInicio = inicio,
                fechaFin = fin,
                aforoMaximo = aforo,
                latitud = lat,
                longitud = lng
            )) {
                is Resultado.Exito -> {
                    _estado.update { it.copy(guardando = false) }
                    onCreado(resultado.valor)
                }
                is Resultado.Error -> _estado.update {
                    it.copy(guardando = false, mensaje = resultado.mensaje)
                }
            }
        }
    }

    private fun parsearFecha(texto: String): Instant? = try {
        LocalDateTime.parse(texto.trim(), FORMATO).atZone(ZoneId.systemDefault()).toInstant()
    } catch (_: Exception) {
        null
    }

    private companion object {
        val FORMATO: DateTimeFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")
    }
}
