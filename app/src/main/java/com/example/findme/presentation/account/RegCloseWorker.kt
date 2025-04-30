package com.example.findme.presentation.account

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference

class RegCloseWorker(appContext: Context, workerParams: WorkerParameters) : CoroutineWorker(appContext, workerParams) {
    companion object {
        const val LOGIN_FOR_WORKER = "login"
    }

    private val databaseReference: DatabaseReference by lazy { FirebaseDatabase.getInstance().reference.child("Users") }
    private val storageReference: StorageReference by lazy { FirebaseStorage.getInstance().reference.child("Avatars") }

    override suspend fun doWork(): Result {
        val login = inputData.getString("login").toString()

        databaseReference.child(login).removeValue().addOnCompleteListener {
            val oldImageRef = storageReference.child("${login}_avatar")
            oldImageRef.metadata.addOnSuccessListener {
                oldImageRef.delete()
            }
        }

        return Result.success()
    }

}