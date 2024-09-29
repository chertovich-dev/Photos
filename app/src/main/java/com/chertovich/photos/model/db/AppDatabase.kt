package com.chertovich.photos.model.db

import androidx.room.Database
import androidx.room.RoomDatabase

const val DATABASE_NAME = "database.db"

@Database(
    version = 1,
    entities = [
        ImageEntity::class
    ]
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun getImageDao(): ImageDao
}