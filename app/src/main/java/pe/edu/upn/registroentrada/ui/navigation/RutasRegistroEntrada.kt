package pe.edu.upn.registroentrada.ui.navigation

/**
 * Rutas centralizadas de Navigation Compose. Ninguna pantalla escribe rutas
 * como texto suelto (7.3 del diseño).
 */
object Rutas {
    const val LOGIN = "login"
    const val INICIO = "inicio"
    const val EVENTOS = "eventos"
    const val EVENTO_NUEVO = "evento_nuevo"
    const val EVENTO_DETALLE = "evento/{eventoId}"
    const val EVENTO_MAPA = "evento/{eventoId}/mapa"
    const val ASISTENTES = "asistentes"
    const val ASISTENTE_NUEVO = "asistente_nuevo"
    const val EMITIR = "emitir/{eventoId}"
    const val ENTRADAS = "entradas/{eventoId}"
    const val ENTRADA_DETALLE = "entrada/{entradaId}"
    const val ESCANER = "escaner"
    const val HISTORIAL = "historial"
    const val AJUSTES = "ajustes"

    const val ARG_EVENTO_ID = "eventoId"
    const val ARG_ENTRADA_ID = "entradaId"

    fun eventoDetalle(eventoId: Long) = "evento/$eventoId"
    fun eventoMapa(eventoId: Long) = "evento/$eventoId/mapa"
    fun emitir(eventoId: Long) = "emitir/$eventoId"
    fun entradas(eventoId: Long) = "entradas/$eventoId"
    fun entradaDetalle(entradaId: Long) = "entrada/$entradaId"
}

/** Rutas que muestran la barra inferior principal. */
val RutasConBarraInferior = setOf(
    Rutas.INICIO,
    Rutas.EVENTOS,
    Rutas.ESCANER,
    Rutas.HISTORIAL,
    Rutas.AJUSTES
)
