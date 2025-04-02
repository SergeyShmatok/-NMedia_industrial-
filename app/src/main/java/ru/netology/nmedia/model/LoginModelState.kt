package ru.netology.nmedia.model

data class LoginModelState(

    val loading: Boolean = false,
    val error: Boolean = false,
    val isSuccessful: Boolean = false,

    )
