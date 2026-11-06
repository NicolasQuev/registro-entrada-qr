package pe.edu.upn.registroentrada.data.local.seed

import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import pe.edu.upn.registroentrada.core.Hasher
import pe.edu.upn.registroentrada.core.Reloj

/**
 * Siembra un operador administrador en el primer arranque para poder iniciar
 * sesión (usuario `admin`, PIN `1234`). El PIN se guarda hasheado con sal.
 *
 * Se usa SQL directo porque el callback corre antes de que Room entregue los
 * DAO. El usuario debe cambiar el PIN desde Ajustes en un uso real.
 */
class DatosIniciales(private val reloj: Reloj) : RoomDatabase.Callback() {

    override fun onCreate(db: SupportSQLiteDatabase) {
        super.onCreate(db)
        val sal = Hasher.generarSal()
        val hash = Hasher.pinHash(PIN_INICIAL, sal)
        db.execSQL(
            "INSERT INTO operadores (nombre, usuario, pin_hash, pin_sal, rol, activo, creado_en) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?)",
            arrayOf(
                "Administrador",
                USUARIO_INICIAL,
                hash,
                sal,
                "ADMINISTRADOR",
                1,
                reloj.ahora().toEpochMilli()
            )
        )
    }

    companion object {
        const val USUARIO_INICIAL = "admin"
        const val PIN_INICIAL = "1234"
    }
}
