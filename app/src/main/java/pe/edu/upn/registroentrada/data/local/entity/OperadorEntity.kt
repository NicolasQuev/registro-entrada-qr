package pe.edu.upn.registroentrada.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

/** Tabla `operadores`: usuarios de la aplicación. El PIN se guarda con sal. */
@Entity(
    tableName = "operadores",
    indices = [Index(value = ["usuario"], unique = true, name = "idx_operadores_usuario")]
)
data class OperadorEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val nombre: String,
    val usuario: String,
    @ColumnInfo(name = "pin_hash") val pinHash: String,
    @ColumnInfo(name = "pin_sal") val pinSal: String,
    val rol: String,
    val activo: Boolean,
    @ColumnInfo(name = "creado_en") val creadoEn: Long
)
