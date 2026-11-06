package pe.edu.upn.registroentrada.data.local

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

/**
 * v2 → v3: se agregan las columnas `remote_id`, `latitud` y `longitud` a la tabla `eventos`
 * para soportar sincronización REST en la nube y geolocalización en mapa.
 */
val Migracion2a3 = object : Migration(2, 3) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("ALTER TABLE `eventos` ADD COLUMN `remote_id` TEXT")
        db.execSQL("ALTER TABLE `eventos` ADD COLUMN `latitud` REAL")
        db.execSQL("ALTER TABLE `eventos` ADD COLUMN `longitud` REAL")
    }
}
