package com.example.findme.domain

import android.net.Uri
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import javax.inject.Inject

class StorageUC @Inject constructor() {
    private val storageReference: StorageReference by lazy { FirebaseStorage.getInstance().reference.child("Avatars") }

    private val _storageState = MutableLiveData<String?>()
    val storageState: LiveData<String?> get() = _storageState

    fun uploadAvatar(imageUri: Uri, login: String) {
        _storageState.value = null
        val imageRef = storageReference.child("${login}_avatar")
        imageRef.putFile(imageUri).addOnSuccessListener {
            imageRef.downloadUrl.addOnSuccessListener { url ->
                _storageState.value = url.toString()
            }
        }
    }
}