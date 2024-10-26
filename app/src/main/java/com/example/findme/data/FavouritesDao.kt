package com.example.findme.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.example.findme.other.FavouriteForm


@Dao
interface FavouritesDao {
    @Query("SELECT * FROM favourites_forms")
    fun getAllFavourites(): List<FavouriteForm>

    @Insert
    fun insertFavourite(favouriteForm: FavouriteForm)

    @Query("DELETE FROM favourites_forms WHERE id = :favouriteId")
    fun deleteFavourite(favouriteId: Int)
}