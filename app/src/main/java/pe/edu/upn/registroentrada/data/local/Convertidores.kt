package pe.edu.upn.registroentrada.data.local

import java.time.Instant

/**
 * Conversión entre `java.time.Instant` del dominio y el `INTEGER` (epoch
 * millis) que usa SQLite, tal como se documentó en 7.4.2.
 */
fun Instant.aEpochMillis(): Long = toEpochMilli()

fun Long.aInstant(): Instant = Instant.ofEpochMilli(this)
