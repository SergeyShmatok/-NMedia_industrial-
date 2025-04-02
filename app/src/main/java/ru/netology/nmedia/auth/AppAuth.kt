package ru.netology.nmedia.auth

import android.content.Context
import androidx.core.content.edit
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

// передаём контекст, потому что будем работать с преференсами
class AppAuth private constructor(context: Context) {

    private val pref = context.getSharedPreferences("auth", Context.MODE_PRIVATE) // название и "мод"
    private val _authState = MutableStateFlow<AuthState?>(null)
    val authState: StateFlow<AuthState?> = _authState.asStateFlow() // преобразование в обычный Flow

    init {
        val id = pref.getLong(ID_KEY, 0L)
        val token = pref.getString(TOKEN_KEY, null)

        if (id == 0L || token == null) {
            pref.edit { clear() }

        } else {
            _authState.value = AuthState(id, token)
        }


    }

    @Synchronized // означает, что метод будет защищен от одновременного выполнения
    // несколькими потоками монитором экземпляра
    // (или, для статических методов, класса), на котором определен метод (??)
    fun setAuth(userId: Long, token: String) {
    _authState.value = AuthState(userId, token)
        pref.edit {
            putString(TOKEN_KEY, token)
            putLong(ID_KEY, userId)
        }
}

    @Synchronized
    fun removeAuth() {
        _authState.value = null
        pref.edit { clear() }

    }


companion object {

    private var INSTANCE: AppAuth? = null

    private const val TOKEN_KEY = "token"
    private const val ID_KEY = "id"

    fun getInstance(): AppAuth = synchronized(this) {
        checkNotNull(INSTANCE) {
            "You must initialize before calling"
        } // гарантирует, что там нет "null"

    }


    fun initApp(context: Context) = INSTANCE ?: synchronized(this) { // в качестве "лока" можно использовать companion object
        // companion обеспечивает постоянность "объекта"
        INSTANCE ?: AppAuth(context).also { INSTANCE = it }

    }

}

}

// checkNotNull — функция проверки условий из стандартной библиотеки Kotlin.
// Она проверяет значение на null и возвращает его, если оно не равно null,
// а иначе возбуждает IllegalStateException.

// Функция принимает два аргумента:

// 1 - Значение для проверки на null.
// 2 - Сообщение об ошибке, которое надо вывести в консоль, если значение, принятое на проверку, оказалось null.

// Использование checkNotNull позволяет получить более ясный код, чем ручное возбуждение исключений.