package pe.edu.upn.registroentrada.data.mapper

import pe.edu.upn.registroentrada.data.local.aInstant
import pe.edu.upn.registroentrada.data.local.aEpochMillis
import pe.edu.upn.registroentrada.data.local.entity.AsistenteEntity
import pe.edu.upn.registroentrada.data.local.entity.EntradaEntity
import pe.edu.upn.registroentrada.data.local.entity.EventoEntity
import pe.edu.upn.registroentrada.data.local.entity.OperadorEntity
import pe.edu.upn.registroentrada.data.local.entity.RegistroAccesoEntity
import pe.edu.upn.registroentrada.data.local.entity.TipoEntradaEntity
import pe.edu.upn.registroentrada.data.local.relation.EntradaDetalleRow
import pe.edu.upn.registroentrada.data.local.relation.RegistroAccesoDetalleRow
import pe.edu.upn.registroentrada.domain.model.Asistente
import pe.edu.upn.registroentrada.domain.model.Entrada
import pe.edu.upn.registroentrada.domain.model.EntradaDetalle
import pe.edu.upn.registroentrada.domain.model.EstadoEntrada
import pe.edu.upn.registroentrada.domain.model.EstadoEvento
import pe.edu.upn.registroentrada.domain.model.Evento
import pe.edu.upn.registroentrada.domain.model.Operador
import pe.edu.upn.registroentrada.domain.model.RegistroAcceso
import pe.edu.upn.registroentrada.domain.model.RegistroAccesoDetalle
import pe.edu.upn.registroentrada.domain.model.ResultadoAcceso
import pe.edu.upn.registroentrada.domain.model.RolOperador
import pe.edu.upn.registroentrada.domain.model.TipoEntrada

/**
 * Conversiones entre entidades de Room y modelos de dominio. Mantienen la
 * base de datos aislada de la interfaz (los repositorios son los únicos que
 * conocen estas funciones).
 */

fun EventoEntity.aDominio() = Evento(
    id = id,
    remoteId = remoteId,
    nombre = nombre,
    descripcion = descripcion,
    lugar = lugar,
    latitud = latitud,
    longitud = longitud,
    fechaInicio = fechaInicio.aInstant(),
    fechaFin = fechaFin.aInstant(),
    aforoMaximo = aforoMaximo,
    estado = EstadoEvento.desde(estado),
    creadoEn = creadoEn.aInstant()
)

fun Evento.aEntidad() = EventoEntity(
    id = id,
    remoteId = remoteId,
    nombre = nombre,
    descripcion = descripcion,
    lugar = lugar,
    latitud = latitud,
    longitud = longitud,
    fechaInicio = fechaInicio.aEpochMillis(),
    fechaFin = fechaFin.aEpochMillis(),
    aforoMaximo = aforoMaximo,
    estado = estado.name,
    creadoEn = creadoEn.aEpochMillis()
)

fun TipoEntradaEntity.aDominio() = TipoEntrada(
    id = id,
    eventoId = eventoId,
    nombre = nombre,
    precio = precio,
    cupo = cupo,
    color = color
)

fun TipoEntrada.aEntidad() = TipoEntradaEntity(
    id = id,
    eventoId = eventoId,
    nombre = nombre,
    precio = precio,
    cupo = cupo,
    color = color
)

fun AsistenteEntity.aDominio() = Asistente(
    id = id,
    documento = documento,
    nombres = nombres,
    apellidos = apellidos,
    correo = correo,
    telefono = telefono,
    creadoEn = creadoEn.aInstant()
)

fun Asistente.aEntidad() = AsistenteEntity(
    id = id,
    documento = documento,
    nombres = nombres,
    apellidos = apellidos,
    correo = correo,
    telefono = telefono,
    creadoEn = creadoEn.aEpochMillis()
)

fun Entrada.aEntidad() = EntradaEntity(
    id = id,
    codigo = codigo,
    firma = firma,
    eventoId = eventoId,
    tipoEntradaId = tipoEntradaId,
    asistenteId = asistenteId,
    estado = estado.name,
    emitidaEn = emitidaEn.aEpochMillis(),
    usadaEn = usadaEn?.aEpochMillis()
)

fun EntradaDetalleRow.aDominio() = EntradaDetalle(
    entrada = Entrada(
        id = entradaId,
        codigo = codigo,
        firma = firma,
        eventoId = eventoId,
        tipoEntradaId = tipoEntradaId,
        asistenteId = asistenteId,
        estado = EstadoEntrada.desde(estado),
        emitidaEn = emitidaEn.aInstant(),
        usadaEn = usadaEn?.aInstant()
    ),
    nombreAsistente = nombreAsistente,
    documentoAsistente = documentoAsistente,
    nombreTipoEntrada = nombreTipoEntrada,
    colorTipoEntrada = colorTipoEntrada,
    nombreEvento = nombreEvento,
    lugarEvento = lugarEvento
)

fun RegistroAcceso.aEntidad() = RegistroAccesoEntity(
    id = id,
    entradaId = entradaId,
    eventoId = eventoId,
    codigoEscaneado = codigoEscaneado,
    resultado = resultado.name,
    puerta = puerta,
    operadorId = operadorId,
    fechaHora = fechaHora.aEpochMillis(),
    observacion = observacion
)

fun RegistroAccesoDetalleRow.aDominio() = RegistroAccesoDetalle(
    registro = RegistroAcceso(
        id = id,
        entradaId = entradaId,
        eventoId = eventoId,
        codigoEscaneado = codigoEscaneado,
        resultado = ResultadoAcceso.desde(resultado),
        puerta = puerta,
        operadorId = operadorId,
        fechaHora = fechaHora.aInstant(),
        observacion = observacion
    ),
    nombreOperador = nombreOperador,
    nombreAsistente = nombreAsistente
)

fun OperadorEntity.aDominio() = Operador(
    id = id,
    nombre = nombre,
    usuario = usuario,
    pinHash = pinHash,
    pinSal = pinSal,
    rol = RolOperador.desde(rol),
    activo = activo,
    creadoEn = creadoEn.aInstant()
)

fun Operador.aEntidad() = OperadorEntity(
    id = id,
    nombre = nombre,
    usuario = usuario,
    pinHash = pinHash,
    pinSal = pinSal,
    rol = rol.name,
    activo = activo,
    creadoEn = creadoEn.aEpochMillis()
)
