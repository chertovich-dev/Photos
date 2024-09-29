package com.chertovich.photos.model.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query

@Dao
interface ImageDao {
    @Insert
    fun insertImage(imageEntity: ImageEntity)

    @Query("DELETE FROM images")
    fun deleteAllImages()
}