package pe.edu.upn.registroentrada.domain.usecase

import javax.inject.Inject
import pe.edu.upn.registroentrada.core.Reloj
import pe.edu.upn.registroentrada.core.Resultado
import pe.edu.upn.registroentrada.core.ValidadorDocumento
import pe.edu.upn.registroentrada.domain.model.Asistente
import pe.edu.upn.registroentrada.domain.repository.AsistenteRepository

/**
 * Registra un asistente aplicando las validaciones de HU-05 y evitando
 * documentos duplicados.
 */
class RegistrarAsistenteUseCase @Inject constructor(
    private val asistentes: AsistenteRepository,
    private val reloj: Reloj
) {
    suspend operator fun invoke(
        documento: String,
        nombres: String,
        apellidos: String,
        correo: String?,
        telefono: String?
    ): Resultado<Long> {
        ValidadorDocumento.errorDocumento(documento)?.let { return Resultado.Error(it) }
        if (nombres.isBlank()) return Resultado.Error("Los nombres son obligatorios")
        if (apellidos.isBlank()) return Resultado.Error("Los apellidos son obligatorios")
        ValidadorDocumento.errorCorreo(correo.orEmpty())?.let { return Resultado.Error(it) }
        ValidadorDocumento.errorTelefono(telefono.orEmpty())?.let { return Resultado.Error(it) }

        val existente = asistentes.buscarPorDocumento(documento.trim())
        if (existente != null) {
            return Resultado.Error("Ese documento ya está registrado para ${existente.nombreCompleto}")
        }

        val id = asistentes.guardar(
            Asistente(
                documento = documento.trim(),
                nombres = nombres.trim(),
                apellidos = apellidos.trim(),
                correo = correo?.trim()?.ifBlank { null },
                telefono = telefono?.trim()?.ifBlank { null },
                creadoEn = reloj.ahora()
            )
        )
        return Resultado.Exito(id)
    }
}
