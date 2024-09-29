package com.chertovich.photos.model

import com.chertovich.photos.data.ImageResponse
import com.chertovich.photos.data.ImagesResponse
import com.chertovich.photos.data.RegData
import com.chertovich.photos.data.UserResponse
import com.chertovich.photos.data.UploadImage
import okhttp3.ResponseBody

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Url
import java.io.InputStream

interface PhotosService {
    @POST("/api/account/signup")
    suspend fun signUp(@Body regData: RegData): Response<UserResponse>

    @POST("/api/account/signin")
    suspend fun signIn(@Body regData: RegData): Response<UserResponse>

    @POST("/api/image")
    suspend fun uploadImage(@Header("Access-Token") token: String,
                            @Body uploadImage: UploadImage
    ): Response<ImageResponse>

    @GET("/api/image?page=0")
    suspend fun loadImages(@Header("Access-Token") token: String): Response<ImagesResponse>

    @GET
    suspend fun loadImage(@Url url: String): Response<ResponseBody>
}