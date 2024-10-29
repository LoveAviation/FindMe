package com.example.findme.data

import androidx.room.Database
import androidx.room.RoomDatabase
import com.example.findme.other.FavouriteForm

@Database(entities = [FavouriteForm::class], version = 1)
abstract class AppDatabase: RoomDatabase() {
    abstract fun getSearchesDao() : FavouritesDao
}