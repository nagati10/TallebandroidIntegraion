// File: util/FileUtils.kt
package sim2.app.talleb_5edma.util

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.InputStream

object FileUtils {

    // Convert Uri to ByteArray
    fun uriToByteArray(context: Context, uri: Uri): ByteArray? {
        return try {
            context.contentResolver.openInputStream(uri)?.use { inputStream ->
                inputStream.readBytes()
            }
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    // Get filename from Uri
    fun getFileName(context: Context, uri: Uri): String {
        return try {
            val cursor = context.contentResolver.query(uri, null, null, null, null)
            cursor?.use {
                if (it.moveToFirst()) {
                    val displayName = it.getString(it.getColumnIndexOrThrow("_display_name"))
                    if (!displayName.isNullOrEmpty()) {
                        return displayName
                    }
                }
            }
            // Fallback to timestamp-based name
            "profile_${System.currentTimeMillis()}.jpg"
        } catch (e: Exception) {
            "profile_${System.currentTimeMillis()}.jpg"
        }
    }

    // Convert Uri to Bitmap for preview
    fun uriToBitmap(context: Context, uri: Uri): Bitmap? {
        return try {
            context.contentResolver.openInputStream(uri)?.use { inputStream ->
                BitmapFactory.decodeStream(inputStream)
            }
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    // Compress bitmap to ByteArray
    fun bitmapToByteArray(bitmap: Bitmap, quality: Int = 80): ByteArray {
        val outputStream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, quality, outputStream)
        return outputStream.toByteArray()
    }

    // Create temporary file from Uri
    fun createTempFileFromUri(context: Context, uri: Uri): File? {
        return try {
            val inputStream: InputStream? = context.contentResolver.openInputStream(uri)
            inputStream?.use { stream ->
                val file = File.createTempFile("upload_", ".jpg", context.cacheDir)
                file.outputStream().use { outputStream ->
                    stream.copyTo(outputStream)
                }
                file
            }
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}