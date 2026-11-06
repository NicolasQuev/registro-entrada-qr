package pe.edu.upn.registroentrada.ui.theme

import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.Color

/*
 * Identidad visual "Pase": señalización de puerta y credencial de acceso.
 * Base grafito, un único acento ámbar de señalética y colores de señal
 * (acceso / rechazo / atención) reservados para el resultado de la puerta.
 */

// Base
internal val Grafito = Color(0xFF121418)
internal val GrafitoAlto = Color(0xFF1B1F25)
internal val GrafitoMedio = Color(0xFF242A32)
internal val GrafitoBorde = Color(0xFF343A43)

// Acento
internal val Ambar = Color(0xFFF2B01E)
internal val AmbarClaro = Color(0xFFFFE08A)
internal val AmbarOscuro = Color(0xFF8A5A00)
internal val AmbarContenedorOscuro = Color(0xFF3D2E00)

// Neutros claros
internal val Hueso = Color(0xFFF4F5F3)
internal val Blanco = Color(0xFFFFFFFF)
internal val Tinta = Color(0xFF17191C)
internal val Gris = Color(0xFF9AA3AF)
internal val GrisClaro = Color(0xFF4B5563)
internal val BordeClaro = Color(0xFFC9CDC7)
internal val SuperficieClara = Color(0xFFECEEEB)

// Señal
internal val Verde = Color(0xFF2FBF71)
internal val VerdeOscuro = Color(0xFF0E7C4A)
internal val Rojo = Color(0xFFFF6B61)
internal val RojoOscuro = Color(0xFFB3261E)
internal val Mostaza = Color(0xFF9A6700)

/** Colores de señal que cambian según el tema. */
@Immutable
data class ColoresSenal(
    val acceso: Color,
    val sobreAcceso: Color,
    val rechazo: Color,
    val sobreRechazo: Color,
    val atencion: Color,
    val sobreAtencion: Color
)
