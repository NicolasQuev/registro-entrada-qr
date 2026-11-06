package pe.edu.upn.registroentrada.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import pe.edu.upn.registroentrada.data.local.dao.AsistenteDao
import pe.edu.upn.registroentrada.data.local.dao.EntradaDao
import pe.edu.upn.registroentrada.data.local.dao.EventoDao
import pe.edu.upn.registroentrada.data.local.dao.OperadorDao
import pe.edu.upn.registroentrada.data.local.dao.RegistroAccesoDao
import pe.edu.upn.registroentrada.data.local.dao.TipoEntradaDao
import pe.edu.upn.registroentrada.data.local.entity.AsistenteEntity
import pe.edu.upn.registroentrada.data.local.entity.EntradaEntity
import pe.edu.upn.registroentrada.data.local.entity.EventoEntity
import pe.edu.upn.registroentrada.data.local.entity.OperadorEntity
import pe.edu.upn.registroentrada.data.local.entity.RegistroAccesoEntity
import pe.edu.upn.registroentrada.data.local.entity.TipoEntradaEntity

/**
 * Base de datos local (SQLite mediante Room) con las seis tablas de negocio.
 * El esquema se exporta a `app/schemas/.../1.json` (HT-03).
 */
@Database(
    entities = [
        EventoEntity::class,
        TipoEntradaEntity::class,
        AsistenteEntity::class,
        EntradaEntity::class,
        RegistroAccesoEntity::class,
        OperadorEntity::class
    ],
    version = 3,
    exportSchema = true
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun eventoDao(): EventoDao
    abstract fun tipoEntradaDao(): TipoEntradaDao
    abstract fun asistenteDao(): AsistenteDao
    abstract fun entradaDao(): EntradaDao
    abstract fun registroAccesoDao(): RegistroAccesoDao
    abstract fun operadorDao(): OperadorDao

    companion object {
        const val NOMBRE = "registro_entrada.db"
        val MIGRACIONES = arrayOf(Migracion1a2, Migracion2a3)
    }
}
