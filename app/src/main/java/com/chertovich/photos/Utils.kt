package com.chertovich.photos

import android.graphics.BitmapFactory
import android.widget.ImageView
import com.chertovich.photos.data.Photo

const val FIRST_INDEX = 0
const val WRONG_INDEX = -1

private const val SERVER_DATE_FACTOR = 100

fun setPhotoToImageView(imageView: ImageView, photo: Photo) {
    val data = photo.data

    if (data != null && data.isNotEmpty()) {
        val bitmap = BitmapFactory.decodeByteArray(data, 0, data.size)

        if (bitmap != null) {
            imageView.setImageBitmap(bitmap)
        }
    }
}

fun dateToServerDate(date: Long): Long {
    return date / SERVER_DATE_FACTOR
}

fun serverDateToDate(serverDate: Long): Long {
    return serverDate * SERVER_DATE_FACTOR
}