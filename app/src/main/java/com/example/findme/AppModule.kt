package com.example.findme

import android.content.Context
import androidx.room.Room
import com.example.findme.data.AppDatabase
import com.example.findme.data.FavouritesDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule{
    @Singleton
    @Provides
    fun provideSearchDatabase(@ApplicationContext context : Context) =
        Room.databaseBuilder(context, AppDatabase::class.java, "favourite_forms")
            .fallbackToDestructiveMigration()
            .build()
    @Provides
    fun provideSearchDAO(appDatabase: AppDatabase): FavouritesDao {
        return appDatabase.getSearchesDao()
    }
}