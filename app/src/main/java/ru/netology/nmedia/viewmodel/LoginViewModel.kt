package ru.netology.nmedia.viewmodel

import android.app.Application
import android.widget.Toast
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import ru.netology.nmedia.db.AppDb
import ru.netology.nmedia.error.ApiError
import ru.netology.nmedia.model.LoginModelState
import ru.netology.nmedia.repository.PostRepository
import ru.netology.nmedia.util.SingleLiveEvent


class LoginViewModel(application: Application): AndroidViewModel(application) {

    private val repository = PostRepository(AppDb.getInstance(application).postDao())

    private val _loginSuccessful = SingleLiveEvent<Unit>() // Переход на фрагмент с лентой, когда вход успешен
    val loginSuccessful: LiveData<Unit>
        get() = _loginSuccessful

    private val _loginState = MutableLiveData<LoginModelState>()
    val loginState: LiveData<LoginModelState>
        get() = _loginState


    fun checkingUserLogin(login: String, pass: String) = viewModelScope.launch {

        try {

            _loginState.value = LoginModelState(loading = true)

            repository.updateUser(login, pass)

            _loginState.value = LoginModelState()

            _loginSuccessful.value = Unit

        } catch(e: ApiError) {

            _loginState.value = LoginModelState()

             if (e.status == 404) toastFun(invalidInput = true)

        } catch (e: Exception) {
            _loginState.value = LoginModelState(error = true)

        }

    }

    fun toastFun(login: Boolean = false, invalidInput:Boolean = false) {
        val loginSuccessful = "Вход выполнен"
        val badLoginOrPassword = "Неверный логин или пароль"
        val phrase = listOf(
            "Не удалось, попробуйте позже",
            "Ошибка :(",
            "Что-то пошло нет так..",
            "Ошибка соединения",
        )

        val randomPhrase = phrase.random()
        val text = when {
            invalidInput -> badLoginOrPassword
            login -> loginSuccessful
            else -> randomPhrase
        }
        Toast.makeText(getApplication(), text, Toast.LENGTH_SHORT).show()
    }



}