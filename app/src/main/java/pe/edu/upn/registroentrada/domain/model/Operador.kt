package pe.edu.upn.registroentrada.domain.model

import java.time.Instant

/** Rol del operador: controla qué secciones ve y qué puede autorizar (RN-10). */
enum class RolOperador {
    ADMINISTRADOR,
    PORTERO;

    companion object {
        fun desde(texto: String): RolOperador =
            entries.firstOrNull { it.name == texto } ?: PORTERO
    }
}

/** Usuario que opera la aplicación. El PIN se guarda como hash con sal. */
data class Operador(
    val id: Long = 0,
    val nombre: String,
    val usuario: String,
    val pinHash: String,
    val pinSal: String,
    val rol: RolOperador,
    val activo: Boolean = true,
    val creadoEn: Instant
) {
    val esAdministrador: Boolean
        get() = rol == RolOperador.ADMINISTRADOR && activo
}
