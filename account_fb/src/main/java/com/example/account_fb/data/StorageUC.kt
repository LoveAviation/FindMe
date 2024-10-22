package com.example.account_fb.data

import android.content.ContentValues.TAG
import android.net.Uri
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.account_fb.other.ErrorStates
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import javax.inject.Inject

class StorageUC @Inject constructor() {
    private val storageReference: StorageReference by lazy { FirebaseStorage.getInstance().reference.child("Avatars") }

    private val _errorStorage = MutableLiveData<ErrorStates>(ErrorStates.NULL)
    val errorStorage: LiveData<ErrorStates> get() = _errorStorage

    private val _storageState = MutableLiveData<String?>()
    val storageState: LiveData<String?> get() = _storageState

    fun uploadAvatar(imageUri: Uri, login: String) {
        _errorStorage.value = ErrorStates.NULL
        _storageState.value = null
        val imageRef = storageReference.child("${login}_avatar")
        imageRef.putFile(imageUri).addOnSuccessListener {
            imageRef.downloadUrl.addOnSuccessListener { url ->
                _storageState.value = url.toString()
            }.addOnFailureListener{
                _errorStorage.value = ErrorStates.ERROR
            }
        }
    }

    fun changeAvatar(imageUri: Uri, login: String){
        _errorStorage.value = ErrorStates.ERROR
        _storageState.value = null
        val oldImageRef = storageReference.child("${login}_avatar")
        oldImageRef.metadata.addOnSuccessListener {
            oldImageRef.delete().addOnSuccessListener{
                uploadAvatar(imageUri, login)
            }.addOnFailureListener{
                _errorStorage.value = ErrorStates.ERROR
            }
        }.addOnFailureListener{
            uploadAvatar(imageUri, login)
        }
    }

    fun deleteAvatar(login: String){
        _errorStorage.value = ErrorStates.ERROR
        _storageState.value = null
        val oldImageRef = storageReference.child("${login}_avatar")
        oldImageRef.metadata.addOnSuccessListener {
            oldImageRef.delete().addOnSuccessListener{
                _storageState.value = "DELETED"
            }.addOnFailureListener{
                _errorStorage.value = ErrorStates.ERROR
            }
        }.addOnFailureListener{
            _errorStorage.value = ErrorStates.ERROR
        }
    }
}