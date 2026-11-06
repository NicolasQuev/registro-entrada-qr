package pe.edu.upn.registroentrada.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/** Tabla `registros_acceso`: auditoría inmutable de cada escaneo (RNF-18). */
@Entity(
    tableName = "registros_acceso",
    foreignKeys = [
        ForeignKey(
            entity = EntradaEntity::class,
            parentColumns = ["id"],
            childColumns = ["entrada_id"],
            onDelete = ForeignKey.SET_NULL
        ),
        ForeignKey(
            entity = EventoEntity::class,
            parentColumns = ["id"],
            childColumns = ["evento_id"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = OperadorEntity::class,
            parentColumns = ["id"],
            childColumns = ["operador_id"],
            onDelete = ForeignKey.RESTRICT
        )
    ],
    indices = [
        Index(value = ["evento_id", "fecha_hora"], name = "idx_accesos_evento_fecha"),
        Index(value = ["entrada_id"]),
        Index(value = ["operador_id"])
    ]
)
data class RegistroAccesoEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    @ColumnInfo(name = "entrada_id") val entradaId: Long?,
    @ColumnInfo(name = "evento_id") val eventoId: Long,
    @ColumnInfo(name = "codigo_escaneado") val codigoEscaneado: String,
    val resultado: String,
    val puerta: String,
    @ColumnInfo(name = "operador_id") val operadorId: Long,
    @ColumnInfo(name = "fecha_hora") val fechaHora: Long,
    val observacion: String?
)
