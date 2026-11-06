package pe.edu.upn.registroentrada.data.preferences

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore

/**
 * Único DataStore de la aplicación (HT-04). No se debe crear más de una
 * instancia, por eso la extensión vive a nivel de archivo.
 */
val Context.ajustesDataStore: DataStore<Preferences> by preferencesDataStore(name = "ajustes")

/** Nombres de las claves de preferencias (7.4.3). */
object ClavesAjustes {
    val OPERADOR_ACTIVO_ID = longPreferencesKey("operador_activo_id")
    val OPERADOR_ACTIVO_NOMBRE = stringPreferencesKey("operador_activo_nombre")
    val EVENTO_ACTIVO_ID = longPreferencesKey("evento_activo_id")
    val PUERTA_ACTIVA = stringPreferencesKey("puerta_activa")
    val TEMA_APP = stringPreferencesKey("tema_app")
    val SONIDO_ESCANEO = booleanPreferencesKey("sonido_escaneo")
    val VIBRACION_ESCANEO = booleanPreferencesKey("vibracion_escaneo")
    val INTENTOS_PIN_FALLIDOS = intPreferencesKey("intentos_pin_fallidos")
    val BLOQUEO_HASTA = longPreferencesKey("bloqueo_hasta")
    val ULTIMO_RESPALDO = longPreferencesKey("ultimo_respaldo")
}
