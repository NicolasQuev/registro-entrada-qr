package pe.edu.upn.registroentrada.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import pe.edu.upn.registroentrada.domain.model.TemaApp

private val SobreAmbar = Color(0xFF241A00)
private val TextoOscuro = Color(0xFFECEEF1)
private val TextoSuaveOscuro = Color(0xFFB4BBC5)
private val BordeOscuroVariante = Color(0xFF2A3038)
private val ContenedorAmbarClaro = Color(0xFFFFE7BF)
private val SobreContenedorAmbar = Color(0xFF3A2500)
private val BordeClaroVariante = Color(0xFFDDE0DC)

private val SenalOscura = ColoresSenal(
    acceso = Verde,
    sobreAcceso = Color(0xFF05210F),
    rechazo = Rojo,
    sobreRechazo = Color(0xFF2A0A08),
    atencion = Ambar,
    sobreAtencion = SobreAmbar
)
private val SenalClara = ColoresSenal(
    acceso = VerdeOscuro,
    sobreAcceso = Blanco,
    rechazo = RojoOscuro,
    sobreRechazo = Blanco,
    atencion = Mostaza,
    sobreAtencion = Blanco
)

private val LocalColoresSenal = staticCompositionLocalOf { SenalOscura }

private val EsquemaOscuro = darkColorScheme(
    primary = Ambar,
    onPrimary = SobreAmbar,
    primaryContainer = AmbarContenedorOscuro,
    onPrimaryContainer = AmbarClaro,
    secondary = Gris,
    onSecondary = Grafito,
    secondaryContainer = GrafitoMedio,
    onSecondaryContainer = Hueso,
    background = Grafito,
    onBackground = TextoOscuro,
    surface = GrafitoAlto,
    onSurface = TextoOscuro,
    surfaceVariant = GrafitoMedio,
    onSurfaceVariant = TextoSuaveOscuro,
    outline = GrafitoBorde,
    outlineVariant = BordeOscuroVariante,
    error = Rojo,
    onError = Color(0xFF2A0A08)
)

private val EsquemaClaro = lightColorScheme(
    primary = AmbarOscuro,
    onPrimary = Blanco,
    primaryContainer = ContenedorAmbarClaro,
    onPrimaryContainer = SobreContenedorAmbar,
    secondary = GrisClaro,
    onSecondary = Blanco,
    secondaryContainer = SuperficieClara,
    onSecondaryContainer = Tinta,
    background = Hueso,
    onBackground = Tinta,
    surface = Blanco,
    onSurface = Tinta,
    surfaceVariant = SuperficieClara,
    onSurfaceVariant = GrisClaro,
    outline = BordeClaro,
    outlineVariant = BordeClaroVariante,
    error = RojoOscuro,
    onError = Blanco
)

private val Formas = Shapes(
    extraSmall = RoundedCornerShape(6.dp),
    small = RoundedCornerShape(10.dp),
    medium = RoundedCornerShape(14.dp),
    large = RoundedCornerShape(20.dp),
    extraLarge = RoundedCornerShape(28.dp)
)

/** Colores de señal del tema activo (acceso / rechazo / atención). */
@Composable
fun coloresSenal(): ColoresSenal = LocalColoresSenal.current

@Composable
fun RegistroEntradaTheme(
    tema: TemaApp = TemaApp.SISTEMA,
    content: @Composable () -> Unit
) {
    val oscuro = when (tema) {
        TemaApp.SISTEMA -> isSystemInDarkTheme()
        TemaApp.CLARO -> false
        TemaApp.OSCURO -> true
    }
    CompositionLocalProvider(LocalColoresSenal provides if (oscuro) SenalOscura else SenalClara) {
        MaterialTheme(
            colorScheme = if (oscuro) EsquemaOscuro else EsquemaClaro,
            typography = Tipografia,
            shapes = Formas,
            content = content
        )
    }
}
