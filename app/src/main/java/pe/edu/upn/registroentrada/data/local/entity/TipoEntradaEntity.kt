package pe.edu.upn.registroentrada.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/** Tabla `tipos_entrada`: pertenece a un evento y se borra en cascada. */
@Entity(
    tableName = "tipos_entrada",
    foreignKeys = [
        ForeignKey(
            entity = EventoEntity::class,
            parentColumns = ["id"],
            childColumns = ["evento_id"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["evento_id", "nombre"], unique = true, name = "idx_tipos_evento_nombre"),
        Index(value = ["evento_id"])
    ]
)
data class TipoEntradaEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    @ColumnInfo(name = "evento_id") val eventoId: Long,
    val nombre: String,
    val precio: Double,
    val cupo: Int,
    val color: String
)
