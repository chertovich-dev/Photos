package com.chertovich.photos.model

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.util.Base64
import androidx.core.app.ActivityCompat
import androidx.room.Room
import com.chertovich.photos.FIRST_INDEX
import com.chertovich.photos.ImageIsTooBigException
import com.chertovich.photos.LoadingPhotoException
import com.chertovich.photos.LoginAlreadyUsedException
import com.chertovich.photos.LoginIsEmptyException
import com.chertovich.photos.PasswordIsEmptyException
import com.chertovich.photos.PasswordIsIncorrectException
import com.chertovich.photos.PhotosException
import com.chertovich.photos.UnknownException
import com.chertovich.photos.WrongSizeOfLoginException
import com.chertovich.photos.WrongSizeOfPasswordException
import com.chertovich.photos.data.Coordinate
import com.chertovich.photos.data.Image
import com.chertovich.photos.data.RegData
import com.chertovich.photos.data.UploadImage
import com.chertovich.photos.data.User
import com.chertovich.photos.model.db.AppDatabase
import com.chertovich.photos.model.db.DATABASE_NAME
import com.chertovich.photos.view.log
import com.google.android.gms.location.CurrentLocationRequest
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationTokenSource
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.ResponseBody
import org.json.JSONException
import org.json.JSONObject
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.io.ByteArrayOutputStream
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

private const val URL = "https://junior.balinasoft.com/"

private const val CLIENT_TIMEOUT_SECONDS = 60L
private const val PHOTO_WIDTH = 500f
private const val PHOTO_QUALITY = 80

private const val KEY_ERROR = "error"
private const val KEY_VALID = "valid"
private const val KEY_FIELD = "field"
private const val KEY_MESSAGE = "message"

private const val ERROR_VALIDATION = "validation-error"
private const val ERROR_SIGNIN_INCORRECT = "security.signin.incorrect"
private const val ERROR_LOGIN_ALREADY_USED = "security.signup.login-already-use"
private const val ERROR_BIG_IMAGE = "big-image"

private const val FIELD_LOGIN = "login"
private const val FIELD_PASSWORD = "password"
private const val FIELD_BASE64IMAGE = "base64Image"
private const val MESSAGE_MAY_NOT_BE_EMPTY = "may not be empty"
private const val MESSAGE_SIZE_MUST_BE_BETWEEN_4_AND_32 = "size must be between 4 and 32"
private const val MESSAGE_SIZE_MUST_BE_BETWEEN_8_AND_500 = "size must be between 8 and 500"
private const val MESSAGE_LENGTH_MUST_BE_BETWEEN_0_AND_8388608 = "length must be between 0 and 8388608"

@Singleton
class RepositoryImpl @Inject constructor(@ApplicationContext private val appContext: Context) : Repository  {
    private val client = OkHttpClient.Builder()
        .connectTimeout(CLIENT_TIMEOUT_SECONDS, TimeUnit.SECONDS)
        .readTimeout(CLIENT_TIMEOUT_SECONDS, TimeUnit.SECONDS)
        .writeTimeout(CLIENT_TIMEOUT_SECONDS, TimeUnit.SECONDS)
        .build()

    private val retrofit = Retrofit.Builder()
        .baseUrl(URL)
        .client(client)
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    private val photosService = retrofit.create(PhotosService::class.java)

    private val db = Room.databaseBuilder(appContext, AppDatabase::class.java, DATABASE_NAME).build()

    private var fusedLocationClient = LocationServices.getFusedLocationProviderClient(appContext)

    private fun getPhotosException(responseBody: ResponseBody?): PhotosException {
        val errorJSON = responseBody?.string()
        val jsonObject = JSONObject(errorJSON)

        log("errorJSON = $errorJSON")

        try {
            val error = jsonObject.getString(KEY_ERROR)

            return when (error) {
                ERROR_VALIDATION -> {
                    getFirstValidationError(jsonObject)
                }

                ERROR_LOGIN_ALREADY_USED -> {
                    LoginAlreadyUsedException()
                }

                ERROR_SIGNIN_INCORRECT -> {
                    PasswordIsIncorrectException()
                }

                ERROR_BIG_IMAGE -> {
                    ImageIsTooBigException()
                }

                else -> {
                    UnknownException()
                }
            }

        } catch (e: JSONException) {
            return UnknownException()
        }
    }

    private fun getFirstValidationError(jsonObject: JSONObject): PhotosException {
        val validJSONArray = jsonObject.getJSONArray(KEY_VALID)

        val exceptions = mutableListOf<PhotosException>()

        for (i in 0 until validJSONArray.length()) {
            val validJSON = validJSONArray.getJSONObject(i)
            exceptions.add(getValidationError(validJSON))
        }

        if (exceptions.isNotEmpty()) {
            // Выполняем сортировку исключений, так как сервер может выдавать их в разном порядке
            exceptions.sortBy { it.javaClass.name }

            return exceptions[FIRST_INDEX]
        }

        return UnknownException()
    }

