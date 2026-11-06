package pe.edu.upn.registroentrada.domain.model

/** Tema visual elegido por el usuario (HU-23). */
enum class TemaApp {
    SISTEMA,
    CLARO,
    OSCURO;

    companion object {
        fun desde(texto: String): TemaApp =
            entries.firstOrNull { it.name == texto } ?: SISTEMA
    }
}

/**
 * Preferencias de la aplicación y sesión activa, tal como se guardan en
 * DataStore (HT-04). Los datos de negocio no se mezclan aquí (RNF-35).
 */
data class AjustesUsuario(
    val operadorActivoId: Long = SIN_SESION,
    val operadorActivoNombre: String = "",
    val eventoActivoId: Long = SIN_EVENTO,
    val puertaActiva: String = "P1",
    val tema: TemaApp = TemaApp.SISTEMA,
    val sonidoEscaneo: Boolean = true,
    val vibracionEscaneo: Boolean = true,
    val intentosPinFallidos: Int = 0,
    val bloqueoHasta: Long = 0L,
    val ultimoRespaldo: Long = 0L
) {
    val haySesion: Boolean get() = operadorActivoId != SIN_SESION
    val hayEventoActivo: Boolean get() = eventoActivoId != SIN_EVENTO

    companion object {
        const val SIN_SESION = -1L
        const val SIN_EVENTO = -1L
    }
}
