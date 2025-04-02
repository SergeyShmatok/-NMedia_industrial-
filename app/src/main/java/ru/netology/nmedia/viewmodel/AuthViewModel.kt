package ru.netology.nmedia.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import ru.netology.nmedia.auth.AppAuth
import ru.netology.nmedia.auth.AuthState

class AuthViewModel: ViewModel() { // ViewModel для проверки авторизации (ViewModel сохраняет данные)

    val state: LiveData<AuthState?> = AppAuth.getInstance() // представим подписку как LiveData
        .authState.asLiveData() // получаем инстанс и доступ к его полю

        val isAuthenticated: Boolean
            get() = AppAuth.getInstance().authState.value != null // проверка на вход в приложение

}