package pe.edu.upn.registroentrada.data.local

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

/**
 * v1 → v2: en `entradas`, el asistente pasa a ser opcional para permitir
 * entradas genéricas (sin registrar personas). SQLite no permite cambiar la
 * nulabilidad de una columna, así que se recrea la tabla conservando datos.
 */
val Migracion1a2 = object : Migration(1, 2) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL(
            """
            CREATE TABLE IF NOT EXISTS `entradas_new` (
                `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                `codigo` TEXT NOT NULL,
                `firma` TEXT NOT NULL,
                `evento_id` INTEGER NOT NULL,
                `tipo_entrada_id` INTEGER NOT NULL,
                `asistente_id` INTEGER,
                `estado` TEXT NOT NULL,
                `emitida_en` INTEGER NOT NULL,
                `usada_en` INTEGER,
                FOREIGN KEY(`evento_id`) REFERENCES `eventos`(`id`)
                    ON UPDATE NO ACTION ON DELETE CASCADE,
                FOREIGN KEY(`tipo_entrada_id`) REFERENCES `tipos_entrada`(`id`)
                    ON UPDATE NO ACTION ON DELETE RESTRICT,
                FOREIGN KEY(`asistente_id`) REFERENCES `asistentes`(`id`)
                    ON UPDATE NO ACTION ON DELETE CASCADE
            )
            """.trimIndent()
        )
        db.execSQL(
            """
            INSERT INTO entradas_new
                (id, codigo, firma, evento_id, tipo_entrada_id, asistente_id,
                 estado, emitida_en, usada_en)
            SELECT id, codigo, firma, evento_id, tipo_entrada_id, asistente_id,
                   estado, emitida_en, usada_en
            FROM entradas
            """.trimIndent()
        )
        db.execSQL("DROP TABLE `entradas`")
        db.execSQL("ALTER TABLE `entradas_new` RENAME TO `entradas`")
        db.execSQL(
            "CREATE UNIQUE INDEX IF NOT EXISTS `idx_entradas_codigo` ON `entradas` (`codigo`)"
        )
        db.execSQL(
            "CREATE INDEX IF NOT EXISTS `index_entradas_evento_id_estado` " +
                "ON `entradas` (`evento_id`, `estado`)"
        )
        db.execSQL(
            "CREATE INDEX IF NOT EXISTS `index_entradas_tipo_entrada_id` " +
                "ON `entradas` (`tipo_entrada_id`)"
        )
        db.execSQL(
            "CREATE INDEX IF NOT EXISTS `index_entradas_asistente_id` " +
                "ON `entradas` (`asistente_id`)"
        )
    }
}
