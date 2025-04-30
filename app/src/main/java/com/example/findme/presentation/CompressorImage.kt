package com.example.findme.presentation

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.media.ExifInterface
import android.net.Uri
import java.io.ByteArrayOutputStream

/**
 * Сжимает изображения. После него использовать "ByteArrayInputStream(result)"
 */
class CompressorImage {

    fun compressImage(context: Context, imageUri: Uri): ByteArray? {
        return try {
            // Открываем поток, чтобы сначала вытащить EXIF
            val exifInputStream = context.contentResolver.openInputStream(imageUri)
            val exif = exifInputStream?.let { ExifInterface(it) }
            exifInputStream?.close()

            // Получаем угол поворота из EXIF
            val orientation = exif?.getAttributeInt(
                ExifInterface.TAG_ORIENTATION,
                ExifInterface.ORIENTATION_NORMAL
            ) ?: ExifInterface.ORIENTATION_NORMAL

            val rotationAngle = when (orientation) {
                ExifInterface.ORIENTATION_ROTATE_90 -> 90f
                ExifInterface.ORIENTATION_ROTATE_180 -> 180f
                ExifInterface.ORIENTATION_ROTATE_270 -> 270f
                else -> 0f
            }

            // Открываем поток заново для Bitmap
            val imageInputStream = context.contentResolver.openInputStream(imageUri)
            val bitmap = BitmapFactory.decodeStream(imageInputStream)
            imageInputStream?.close()

            // Крутим если надо
            val rotatedBitmap = if (rotationAngle != 0f) {
                val matrix = Matrix()
                matrix.postRotate(rotationAngle)
                Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
            } else {
                bitmap
            }

            // Сжимаем
            val outputStream = ByteArrayOutputStream()
            rotatedBitmap.compress(Bitmap.CompressFormat.JPEG, 70, outputStream)
            outputStream.toByteArray()
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

}