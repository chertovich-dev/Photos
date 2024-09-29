package com.chertovich.photos.model

import android.net.Uri
import com.chertovich.photos.data.Coordinate
import com.chertovich.photos.data.Photo
import com.chertovich.photos.data.RegData
import com.chertovich.photos.data.User
import com.chertovich.photos.data.UploadImage
import com.chertovich.photos.data.Image
import java.io.InputStream

interface Repository {
    suspend fun signUp(regData: RegData): User
    suspend fun signIn(regData: RegData): User

    suspend fun getPhotoBase64(uri: Uri): String

    suspend fun uploadImage(token: String, uploadImage: UploadImage): Image
    suspend fun loadImages(token: String): List<Image>
    suspend fun loadImage(url: String): ByteArray

    suspend fun getCoordinate(): Coordinate
}