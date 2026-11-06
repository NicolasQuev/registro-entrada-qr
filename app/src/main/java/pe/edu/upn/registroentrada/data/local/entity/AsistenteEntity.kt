package pe.edu.upn.registroentrada.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

/** Tabla `asistentes`: datos mínimos de la persona, documento único. */
@Entity(
    tableName = "asistentes",
    indices = [Index(value = ["documento"], unique = true, name = "idx_asistentes_documento")]
)
data class AsistenteEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val documento: String,
    val nombres: String,
    val apellidos: String,
    val correo: String?,
    val telefono: String?,
    @ColumnInfo(name = "creado_en") val creadoEn: Long
)
