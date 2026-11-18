package com.example.hostelmanager.util

import android.content.Context
import android.net.Uri
import androidx.core.content.FileProvider
import java.io.File
import java.util.UUID

object ImageStore {

    private const val IMAGES_DIR = "images"

    // Gives the camera app a temporary file (via FileProvider) to write the photo into.
    fun createTempCaptureUri(context: Context): Uri {
        val tempFile = File(context.cacheDir, "capture_${System.currentTimeMillis()}.jpg")
        return FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            tempFile
        )
    }

    // Copies a photo (from the camera's temp file OR a gallery pick) into permanent
    // app-private storage, then returns the saved file's full path.
    fun saveImage(context: Context, sourceUri: Uri): String {
        val imagesDir = File(context.filesDir, IMAGES_DIR)
        if (!imagesDir.exists()) imagesDir.mkdirs()

        val destFile = File(imagesDir, "img_${System.currentTimeMillis()}_${UUID.randomUUID()}.jpg")

        context.contentResolver.openInputStream(sourceUri)?.use { input ->
            destFile.outputStream().use { output ->
                input.copyTo(output)
            }
        }

        return destFile.absolutePath
    }
}