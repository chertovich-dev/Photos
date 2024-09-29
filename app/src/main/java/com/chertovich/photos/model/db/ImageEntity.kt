package com.chertovich.photos.model.db

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(
    tableName = "images"
)
data class ImageEntity(
    @PrimaryKey val id: Long,
    val url: String,
    val date: Long,
    val lat: Double,
    val lng: Double
)