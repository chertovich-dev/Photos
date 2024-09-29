package com.chertovich.photos.viewmodel

import android.content.Context
import android.net.Uri
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.chertovich.photos.ImageIsTooBigException
import com.chertovich.photos.LoadingPhotoException
import com.chertovich.photos.LoginAlreadyUsedException
import com.chertovich.photos.LoginIsEmptyException
import com.chertovich.photos.PasswordIsEmptyException
import com.chertovich.photos.PasswordIsIncorrectException
import com.chertovich.photos.R
import com.chertovich.photos.WrongSizeOfLoginException
import com.chertovich.photos.WrongSizeOfPasswordException
import com.chertovich.photos.data.Photo
import com.chertovich.photos.data.PhotoState
import com.chertovich.photos.data.RegData
import com.chertovich.photos.data.UploadImage
import com.chertovich.photos.data.User
import com.chertovich.photos.dateToServerDate
import com.chertovich.photos.model.Repository
import com.chertovich.photos.view.log
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.launch
import java.util.Date
import javax.inject.Inject

@HiltViewModel
class MainViewModel
    @Inject constructor(private val repository: Repository, @ApplicationContext private val appContext: Context)
    : ViewModel() {
    private var user: User? = null

    private val token: String
        get() = user?.token ?: ""

    private val photos = mutableListOf<Photo>()

    private val _photosLiveData = MutableLiveData<List<Photo>>(photos)
    val photosLiveData: LiveData<List<Photo>> = _photosLiveData

    private val _authorizedLiveData = MutableLiveData(user)
    val authorizedLiveData: LiveData<User?> = _authorizedLiveData

    private val _messageLiveData = SingleLiveEvent<String>()
    val messageLiveData: LiveData<String> = _messageLiveData

    private val _navLiveData = SingleLiveEvent<Nav>()
    val navLiveData: LiveData<Nav> = _navLiveData

    private val _userLive = SingleLiveEvent<User>()
    val userLiveEvent: LiveData<User> = _userLive

    private val _clearRegDataLiveData = SingleLiveEvent<Any>()
    val clearRegDataLiveData: LiveData<Any> = _clearRegDataLiveData

    private val _goToLoginLiveData = SingleLiveEvent<Any>()
    val goToLoginLiveData: LiveData<Any> = _goToLoginLiveData

    private val _refreshPhotosLiveData = SingleLiveEvent<List<Photo>>()
    val refreshPhotosLiveData: LiveData<List<Photo>> = _refreshPhotosLiveData

    private val _photosChangedLiveData = SingleLiveEvent<Any>()
    val photosChangedLiveData: LiveData<Any> = _photosChangedLiveData

    private val _photoLiveData = MutableLiveData<Photo>()
    val photoLiveData: LiveData<Photo> = _photoLiveData

    private val _deleteDialogLiveData = SingleLiveEvent<Int>()
    val deleteDialogLiveData: LiveData<Int> = _deleteDialogLiveData

    private fun sendMessage(message: String) {
        _messageLiveData.value = message
    }

    private fun sendMessage(resId: Int) {
        sendMessage(appContext.getString(resId))
    }

    private fun refreshPhotos() {
        _refreshPhotosLiveData.value = photos
    }

    private fun photosChanged() {
        _photosChangedLiveData.value = Any()
    }

    private fun handleException(e: Exception) {
        when (e) {
            is LoginIsEmptyException -> sendMessage(R.string.message_login_may_not_be_empty)
            is PasswordIsEmptyException -> sendMessage(R.string.message_password_may_not_be_empty)
            is WrongSizeOfLoginException -> sendMessage(R.string.message_size_of_login_must_be_between_4_and_32)
            is WrongSizeOfPasswordException -> sendMessage(R.string.message_size_of_password_must_be_between_8_and_500)
            is LoginAlreadyUsedException -> sendMessage(R.string.message_login_already_used)
            is PasswordIsIncorrectException -> sendMessage(R.string.message_password_is_incorrect)
            is ImageIsTooBigException -> sendMessage(R.string.message_image_is_too_big)
            is LoadingPhotoException -> sendMessage(R.string.message_exception_loading_photo)
            else -> sendMessage(R.string.message_unknown_exception)
        }
    }

    fun signUp(login: String, password: String, passwordConfirm: String) {
        if (password == passwordConfirm) {
            val regData = RegData(login, password)

            viewModelScope.launch {
                try {
                    val user = repository.signUp(regData)
                    sendMessage(R.string.message_registration_was_successful)
                    _userLive.value = user
                    _clearRegDataLiveData.value = Any()
                    _goToLoginLiveData.value = Any()
                } catch (e: Exception) {
                    handleException(e)
                }
            }
        } else {
            sendMessage(R.string.message_password_and_confirm_password_are_different)
        }
    }

    fun signIn(login: String, password: String) {
        viewModelScope.launch {
            try {
                val regData = RegData(login, password)
                user = repository.signIn(regData)
                _navLiveData.value = NavAuthToPhotos()
                _authorizedLiveData.value = user

                log("userData = $user")
            } catch (e: Exception) {
                handleException(e)
            }
        }
    }

    fun savePhoto(uri: Uri) {
        val serverDate = dateToServerDate(Date().time)

        viewModelScope.launch {
            try {
                val coordinate = repository.getCoordinate()
                val photoBase64 = repository.getPhotoBase64(uri)
                val uploadImage = UploadImage(photoBase64, serverDate, coordinate.latitude, coordinate.longitude)
                val image = repository.uploadImage(token, uploadImage)
                val photo = Photo(image)
                photos.add(photo)
                photosChanged()
            } catch (e: Exception) {
                handleException(e)
            }
        }
    }

    fun loadPhotos() {
        viewModelScope.launch {
            val images = repository.loadImages(token)

            photos.clear()

            for (image in images) {
                val photo = Photo(image)
                photos.add(photo)
            }

            _photosLiveData.value = photos

            repository.deleteAllImagesFromDB()

            for (image in images) {
                repository.insertImageIntoDB(image)
            }
        }
    }

    fun loadPhoto(index: Int) {
        if (index in photos.indices) {
            val photo = photos[index]
            photo.state = PhotoState.LOADING

            viewModelScope.launch {
                try {
                    photo.data = repository.loadImage(photo.image.url)
                    photo.state = PhotoState.REFRESH
                    refreshPhotos()
                } catch (e: Exception) {
                    handleException(e)
                }
            }
        }
    }

    fun showPhoto(index: Int) {
        if (index in photos.indices) {
            val photo = photos[index]

            if (photo.isLoaded) {
                _photoLiveData.value = photo
                _navLiveData.value = NavPhotosToPhoto()
            }
        }
    }

    fun showDeleteDialog(index: Int) {
        _deleteDialogLiveData.value = index
    }

    fun deletePhoto(index: Int) {
        viewModelScope.launch {
            if (index in photos.indices) {
                val photo = photos[index]
                try {
                    repository.deleteImage(token, photo.image.id)
                    photos.removeAt(index)
                    photosChanged()
                } catch (e: Exception) {
                    handleException(e)
                }
            }
        }
    }
}