package com.example.findme

import android.app.Application
import androidx.room.Room
import com.example.findme.data.AppDatabase
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class App : Application(){
    lateinit var db : AppDatabase

    override fun onCreate() {
        super.onCreate()

        db = Room.databaseBuilder(
            applicationContext,
            AppDatabase::class.java,
            "favourites_forms"
        ).build()
    }
}