package com.example.findme

import android.app.Application
import android.content.ContentValues.TAG
import android.content.Context
import android.content.SharedPreferences
import android.content.res.Configuration
import android.util.Log
import androidx.appcompat.app.AppCompatDelegate
import androidx.room.Room
import com.example.findme.data.AppDatabase
import dagger.hilt.android.HiltAndroidApp
import java.util.Locale

@HiltAndroidApp
class App : Application(){
    lateinit var db : AppDatabase
    lateinit var preferences : SharedPreferences

    override fun onCreate() {
        super.onCreate()
        preferences = getSharedPreferences("settings", MODE_PRIVATE)
        val isDarkTheme = preferences.getBoolean("dark_theme", false)

        AppCompatDelegate.setDefaultNightMode(
            if (isDarkTheme) AppCompatDelegate.MODE_NIGHT_YES else AppCompatDelegate.MODE_NIGHT_NO
        )

        db = Room.databaseBuilder(
            applicationContext,
            AppDatabase::class.java,
            "favourites_forms"
        ).build()
    }
}