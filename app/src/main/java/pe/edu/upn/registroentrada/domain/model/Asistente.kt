package pe.edu.upn.registroentrada.domain.model

import java.time.Instant

/** Persona registrada a la que se le pueden emitir entradas (datos mínimos). */
data class Asistente(
    val id: Long = 0,
    val documento: String,
    val nombres: String,
    val apellidos: String,
    val correo: String? = null,
    val telefono: String? = null,
    val creadoEn: Instant
) {
    val nombreCompleto: String
        get() = "$nombres $apellidos".trim()
}
