package com.chertovich.photos.data

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class Coordinate(val latitude: Double = 0.0, val longitude: Double = 0.0) : Parcelable {
    override fun toString(): String = "$latitude, $longitude"
}