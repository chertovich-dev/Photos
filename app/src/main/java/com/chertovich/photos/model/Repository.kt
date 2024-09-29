package com.chertovich.photos.model

import android.net.Uri
import com.chertovich.photos.data.Coordinate
import com.chertovich.photos.data.RegData
import com.chertovich.photos.data.User
import com.chertovich.photos.data.UploadImage
import com.chertovich.photos.data.Image

interface Repository {
    suspend fun signUp(regData: RegData): User
    suspend fun signIn(regData: RegData): User

    suspend fun uploadImage(token: String, uploadImage: UploadImage): Image
    suspend fun loadImages(token: String): List<Image>
    suspend fun loadImage(url: String): ByteArray
    suspend fun deleteImage(token: String, id: Int)

    suspend fun getPhotoBase64(uri: Uri): String

    suspend fun getCoordinate(): Coordinate

    suspend fun deleteAllImagesFromDB()
    suspend fun insertImageIntoDB(image: Image)
}