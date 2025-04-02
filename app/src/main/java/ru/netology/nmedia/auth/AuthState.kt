package ru.netology.nmedia.auth



data class AuthState(
    val userId: Long,
    val token: String,

)