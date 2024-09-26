package com.chertovich.photos.model

import android.content.Context
import com.chertovich.photos.LoginIsEmptyException
import com.chertovich.photos.PasswordIsEmptyException
import com.chertovich.photos.PhotosException
import com.chertovich.photos.UnknownException
import com.chertovich.photos.WrongSizeOfLoginException
import com.chertovich.photos.WrongSizeOfPasswordException
import com.chertovich.photos.data.RegData
import com.chertovich.photos.data.UserData
import com.chertovich.photos.view.log
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.ResponseBody
import org.json.JSONException
import org.json.JSONObject
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import javax.inject.Inject
import javax.inject.Singleton

private const val URL = "https://junior.balinasoft.com/"

private const val KEY_ERROR = "error"
private const val KEY_VALID = "valid"
private const val KEY_FIELD = "field"
private const val KEY_MESSAGE = "message"

private const val VALIDATION_ERROR = "validation-error"
private const val LOGIN = "login"
private const val PASSWORD = "password"
private const val MAY_NOT_BE_EMPTY = "may not be empty"
private const val SIZE_MUST_BE_BETWEEN_4_AND_32 = "size must be between 4 and 32"
private const val SIZE_MUST_BE_BETWEEN_8_AND_500 = "size must be between 8 and 500"

private const val FIST_INDEX = 0

@Singleton
class RepositoryImpl @Inject constructor(@ApplicationContext private val appContext: Context) : Repository  {
    private val retrofit = Retrofit.Builder()
        .baseUrl(URL)
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    private val photosService = retrofit.create(PhotosService::class.java)

    private fun getPhotosException(responseBody: ResponseBody?): PhotosException {
        val errorJSON = responseBody?.string()
        val json = JSONObject(errorJSON)
        log("errorJSON = $errorJSON")
        try {
            val error = json.getString(KEY_ERROR)

            if (error == VALIDATION_ERROR) {
                val validJSONArray = json.getJSONArray(KEY_VALID)

                val exceptions = mutableListOf<PhotosException>()

                for (i in 0 until validJSONArray.length()) {
                    val validJSON = validJSONArray.getJSONObject(i)
                    exceptions.add(getPhotosExceptionOfJSON(validJSON))
                }

                if (exceptions.isNotEmpty()) {
                    // Выполняем сортировку исключений, так как сервер может выдавать их в разном порядке
                    exceptions.sortBy { it.javaClass.name }

                    return exceptions[FIST_INDEX]
                }
            }
        } catch (e: JSONException) {
            //
        }

        return UnknownException()
    }

    private fun getPhotosExceptionOfJSON(jsonObject: JSONObject): PhotosException {
        val field = jsonObject.getString(KEY_FIELD)
        val message = jsonObject.getString(KEY_MESSAGE)

        if (field == LOGIN && message == MAY_NOT_BE_EMPTY) {
            return LoginIsEmptyException()
        }

        if (field == PASSWORD && message == MAY_NOT_BE_EMPTY) {
            return PasswordIsEmptyException()
        }

        if (field == LOGIN && message == SIZE_MUST_BE_BETWEEN_4_AND_32) {
            return WrongSizeOfLoginException()
        }

        if (field == PASSWORD && message == SIZE_MUST_BE_BETWEEN_8_AND_500) {
            return WrongSizeOfPasswordException()
        }

        return UnknownException()
    }

    override suspend fun register(regData: RegData): UserData = withContext(Dispatchers.IO) {
        val response = photosService.signUp(regData)

        val data: UserData? =  if (response.isSuccessful) {
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
}