package ru.netology.nmedia.viewmodel

import android.content.Context
import android.widget.Toast
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.launch
import ru.netology.nmedia.error.ApiError
import ru.netology.nmedia.model.LoginModelState
import ru.netology.nmedia.repository.PostRepositoryFun
import javax.inject.Inject

@HiltViewModel
class LoginViewModel @Inject constructor (
    private val repository: PostRepositoryFun,
    private val applicationContext: Context,
    ): ViewModel() {

        // private val repository = PostRepository(AppDb.getInstance(application).postDao())

    private val _loginSuccessful = MutableStateFlow<Unit?>(null) // Переход на фрагмент с лентой, когда вход успешен
    val loginSuccessful: Flow<Unit>
        get() = _loginSuccessful.asStateFlow().filterNotNull()

    fun loginSuccessfulIsNull() {
        _loginSuccessful.value = null
    }


    private val _loginState = MutableStateFlow(LoginModelState())
    val loginState: StateFlow<LoginModelState>
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

        Toast.makeText(applicationContext, text, Toast.LENGTH_SHORT).show()
    }

}