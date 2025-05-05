package ru.netology.nmedia.viewmodel

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.StateFlow
import ru.netology.nmedia.auth.AppAuth
import ru.netology.nmedia.auth.AuthState
import javax.inject.Inject

@HiltViewModel
class AuthViewModel @Inject constructor (
    private val appAuth: AppAuth,
    ): ViewModel() { // ViewModel для проверки авторизации (ViewModel сохраняет данные)

    val state: StateFlow<AuthState?> = appAuth // представим подписку как LiveData
        .authState

        val isAuthenticated: Boolean
            get() = appAuth.authState.value != null // проверка на вход в приложение

}