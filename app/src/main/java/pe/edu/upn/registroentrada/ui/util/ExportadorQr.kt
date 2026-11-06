package pe.edu.upn.registroentrada.ui.util

import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import androidx.core.content.FileProvider
import java.io.File
import java.io.FileOutputStream
import pe.edu.upn.registroentrada.scanner.GeneradorQr

/**
 * Exporta el QR de una entrada como imagen PNG: para compartirla por el
 * sistema (WhatsApp, correo, etc.) o guardarla en la carpeta Descargas.
 */
object ExportadorQr {

    private const val CARPETA = "RegistroEntrada"
    private const val TAMANO = 900

    private fun bitmap(textoQr: String): Bitmap = GeneradorQr.generar(textoQr, TAMANO)

    /** Abre la hoja de compartir del sistema con el PNG del QR. */
    fun compartir(context: Context, textoQr: String, nombreArchivo: String, texto: String) {
        val carpeta = File(context.cacheDir, "qr").apply { mkdirs() }
        val archivo = File(carpeta, nombreArchivo)
        FileOutputStream(archivo).use { salida ->
            bitmap(textoQr).compress(Bitmap.CompressFormat.PNG, 100, salida)
        }
        val uri = FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            archivo
        )
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "image/png"
            putExtra(Intent.EXTRA_STREAM, uri)
            putExtra(Intent.EXTRA_TEXT, texto)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        context.startActivity(Intent.createChooser(intent, "Compartir entrada"))
    }

    /**
     * Guarda el PNG del QR en Descargas. En Android 10+ usa MediaStore (sin
     * permisos); en versiones anteriores usa el almacenamiento propio de la app.
     * Devuelve la ruta legible o null si falló.
     */
    fun guardarEnDescargas(context: Context, textoQr: String, nombreArchivo: String): String? {
        val imagen = bitmap(textoQr)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val valores = ContentValues().apply {
                put(MediaStore.MediaColumns.DISPLAY_NAME, nombreArchivo)
                put(MediaStore.MediaColumns.MIME_TYPE, "image/png")
                put(
                    MediaStore.MediaColumns.RELATIVE_PATH,
                    Environment.DIRECTORY_DOWNLOADS + "/" + CARPETA
                )
                put(MediaStore.MediaColumns.IS_PENDING, 1)
            }
            val resolver = context.contentResolver
            val uri = resolver.insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, valores)
                ?: return null
            resolver.openOutputStream(uri)?.use { salida ->
                imagen.compress(Bitmap.CompressFormat.PNG, 100, salida)
            } ?: return null
            valores.clear()
            valores.put(MediaStore.MediaColumns.IS_PENDING, 0)
            resolver.update(uri, valores, null, null)
            return "Descargas/$CARPETA/$nombreArchivo"
        } else {
            val dir = File(
                context.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS),
                CARPETA
            ).apply { mkdirs() }
            val archivo = File(dir, nombreArchivo)
            FileOutputStream(archivo).use { salida ->
                imagen.compress(Bitmap.CompressFormat.PNG, 100, salida)
            }
            return archivo.absolutePath
        }
    }
}
