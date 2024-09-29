package com.chertovich.photos.data

data class UploadImage(
    val base64Image: String,
    val date: Long,
    val lat: Double,
    val lng: Double
)