package pe.edu.upn.registroentrada.ui.util

import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

/** Formato de fechas y valores para la interfaz (español, Perú). */
object Formato {

    private val LOCALE_PE: Locale = Locale.forLanguageTag("es-PE")

    private val formatoFechaHora: DateTimeFormatter =
        DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm", LOCALE_PE)

    private val formatoHora: DateTimeFormatter =
        DateTimeFormatter.ofPattern("HH:mm", LOCALE_PE)

    private val formatoFecha: DateTimeFormatter =
        DateTimeFormatter.ofPattern("dd/MM/yyyy", LOCALE_PE)

    fun fechaHora(instant: Instant): String =
        formatoFechaHora.format(instant.atZone(ZoneId.systemDefault()))

    fun fecha(instant: Instant): String =
        formatoFecha.format(instant.atZone(ZoneId.systemDefault()))

    fun hora(instant: Instant): String =
        formatoHora.format(instant.atZone(ZoneId.systemDefault()))

    fun moneda(valor: Double): String = "S/ %.2f".format(LOCALE_PE, valor)
}
