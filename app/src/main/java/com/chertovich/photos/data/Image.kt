package com.chertovich.photos.data

import com.chertovich.photos.model.db.ImageEntity

data class Image(
    val id: Int,
    val url: String,
    val date: Long,
    val lat: Double,
    val lng: Double
) {
    fun toImageEntity() = ImageEntity(id.toLong(), url, date, lat, lng)
}