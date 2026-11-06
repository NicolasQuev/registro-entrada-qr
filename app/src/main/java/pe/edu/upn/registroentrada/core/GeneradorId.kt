package pe.edu.upn.registroentrada.core

import java.util.UUID

/**
 * Generador de identificadores inyectable. En producción crea UUID v4;
 * en las pruebas se puede reemplazar por una secuencia predecible.
 */
fun interface GeneradorId {
    fun nuevo(): String
}

/** Implementación real: UUID versión 4, como exige el formato del QR. */
class GeneradorIdUuid : GeneradorId {
    override fun nuevo(): String = UUID.randomUUID().toString()
}
