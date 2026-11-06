package pe.edu.upn.registroentrada.scanner

import android.graphics.Bitmap
import android.graphics.Color
import com.google.zxing.BarcodeFormat
import com.google.zxing.EncodeHintType
import com.google.zxing.qrcode.QRCodeWriter
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel

/** Genera el Bitmap del QR que se muestra en el detalle de la entrada (HU-08). */
object GeneradorQr {

    fun generar(texto: String, tamanoPx: Int = 720): Bitmap {
        val pistas = mapOf(
            EncodeHintType.ERROR_CORRECTION to ErrorCorrectionLevel.M,
            EncodeHintType.MARGIN to 1,
            EncodeHintType.CHARACTER_SET to "UTF-8"
        )
        val matriz = QRCodeWriter().encode(texto, BarcodeFormat.QR_CODE, tamanoPx, tamanoPx, pistas)
        val bitmap = Bitmap.createBitmap(matriz.width, matriz.height, Bitmap.Config.ARGB_8888)
        for (x in 0 until matriz.width) {
            for (y in 0 until matriz.height) {
                bitmap.setPixel(x, y, if (matriz[x, y]) Color.BLACK else Color.WHITE)
            }
        }
        return bitmap
    }
}

/**
 * Servicio de escaneo con cámara. Se implementará con CameraX + ML Kit en
 * el Sprint 3 (semana 8); por ahora la validación en puerta se hace con el
 * código manual (HU-15) para no adelantar ese sprint.
 */
interface EscaneadorQr {
    suspend fun escanear(): String?
}
