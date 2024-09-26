package com.chertovich.photos.model

import com.chertovich.photos.data.RegData
import com.chertovich.photos.data.UserData

interface Repository {
    suspend fun register(regData: RegData): UserData
}