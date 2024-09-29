package com.chertovich.photos.data

data class Image(
    val id: Int,
    val url: String,
    val date: Long,
    val lat: Double,
    val lng: Double
)