package com.chertovich.photos.viewmodel

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.chertovich.photos.LoginIsEmptyException
import com.chertovich.photos.R
import com.chertovich.photos.UnknownException
import com.chertovich.photos.WrongSizeOfLoginException
import com.chertovich.photos.WrongSizeOfPasswordException
import com.chertovich.photos.data.RegData
import com.chertovich.photos.data.UserData
import com.chertovich.photos.model.Repository
import com.chertovich.photos.view.log
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.contracts.contract

@HiltViewModel
class MainViewModel
    @Inject constructor(private val repository: Repository, @ApplicationContext private val appContext: Context)
    : ViewModel() {

    private val _authorizedLiveData = MutableLiveData(false)
    val authorizedLiveData: LiveData<Boolean> = _authorizedLiveData

    private val _messageLiveData = SingleLiveEvent<String>()
    val messageLiveData: LiveData<String> = _messageLiveData

    private val _regUserDataLiveData = SingleLiveEvent<UserData>()
    val regUserDataLiveEvent: LiveData<UserData> = _regUserDataLiveData

    private val _clearRegDataLiveData = SingleLiveEvent<Any>()
    val clearRegDataLiveData: LiveData<Any> = _clearRegDataLiveData

    private val _goToLoginLiveData = SingleLiveEvent<Any>()
    val goToLoginLiveData: LiveData<Any> = _goToLoginLiveData

    private fun sendMessage(message: String) {
        _messageLiveData.value = message
    }

    private fun sendMessage(resId: Int) {
        sendMessage(appContext.getString(resId))
    }

    fun register(login: String, password: String, passwordConfirm: String) {
        if (password == passwordConfirm) {
            val regData = RegData(login, password)

            viewModelScope.launch {
                try {
                    val userData = repository.register(regData)
                    sendMessage(R.string.message_registration_was_successful)
                    _regUserDataLiveData.value = userData
                    _clearRegDataLiveData.value = Any()
                    _goToLoginLiveData.value = Any()
                } catch (e: LoginIsEmptyException) {
                    sendMessage(R.string.message_login_may_not_be_empty)
                } catch (e: LoginIsEmptyException) {
                    sendMessage(R.string.message_password_may_not_be_empty)
                } catch (e: WrongSizeOfLoginException) {
                    sendMessage(R.string.message_size_of_login_must_be_between_4_and_32)
                } catch (e: WrongSizeOfPasswordException) {
                    sendMessage(R.string.message_size_of_password_must_be_between_8_and_500)
                } catch (e: UnknownException) {
                    sendMessage(R.string.message_unknown_exception)
                }
            }
        } else {
            sendMessage(R.string.message_password_and_confirm_password_are_different)
        }
    }
}