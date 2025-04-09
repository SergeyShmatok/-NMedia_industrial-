package ru.netology.nmedia.auth



data class AuthState(
    val userId: Long = 0,
    val token: String? = null,

)