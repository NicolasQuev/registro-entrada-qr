package pe.edu.upn.registroentrada.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

/** Tabla `eventos` (ver diccionario de datos 7.4.2). */
@Entity(
    tableName = "eventos",
    indices = [Index(value = ["fecha_inicio"])]
)
data class EventoEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    @ColumnInfo(name = "remote_id") val remoteId: String? = null,
    val nombre: String,
    val descripcion: String?,
    val lugar: String,
    val latitud: Double? = null,
    val longitud: Double? = null,
    @ColumnInfo(name = "fecha_inicio") val fechaInicio: Long,
    @ColumnInfo(name = "fecha_fin") val fechaFin: Long,
    @ColumnInfo(name = "aforo_maximo") val aforoMaximo: Int,
    val estado: String,
    @ColumnInfo(name = "creado_en") val creadoEn: Long
)
