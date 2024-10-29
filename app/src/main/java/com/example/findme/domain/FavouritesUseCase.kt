package com.example.findme.domain

import com.example.findme.data.FavouritesDao
import com.example.findme.other.FavouriteForm
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

class FavouritesUseCase @Inject constructor(private val favDao: FavouritesDao) {
    suspend fun getAll(): List<Int> = withContext(Dispatchers.IO){
        val toReturn = mutableListOf<Int>()
        val get = favDao.getAllFavourites()

        for(f: FavouriteForm in get){
            toReturn.add(f.id)
        }
        toReturn
    }

    suspend fun insert(id:Int) = withContext(Dispatchers.IO){
        favDao.insertFavourite(FavouriteForm(id))
    }

    suspend fun delete(id:Int) = withContext(Dispatchers.IO){
        favDao.deleteFavourite(id)
    }
    suspend fun deleteAll() = withContext(Dispatchers.IO){
        favDao.deleteAllFavourites()
    }
}