package com.example.findme.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.example.findme.other.FavouriteForm

/**
 * Data Access Object
 * Интерфейс с главными функциями
 * для избранных анкет
 */

@Dao
interface FavouritesDao {
    @Query("SELECT * FROM favourites_forms")
    fun getAllFavourites(): List<FavouriteForm>

    @Insert
    fun insertFavourite(favouriteForm: FavouriteForm)

    @Query("DELETE FROM favourites_forms WHERE id = :favouriteId")
    fun deleteFavourite(favouriteId: Int)

    @Query("DELETE FROM favourites_forms")
    fun deleteAllFavourites()
}