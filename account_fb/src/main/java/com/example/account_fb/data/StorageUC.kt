package com.example.account_fb.data

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.media.ExifInterface
import android.net.Uri
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.account_fb.other.ErrorStates
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import javax.inject.Inject

/**
 * Класс по работе с хранилищем
 *
 * UC - Use Case
 * является Use Case так как настоящая работа с базами данных происходит на сервере
 */

class StorageUC @Inject constructor() {
    private val storageReference: StorageReference by lazy { FirebaseStorage.getInstance().reference.child("Avatars") }

    private val _errorStorage = MutableLiveData<ErrorStates>(ErrorStates.NULL)
    val errorStorage: LiveData<ErrorStates> get() = _errorStorage

    private val _storageState = MutableLiveData<String?>()
    val storageState: LiveData<String?> get() = _storageState

    fun uploadAvatar(context: Context, imageUri: Uri, login: String) {
        _errorStorage.value = ErrorStates.NULL
        _storageState.value = null
        val imageRef = storageReference.child("${login}_avatar")
        imageRef.putStream(ByteArrayInputStream(compressImage(context, imageUri))).addOnSuccessListener {
            imageRef.downloadUrl.addOnSuccessListener { url ->
                _storageState.value = url.toString()
            }.addOnFailureListener{
                _errorStorage.value = ErrorStates.ERROR
            }
        }
    }

    fun changeAvatar(context: Context, imageUri: Uri, login: String){
        _errorStorage.value = ErrorStates.ERROR
        _storageState.value = null
        val oldImageRef = storageReference.child("${login}_avatar")
        oldImageRef.metadata.addOnSuccessListener {
            oldImageRef.delete().addOnSuccessListener{
                uploadAvatar(context, imageUri, login)
            }.addOnFailureListener{
                _errorStorage.value = ErrorStates.ERROR
            }
        }.addOnFailureListener{
            uploadAvatar(context, imageUri, login)
        }
    }

    fun deleteAvatar(login: String){
        _errorStorage.value = ErrorStates.NULL
        _storageState.value = null
        val oldImageRef = storageReference.child("${login}_avatar")
        oldImageRef.metadata.addOnSuccessListener {
            oldImageRef.delete().addOnSuccessListener{
                _storageState.value = "DELETED"
            }.addOnFailureListener{
                _errorStorage.value = ErrorStates.ERROR
            }
        }.addOnFailureListener{
            _storageState.value = "DELETED"
        }
    }

    fun compressImage(context: Context, imageUri: Uri): ByteArray? {
        return try {
            val exifInputStream = context.contentResolver.openInputStream(imageUri)
            val exif = exifInputStream?.let { ExifInterface(it) }
            exifInputStream?.close()

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

            val imageInputStream = context.contentResolver.openInputStream(imageUri)
            val bitmap = BitmapFactory.decodeStream(imageInputStream)
            imageInputStream?.close()

            val rotatedBitmap = if (rotationAngle != 0f) {
                val matrix = Matrix()
                matrix.postRotate(rotationAngle)
                Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
            } else {
                bitmap
            }

            val outputStream = ByteArrayOutputStream()
            rotatedBitmap.compress(Bitmap.CompressFormat.JPEG, 70, outputStream)
            outputStream.toByteArray()
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    fun getAvatar(login: String){
        _errorStorage.value = ErrorStates.NULL
        _storageState.value = null

        val imageRef = storageReference.child("${login}_avatar")
        imageRef.metadata.addOnSuccessListener {
            imageRef.downloadUrl.addOnSuccessListener { url ->
                _storageState.value = url.toString()
            }.addOnFailureListener{
                _errorStorage.value = ErrorStates.ERROR
            }
        }.addOnFailureListener{
            _storageState.value = "EMPTY"
        }
    }

}