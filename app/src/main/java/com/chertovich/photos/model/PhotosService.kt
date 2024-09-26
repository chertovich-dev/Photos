package com.chertovich.photos.model

import com.chertovich.photos.data.RegData
import com.chertovich.photos.data.net.RegResponse

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

interface PhotosService {
    @POST("/api/account/signup")
    suspend fun signUp(@Body regData: RegData): Response<RegResponse>

    @POST("/api/account/signin")
    suspend fun signIn(@Body regData: RegData)
}