package com.brainwallet.util

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Environment
import java.io.File
import java.io.FileOutputStream
import java.net.URI

interface ImageStorage {
    fun saveImage(data: ByteArray, timestamp: Long): URI
    fun deleteImage(path: URI): Boolean
}

class ImageHelper(
    public val context: Context
) : ImageStorage {

    override fun saveImage(data: ByteArray, timestamp: Long): URI {
        val bitmap = BitmapFactory.decodeByteArray(data, 0, data.size)
            ?: throw error("Failed to decode image data")

        val directory = context.getExternalFilesDir(Environment.DIRECTORY_PICTURES)
            ?: throw error("External storage is unavailable")

        val file = File(directory, "$timestamp.png")

        FileOutputStream(file).use { fos ->
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, fos)
        }
        return URI.create(file.absolutePath)
    }

    override fun deleteImage(path: URI): Boolean {
        return try {
            val file = File(path)
            when {
                !file.exists() -> false
                !file.isFile -> false
                else -> file.delete()
            }
        } catch (e: Exception) {
            false
        }
    }
}
