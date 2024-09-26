package com.chertovich.photos.di

import com.chertovich.photos.model.Repository
import com.chertovich.photos.model.RepositoryImpl
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ViewModelComponent

@Module
@InstallIn(ViewModelComponent::class)
interface RepositoryModule {
    @Binds
    fun repositoryImpl(repositoryImpl: RepositoryImpl): Repository
}