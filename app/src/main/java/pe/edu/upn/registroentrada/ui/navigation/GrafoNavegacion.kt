package pe.edu.upn.registroentrada.ui.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Event
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import pe.edu.upn.registroentrada.ui.screens.ajustes.AjustesScreen
import pe.edu.upn.registroentrada.ui.screens.asistentes.AsistenteFormScreen
import pe.edu.upn.registroentrada.ui.screens.asistentes.AsistentesScreen
import pe.edu.upn.registroentrada.ui.screens.entradas.EmitirEntradaScreen
import pe.edu.upn.registroentrada.ui.screens.entradas.EntradaDetalleScreen
import pe.edu.upn.registroentrada.ui.screens.entradas.EntradasScreen
import pe.edu.upn.registroentrada.ui.screens.escaner.EscanerScreen
import pe.edu.upn.registroentrada.ui.screens.eventos.EventoDetalleScreen
import pe.edu.upn.registroentrada.ui.screens.eventos.EventoFormScreen
import pe.edu.upn.registroentrada.ui.screens.eventos.EventosScreen
import pe.edu.upn.registroentrada.ui.screens.historial.HistorialScreen
import pe.edu.upn.registroentrada.ui.screens.inicio.InicioScreen
import pe.edu.upn.registroentrada.ui.screens.login.LoginScreen

private data class DestinoBarra(
    val ruta: String,
    val etiqueta: String,
    val icono: ImageVector
)

private val destinosBarra = listOf(
    DestinoBarra(Rutas.INICIO, "Inicio", Icons.Filled.Home),
    DestinoBarra(Rutas.EVENTOS, "Eventos", Icons.Filled.Event),
    DestinoBarra(Rutas.ESCANER, "Escanear", Icons.Filled.QrCodeScanner),
    DestinoBarra(Rutas.HISTORIAL, "Historial", Icons.Filled.History),
    DestinoBarra(Rutas.AJUSTES, "Ajustes", Icons.Filled.Settings)
)

@Composable
fun GrafoNavegacion(haySesion: Boolean) {
    val navController = rememberNavController()
    val entradaActual by navController.currentBackStackEntryAsState()
    val rutaActual = entradaActual?.destination?.route

    LaunchedEffect(haySesion) {
        if (haySesion && rutaActual == Rutas.LOGIN) {
            navController.navigate(Rutas.INICIO) {
                popUpTo(Rutas.LOGIN) { inclusive = true }
            }
        }
    }

    Scaffold(
        bottomBar = {
            if (rutaActual in RutasConBarraInferior) {
                NavigationBar {
                    destinosBarra.forEach { destino ->
                        NavigationBarItem(
                            selected = rutaActual == destino.ruta,
                            onClick = {
                                if (rutaActual != destino.ruta) {
                                    navController.navigate(destino.ruta) {
                                        popUpTo(Rutas.INICIO)
                                        launchSingleTop = true
                                    }
                                }
                            },
                            icon = { Icon(destino.icono, contentDescription = destino.etiqueta) },
                            label = { Text(destino.etiqueta) }
                        )
                    }
                }
            }
        }
    ) { relleno ->
        NavHost(
            navController = navController,
            startDestination = if (haySesion) Rutas.INICIO else Rutas.LOGIN,
            modifier = Modifier.padding(relleno)
        ) {
            composable(Rutas.LOGIN) {
                LoginScreen()
            }

            composable(Rutas.INICIO) {
                InicioScreen(
                    onEscanear = { navController.navigate(Rutas.ESCANER) },
                    onHistorial = { navController.navigate(Rutas.HISTORIAL) },
                    onAsistentes = { navController.navigate(Rutas.ASISTENTES) },
                    onNuevoEvento = { navController.navigate(Rutas.EVENTO_NUEVO) },
                    onVerEntradas = { eventoId -> navController.navigate(Rutas.entradas(eventoId)) }
                )
            }

            composable(Rutas.EVENTOS) {
                EventosScreen(
                    onNuevoEvento = { navController.navigate(Rutas.EVENTO_NUEVO) },
                    onEvento = { id -> navController.navigate(Rutas.eventoDetalle(id)) }
                )
            }

            composable(Rutas.EVENTO_NUEVO) {
                EventoFormScreen(
                    onVolver = { navController.popBackStack() },
                    onCreado = { id ->
                        navController.navigate(Rutas.eventoDetalle(id)) {
                            popUpTo(Rutas.EVENTOS)
                        }
                    }
                )
            }

            composable(
                route = Rutas.EVENTO_DETALLE,
                arguments = listOf(navArgument(Rutas.ARG_EVENTO_ID) { type = NavType.LongType })
            ) {
                EventoDetalleScreen(
                    onVolver = { navController.popBackStack() },
                    onEmitirEntrada = { id -> navController.navigate(Rutas.emitir(id)) },
                    onVerEntradas = { id -> navController.navigate(Rutas.entradas(id)) },
                    onVerMapa = { id -> navController.navigate(Rutas.eventoMapa(id)) }
                )
            }

            composable(
                route = Rutas.EVENTO_MAPA,
                arguments = listOf(navArgument(Rutas.ARG_EVENTO_ID) { type = NavType.LongType })
            ) {
                val vm: pe.edu.upn.registroentrada.ui.screens.eventos.EventoDetalleViewModel = hiltViewModel()
                val estado by vm.estado.collectAsStateWithLifecycle()
                val evento = estado.evento
                if (evento != null) {
                    pe.edu.upn.registroentrada.ui.screens.mapa.MapaEventoScreen(
                        evento = evento,
                        onVolver = { navController.popBackStack() }
                    )
                }
            }

            composable(Rutas.ASISTENTES) {
                AsistentesScreen(
                    onVolver = { navController.popBackStack() },
                    onNuevoAsistente = { navController.navigate(Rutas.ASISTENTE_NUEVO) }
                )
            }

            composable(Rutas.ASISTENTE_NUEVO) {
                AsistenteFormScreen(
                    onVolver = { navController.popBackStack() },
                    onCreado = { navController.popBackStack() }
                )
            }

            composable(
                route = Rutas.EMITIR,
                arguments = listOf(navArgument(Rutas.ARG_EVENTO_ID) { type = NavType.LongType })
            ) {
                EmitirEntradaScreen(
                    onVolver = { navController.popBackStack() },
                    onEmitidas = { eventoId ->
                        navController.navigate(Rutas.entradas(eventoId)) {
                            popUpTo(Rutas.EVENTO_DETALLE) { inclusive = true }
                        }
                    }
                )
            }

            composable(
                route = Rutas.ENTRADAS,
                arguments = listOf(navArgument(Rutas.ARG_EVENTO_ID) { type = NavType.LongType })
            ) {
                EntradasScreen(
                    onVolver = { navController.popBackStack() },
                    onEntrada = { id -> navController.navigate(Rutas.entradaDetalle(id)) }
                )
            }

            composable(
                route = Rutas.ENTRADA_DETALLE,
                arguments = listOf(navArgument(Rutas.ARG_ENTRADA_ID) { type = NavType.LongType })
            ) {
                EntradaDetalleScreen(onVolver = { navController.popBackStack() })
            }

            composable(Rutas.ESCANER) {
                EscanerScreen()
            }

            composable(Rutas.HISTORIAL) {
                HistorialScreen()
            }

            composable(Rutas.AJUSTES) {
                AjustesScreen(
                    onSesionCerrada = {
                        navController.navigate(Rutas.LOGIN) {
                            popUpTo(Rutas.INICIO) { inclusive = true }
                        }
                    }
                )
            }
        }
    }
}
