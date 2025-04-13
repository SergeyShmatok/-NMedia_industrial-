package ru.netology.nmedia.viewmodel

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.StateFlow
import ru.netology.nmedia.auth.AppAuth
import ru.netology.nmedia.auth.AuthState

class AuthViewModel: ViewModel() { // ViewModel для проверки авторизации (ViewModel сохраняет данные)

    val state: StateFlow<AuthState?> = AppAuth.getInstance() // представим подписку как LiveData
        .authState // получаем инстанс и доступ к его полю

        val isAuthenticated: Boolean
            get() = AppAuth.getInstance().authState.value != null // проверка на вход в приложение

}