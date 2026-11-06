package pe.edu.upn.registroentrada.core

import java.security.MessageDigest
import java.security.SecureRandom

/**
 * Utilidades de hashing para credenciales. El PIN nunca se guarda en texto
 * plano: se guarda SHA-256 del PIN más una sal aleatoria por operador
 * (RNF-12, RN-15).
 */
object Hasher {

    private val regexHex = Regex("^[0-9a-fA-F]+$")

    /** Genera una sal aleatoria de 16 bytes en hexadecimal. */
    fun generarSal(): String {
        val bytes = ByteArray(16)
        SecureRandom().nextBytes(bytes)
        return bytes.joinToString(separator = "") { byte -> "%02x".format(byte) }
    }

    /** Calcula SHA-256 de `pin + sal` y lo devuelve en hexadecimal. */
    fun pinHash(pin: String, sal: String): String =
        sha256(pin + sal)

    /** Verifica un PIN contra el hash y la sal almacenados. */
    fun verificarPin(pin: String, sal: String, hashAlmacenado: String): Boolean {
        val calculado = pinHash(pin, sal)
        return MessageDigest.isEqual(
            calculado.toByteArray(Charsets.UTF_8),
            hashAlmacenado.toByteArray(Charsets.UTF_8)
        )
    }

    /** SHA-256 de un texto, en hexadecimal minúscula. */
    fun sha256(texto: String): String {
        val digest = MessageDigest.getInstance("SHA-256")
        val bytes = digest.digest(texto.toByteArray(Charsets.UTF_8))
        return bytes.joinToString(separator = "") { byte -> "%02x".format(byte) }
    }

    /** Indica si un texto tiene forma de hash hexadecimal. */
    fun pareceHash(texto: String): Boolean =
        texto.isNotBlank() && texto.length % 2 == 0 && regexHex.matches(texto)
}
