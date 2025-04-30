package com.example.findme.presentation.account

import android.content.Context
import android.content.Intent
import android.os.Handler
import android.os.Looper
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.findme.presentation.MainActivity
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.postgrest.from
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlin.getValue

class DelContinueWorker(appContext: Context, workerParams: WorkerParameters) : CoroutineWorker(appContext, workerParams) {
    companion object {
        const val LOGIN_FOR_WORKER = "login"
    }

    private val supabase = createSupabaseClient(
        supabaseUrl = "https://qcixwatrekwteykhfpcy.supabase.co",
        supabaseKey = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6InFjaXh3YXRyZWt3dGV5a2hmcGN5Iiwicm9sZSI6ImFub24iLCJpYXQiOjE3MjQxMDA4NzYsImV4cCI6MjAzOTY3Njg3Nn0.IJOlZQKAK9JiPEHVLGXWdyLYL31x6FyN8cva0akbw84"
    ) {
        install(Postgrest)
    }


    private val databaseReference: DatabaseReference by lazy { FirebaseDatabase.getInstance().reference.child("Users") }
    private val storageReference: StorageReference by lazy { FirebaseStorage.getInstance().reference.child("Avatars") }

    override suspend fun doWork(): Result {
        val login = inputData.getString("login").toString()

        databaseReference.child(login).removeValue().addOnCompleteListener {
            val oldImageRef = storageReference.child("${login}_avatar")
            oldImageRef.metadata.addOnSuccessListener {
                oldImageRef.delete().addOnCompleteListener {
                    Handler(Looper.getMainLooper()).post {
                        MainActivity.instance?.clearUserData()
                    }
                }
            }
        }

        CoroutineScope(Dispatchers.IO).launch {
            supabase.from("Forms").delete {
                filter {
                    eq("author_login", login)
                }
            }
        }
        Handler(Looper.getMainLooper()).post {
            MainActivity.instance?.clearUserData()
        }

        val intent = Intent("com.example.broadcast.MY_NOTIFICATION")
        intent.putExtra("message", "deleted")
        LocalBroadcastManager.getInstance(applicationContext).sendBroadcast(intent)

        return Result.success()
    }
}