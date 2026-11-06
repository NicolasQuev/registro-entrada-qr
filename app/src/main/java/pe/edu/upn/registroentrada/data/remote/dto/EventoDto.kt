package pe.edu.upn.registroentrada.data.remote.dto

import com.google.gson.annotations.SerializedName

/**
 * Representación JSON según el contrato de la API REST de Firestore Documents v1.
 * Permite serializar y deserializar documentos mediante Retrofit sin requerir el SDK pesado de Firebase.
 */
data class FirestoreListResponse<T>(
    @SerializedName("documents") val documents: List<T>? = null,
    @SerializedName("nextPageToken") val nextPageToken: String? = null
)

data class FirestoreValueString(@SerializedName("stringValue") val stringValue: String = "")
data class FirestoreValueInt(@SerializedName("integerValue") val integerValue: String = "0")
data class FirestoreValueDouble(@SerializedName("doubleValue") val doubleValue: Double = 0.0)

data class EventoFieldsDto(
    @SerializedName("remoteId") val remoteId: FirestoreValueString? = null,
    @SerializedName("nombre") val nombre: FirestoreValueString = FirestoreValueString(),
    @SerializedName("descripcion") val descripcion: FirestoreValueString? = null,
    @SerializedName("lugar") val lugar: FirestoreValueString = FirestoreValueString(),
    @SerializedName("latitud") val latitud: FirestoreValueDouble? = null,
    @SerializedName("longitud") val longitud: FirestoreValueDouble? = null,
    @SerializedName("aforoMaximo") val aforoMaximo: FirestoreValueInt = FirestoreValueInt("100"),
    @SerializedName("estado") val estado: FirestoreValueString = FirestoreValueString("PROGRAMADO"),
    @SerializedName("fechaInicio") val fechaInicio: FirestoreValueString? = null,
    @SerializedName("fechaFin") val fechaFin: FirestoreValueString? = null
)

data class EventoDocumentDto(
    @SerializedName("name") val name: String? = null,
    @SerializedName("fields") val fields: EventoFieldsDto = EventoFieldsDto(),
    @SerializedName("createTime") val createTime: String? = null,
    @SerializedName("updateTime") val updateTime: String? = null
) {
    /** Extrae el ID del documento a partir del path de Firestore */
    val documentId: String
        get() = name?.substringAfterLast('/') ?: (fields.remoteId?.stringValue ?: "")
}

data class EventoRequestDto(
    @SerializedName("fields") val fields: EventoFieldsDto
)
