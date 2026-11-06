package pe.edu.upn.registroentrada.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/** Tabla `entradas`: cada entrada emitida (el QR es su código firmado). */
@Entity(
    tableName = "entradas",
    foreignKeys = [
        ForeignKey(
            entity = EventoEntity::class,
            parentColumns = ["id"],
            childColumns = ["evento_id"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = TipoEntradaEntity::class,
            parentColumns = ["id"],
            childColumns = ["tipo_entrada_id"],
            onDelete = ForeignKey.RESTRICT
        ),
        ForeignKey(
            entity = AsistenteEntity::class,
            parentColumns = ["id"],
            childColumns = ["asistente_id"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["codigo"], unique = true, name = "idx_entradas_codigo"),
        Index(value = ["evento_id", "estado"]),
        Index(value = ["tipo_entrada_id"]),
        Index(value = ["asistente_id"])
    ]
)
data class EntradaEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val codigo: String,
    val firma: String,
    @ColumnInfo(name = "evento_id") val eventoId: Long,
    @ColumnInfo(name = "tipo_entrada_id") val tipoEntradaId: Long,
    @ColumnInfo(name = "asistente_id") val asistenteId: Long?,
    val estado: String,
    @ColumnInfo(name = "emitida_en") val emitidaEn: Long,
    @ColumnInfo(name = "usada_en") val usadaEn: Long?
)
