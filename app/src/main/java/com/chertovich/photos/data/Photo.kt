package com.chertovich.photos.data

enum class PhotoState {
    EMPTY, LOADING, REFRESH, LOADED
}

class Photo(val image: Image) {
    var state: PhotoState = PhotoState.EMPTY
    var data: ByteArray? = null

    val isLoaded: Boolean
        get() = state == PhotoState.LOADED
}