    private fun getValidationError(jsonObject: JSONObject): PhotosException {
        val field = jsonObject.getString(KEY_FIELD)
        val message = jsonObject.getString(KEY_MESSAGE)

        if (field == FIELD_LOGIN && message == MESSAGE_MAY_NOT_BE_EMPTY) {
            return LoginIsEmptyException()
        }

        if (field == FIELD_PASSWORD && message == MESSAGE_MAY_NOT_BE_EMPTY) {
            return PasswordIsEmptyException()
        }

        if (field == FIELD_LOGIN && message == MESSAGE_SIZE_MUST_BE_BETWEEN_4_AND_32) {
            return WrongSizeOfLoginException()
        }

        if (field == FIELD_PASSWORD && message == MESSAGE_SIZE_MUST_BE_BETWEEN_8_AND_500) {
            return WrongSizeOfPasswordException()
        }

        if (field == FIELD_BASE64IMAGE && message == MESSAGE_LENGTH_MUST_BE_BETWEEN_0_AND_8388608) {
            return ImageIsTooBigException()
        }

        return UnknownException()
    }

    override suspend fun signUp(regData: RegData): User = withContext(Dispatchers.IO) {
        val response = photosService.signUp(regData)

        val data: User? =  if (response.isSuccessful) {
            response.body()?.data
        } else {
            null
        }

        if (data == null) {
            throw getPhotosException(response.errorBody())
        } else {
            return@withContext data
        }
    }

    override suspend fun signIn(regData: RegData): User = withContext(Dispatchers.IO) {
        val response = photosService.signIn(regData)

        val data: User? =  if (response.isSuccessful) {
            response.body()?.data
        } else {
            null
        }

        if (data == null) {
            throw getPhotosException(response.errorBody())
        } else {
            return@withContext data
        }
    }

    override suspend fun getPhotoBase64(uri: Uri): String = withContext(Dispatchers.IO) {
        try {
            val inputStream = appContext.contentResolver.openInputStream(uri)
            val bitmap = BitmapFactory.decodeStream(inputStream)
            inputStream?.close()

            val destWidth = PHOTO_WIDTH
            val ratio = bitmap.width / destWidth
            val destHeight = bitmap.height / ratio
            val scaledBitmap = Bitmap.createScaledBitmap(bitmap, destWidth.toInt(), destHeight.toInt(), false)

            val byteArrayOutputStream = ByteArrayOutputStream()
            scaledBitmap.compress(Bitmap.CompressFormat.JPEG, PHOTO_QUALITY, byteArrayOutputStream)
            val byteArray = byteArrayOutputStream.toByteArray()
            byteArrayOutputStream.close()

            return@withContext Base64.encodeToString(byteArray, Base64.DEFAULT)
        } catch (e: Exception) {
            throw LoadingPhotoException()
        }
    }

    override suspend fun uploadImage(token: String, uploadImage: UploadImage): Image = withContext(Dispatchers.IO) {
        val response = photosService.uploadImage(token, uploadImage)

        val data: Image? = if (response.isSuccessful) {
            response.body()?.data
        } else {
            null
        }

        if (data == null) {
            throw getPhotosException(response.errorBody())
        } else {
            return@withContext data
        }
    }

    override suspend fun loadImages(token: String): List<Image> = withContext(Dispatchers.IO) {
        val response = photosService.loadImages(token)

        val data: List<Image>? = if (response.isSuccessful) {
            response.body()?.data
        } else {
            null
        }

        if (data == null) {
            throw getPhotosException(response.errorBody())
        } else {
            return@withContext data
        }
    }

    override suspend fun deleteImage(token: String, id: Int) = withContext(Dispatchers.IO) {
        val response = photosService.deleteImage(token, id)

        if (!response.isSuccessful) {
            throw UnknownException()
        }
    }

    override suspend fun loadImage(url: String): ByteArray = withContext(Dispatchers.IO) {
        val response = photosService.loadImage(url)

        val data: ByteArray? = if (response.isSuccessful) {
            response.body()?.byteStream()?.readBytes()
        } else {
            null
        }

        if (data == null) {
            throw getPhotosException(response.errorBody())
        } else {
            return@withContext data
        }
    }

    @SuppressLint("MissingPermission")
    override suspend fun getCoordinate(): Coordinate = withContext(Dispatchers.IO) {
        try {
            val currentLocationRequest = CurrentLocationRequest.Builder()
                .setPriority(Priority.PRIORITY_BALANCED_POWER_ACCURACY)
                .build()

            val token = CancellationTokenSource().token
            val location = fusedLocationClient.getCurrentLocation(currentLocationRequest, token).await()

            val coordinate = if (location == null) {
                Coordinate()
            } else {
                Coordinate(location.latitude, location.longitude)
            }

            return@withContext coordinate
        } catch (e: Exception) {
            return@withContext Coordinate()
        }
    }

    override suspend fun deleteAllImagesFromDB() = withContext(Dispatchers.IO) {
        try {
            db.getImageDao().deleteAllImages()
        } catch (e: Exception) {
            //
        }
    }

    override suspend fun insertImageIntoDB(image: Image) = withContext(Dispatchers.IO) {
        try {
            db.getImageDao().insertImage(image.toImageEntity())
        } catch (e: Exception) {
            //
        }
    }
}