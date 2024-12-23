package com.example.findme.other

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import javax.inject.Inject

/**
 * Объект, который хранится в базе данных Room
 */

@Entity(tableName = "favourites_forms")
data class FavouriteForm @Inject constructor(
    @PrimaryKey
    @ColumnInfo(name = "id")
    val id: Int
)