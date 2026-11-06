package pe.edu.upn.registroentrada.data.mapper

import pe.edu.upn.registroentrada.data.remote.dto.EventoDocumentDto
import pe.edu.upn.registroentrada.data.remote.dto.EventoFieldsDto
import pe.edu.upn.registroentrada.data.remote.dto.EventoRequestDto
import pe.edu.upn.registroentrada.data.remote.dto.FirestoreValueDouble
import pe.edu.upn.registroentrada.data.remote.dto.FirestoreValueInt
import pe.edu.upn.registroentrada.data.remote.dto.FirestoreValueString
import pe.edu.upn.registroentrada.domain.model.EstadoEvento
import pe.edu.upn.registroentrada.domain.model.Evento
import java.time.Instant

fun EventoDocumentDto.aDominio(idLocal: Long = 0): Evento {
    val f = fields
    val inicio = f.fechaInicio?.stringValue?.let { runCatching { Instant.parse(it) }.getOrNull() } ?: Instant.now()
    val fin = f.fechaFin?.stringValue?.let { runCatching { Instant.parse(it) }.getOrNull() } ?: inicio.plusSeconds(14400)
    val aforo = f.aforoMaximo.integerValue.toIntOrNull() ?: 100

    return Evento(
        id = idLocal,
        remoteId = documentId,
        nombre = f.nombre.stringValue,
        descripcion = f.descripcion?.stringValue,
        lugar = f.lugar.stringValue,
        latitud = f.latitud?.doubleValue,
        longitud = f.longitud?.doubleValue,
        fechaInicio = inicio,
        fechaFin = fin,
        aforoMaximo = aforo,
        estado = EstadoEvento.desde(f.estado.stringValue),
        creadoEn = createTime?.let { runCatching { Instant.parse(it) }.getOrNull() } ?: Instant.now()
    )
}

fun Evento.aRequestDto(): EventoRequestDto {
    return EventoRequestDto(
        fields = EventoFieldsDto(
            remoteId = remoteId?.let { FirestoreValueString(it) },
            nombre = FirestoreValueString(nombre),
            descripcion = descripcion?.let { FirestoreValueString(it) },
            lugar = FirestoreValueString(lugar),
            latitud = latitud?.let { FirestoreValueDouble(it) },
            longitud = longitud?.let { FirestoreValueDouble(it) },
            aforoMaximo = FirestoreValueInt(aforoMaximo.toString()),
            estado = FirestoreValueString(estado.name),
            fechaInicio = FirestoreValueString(fechaInicio.toString()),
            fechaFin = FirestoreValueString(fechaFin.toString())
        )
    )
}
