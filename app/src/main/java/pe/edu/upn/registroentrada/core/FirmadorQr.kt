package pe.edu.upn.registroentrada.core

import java.security.MessageDigest
import javax.crypto.Mac
import javax.crypto.spec.SecretKeySpec

/**
 * Contenido decodificado de un código QR de entrada.
 *
 * Formato: `RE|1|<uuid-v4>|<firma-hmac-truncada>` (ver 7.6 del diseño).
 */
data class ContenidoQr(
    val version: Int,
    val codigo: String,
    val firma: String
)

/**
 * Firma y verifica códigos QR con HMAC-SHA256 truncado a 8 bytes (RNF-13).
 * La clave vive solo en el dispositivo, por lo que un QR generado en otro
 * equipo no puede validar.
 */
interface FirmadorQr {

    /** Devuelve la firma (8 bytes en hexadecimal) para un código dado. */
    fun firmar(codigo: String): String

    /** Compara la firma recibida con la recalculada (comparación constante). */
    fun verificar(codigo: String, firma: String): Boolean

    /** Empaqueta un código firmado en el texto que viaja dentro del QR. */
    fun empaquetar(codigo: String): String

    /** Decodifica el texto de un QR; devuelve null si el formato no es válido. */
    fun desempaquetar(texto: String): ContenidoQr?

    companion object {
        const val PREFIJO = "RE"
        const val VERSION = 1
        const val BYTES_FIRMA = 8
    }
}

/** Implementación con HMAC-SHA256 sobre una clave local. */
class FirmadorQrHmac(private val clave: ByteArray) : FirmadorQr {

    override fun firmar(codigo: String): String {
        val mac = Mac.getInstance("HmacSHA256")
        mac.init(SecretKeySpec(clave, "HmacSHA256"))
        val completa = mac.doFinal(codigo.toByteArray(Charsets.UTF_8))
        val truncada = completa.copyOf(FirmadorQr.BYTES_FIRMA)
        return truncada.joinToString(separator = "") { byte -> "%02x".format(byte) }
    }

    override fun verificar(codigo: String, firma: String): Boolean {
        val esperada = firmar(codigo)
        return MessageDigest.isEqual(
            esperada.toByteArray(Charsets.UTF_8),
            firma.toByteArray(Charsets.UTF_8)
        )
    }

    override fun empaquetar(codigo: String): String =
        "${FirmadorQr.PREFIJO}|${FirmadorQr.VERSION}|$codigo|${firmar(codigo)}"

    override fun desempaquetar(texto: String): ContenidoQr? {
        val partes = texto.trim().split("|")
        if (partes.size != 4) return null
        if (partes[0] != FirmadorQr.PREFIJO) return null
        val version = partes[1].toIntOrNull() ?: return null
        if (version != FirmadorQr.VERSION) return null
        val codigo = partes[2].ifBlank { return null }
        val firma = partes[3].ifBlank { return null }
        return ContenidoQr(version = version, codigo = codigo, firma = firma)
    }
}
