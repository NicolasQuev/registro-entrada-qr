package pe.edu.upn.registroentrada.data.repository

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import java.io.IOException
import java.time.Instant
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import pe.edu.upn.registroentrada.data.preferences.ClavesAjustes
import pe.edu.upn.registroentrada.domain.model.AjustesUsuario
import pe.edu.upn.registroentrada.domain.model.TemaApp
import pe.edu.upn.registroentrada.domain.repository.AjustesRepository

/**
 * Implementa las preferencias y la sesión con DataStore (HT-04). Si la
 * lectura falla se emiten los valores por defecto en lugar de propagar el
 * error, tal como exige el criterio de aceptación de HT-04.
 */
@Singleton
class AjustesRepositoryImpl @Inject constructor(
    private val dataStore: DataStore<Preferences>
) : AjustesRepository {

    override val ajustes: Flow<AjustesUsuario> = dataStore.data
        .catch { causa ->
            if (causa is IOException) emit(emptyPreferences()) else throw causa
        }
        .map { preferencias ->
            AjustesUsuario(
                operadorActivoId = preferencias[ClavesAjustes.OPERADOR_ACTIVO_ID]
                    ?: AjustesUsuario.SIN_SESION,
                operadorActivoNombre = preferencias[ClavesAjustes.OPERADOR_ACTIVO_NOMBRE].orEmpty(),
                eventoActivoId = preferencias[ClavesAjustes.EVENTO_ACTIVO_ID]
                    ?: AjustesUsuario.SIN_EVENTO,
                puertaActiva = preferencias[ClavesAjustes.PUERTA_ACTIVA] ?: "P1",
                tema = TemaApp.desde(preferencias[ClavesAjustes.TEMA_APP] ?: TemaApp.SISTEMA.name),
                sonidoEscaneo = preferencias[ClavesAjustes.SONIDO_ESCANEO] ?: true,
                vibracionEscaneo = preferencias[ClavesAjustes.VIBRACION_ESCANEO] ?: true,
                intentosPinFallidos = preferencias[ClavesAjustes.INTENTOS_PIN_FALLIDOS] ?: 0,
                bloqueoHasta = preferencias[ClavesAjustes.BLOQUEO_HASTA] ?: 0L,
                ultimoRespaldo = preferencias[ClavesAjustes.ULTIMO_RESPALDO] ?: 0L
            )
        }

    override suspend fun guardarOperadorActivo(id: Long, nombre: String) {
        dataStore.edit {
            it[ClavesAjustes.OPERADOR_ACTIVO_ID] = id
            it[ClavesAjustes.OPERADOR_ACTIVO_NOMBRE] = nombre
        }
    }

    override suspend fun limpiarOperadorActivo() {
        dataStore.edit {
            it[ClavesAjustes.OPERADOR_ACTIVO_ID] = AjustesUsuario.SIN_SESION
            it[ClavesAjustes.OPERADOR_ACTIVO_NOMBRE] = ""
        }
    }

    override suspend fun guardarEventoActivo(eventoId: Long, puerta: String) {
        dataStore.edit {
            it[ClavesAjustes.EVENTO_ACTIVO_ID] = eventoId
            it[ClavesAjustes.PUERTA_ACTIVA] = puerta
        }
    }

    override suspend fun guardarTema(tema: TemaApp) {
        dataStore.edit { it[ClavesAjustes.TEMA_APP] = tema.name }
    }

    override suspend fun guardarSonido(activo: Boolean) {
        dataStore.edit { it[ClavesAjustes.SONIDO_ESCANEO] = activo }
    }

    override suspend fun guardarVibracion(activa: Boolean) {
        dataStore.edit { it[ClavesAjustes.VIBRACION_ESCANEO] = activa }
    }

    override suspend fun registrarIntentoFallido(): Int {
        var total = 0
        dataStore.edit { preferencias ->
            total = (preferencias[ClavesAjustes.INTENTOS_PIN_FALLIDOS] ?: 0) + 1
            preferencias[ClavesAjustes.INTENTOS_PIN_FALLIDOS] = total
        }
        return total
    }

    override suspend fun reiniciarIntentos() {
        dataStore.edit {
            it[ClavesAjustes.INTENTOS_PIN_FALLIDOS] = 0
            it[ClavesAjustes.BLOQUEO_HASTA] = 0L
        }
    }

    override suspend fun bloquearHasta(momento: Instant) {
        dataStore.edit { it[ClavesAjustes.BLOQUEO_HASTA] = momento.toEpochMilli() }
    }

    override suspend fun marcarRespaldo(momento: Instant) {
        dataStore.edit { it[ClavesAjustes.ULTIMO_RESPALDO] = momento.toEpochMilli() }
    }
}
