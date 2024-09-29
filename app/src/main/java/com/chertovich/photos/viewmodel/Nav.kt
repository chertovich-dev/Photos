package com.chertovich.photos.viewmodel

import com.chertovich.photos.R

sealed class Nav(val action: Int)
class NavAuthToPhotos : Nav(R.id.action_nav_auth_to_nav_photos)
class NavPhotosToPhoto : Nav(R.id.action_nav_photos_to_nav_photo)