package com.example.findme.presentation.account

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.lifecycle.MutableLiveData
import androidx.work.CoroutineWorker
import androidx.work.ForegroundInfo
import androidx.work.WorkerParameters
import com.example.account_fb.entity.Account
import com.example.account_fb.other.ErrorStates
import kotlinx.coroutines.delay
import com.example.findme.R
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference

class RegCloseWorker(appContext: Context, workerParams: WorkerParameters) : CoroutineWorker(appContext, workerParams) {
    companion object {
        const val CHANNEL_ID = "CHANNEL_ID"
        const val NOTIFICATION_ID = 1

        const val LOGIN_FOR_WORKER = "login"
    }

    private val databaseReference: DatabaseReference by lazy { FirebaseDatabase.getInstance().reference.child("Users") }
    private val storageReference: StorageReference by lazy { FirebaseStorage.getInstance().reference.child("Avatars") }
    private var deletingState = MutableLiveData<Boolean?>(null)


    override suspend fun doWork(): Result {
        setForeground(createForegroundInfo())

        val login = inputData.getString("login").toString()

        databaseReference.child(login).removeValue().addOnCompleteListener {
            val oldImageRef = storageReference.child("${login}_avatar")
            oldImageRef.metadata.addOnSuccessListener {
                oldImageRef.delete().addOnCompleteListener {
                    closeNotification()
                }
            }.addOnFailureListener{
                closeNotification()
            }
        }

        return Result.success()
    }

    private fun createForegroundInfo(): ForegroundInfo {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Registration Close Notification Channel",
                NotificationManager.IMPORTANCE_LOW
            )
            val notificationManager = applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }

        val notification = createNotification()
        return ForegroundInfo(NOTIFICATION_ID, notification)
    }


    private fun createNotification(): Notification {
        return NotificationCompat.Builder(applicationContext, CHANNEL_ID)
            .setContentTitle(R.string.app_name.toString())
            .setContentText("Registration is closing")
            .setSmallIcon(R.mipmap.ic_launcher)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .build()
    }

    private fun closeNotification() {
        val notificationManager = applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.cancel(NOTIFICATION_ID)
    }
}