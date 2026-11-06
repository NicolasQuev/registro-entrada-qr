package pe.edu.upn.registroentrada.core

import java.time.Instant

/**
 * Reloj inyectable. Permite fijar el tiempo en las pruebas unitarias del
 * dominio sin depender del reloj del sistema (ver 7.8 del diseño).
 */
fun interface Reloj {
    fun ahora(): Instant
}

/** Implementación real basada en el reloj del sistema. */
class RelojSistema : Reloj {
    override fun ahora(): Instant = Instant.now()
}
