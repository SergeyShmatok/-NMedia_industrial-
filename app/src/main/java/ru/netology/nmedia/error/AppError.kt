package ru.netology.nmedia.error

import android.database.SQLException
import java.io.IOException

// sealed классы в явном виде перечисляют классы, которым разрешено быть
// их дочерними классами. Такой контроль дает нам больше уверенности в том,
// как именно тот или иной класс будет использоваться в нашем коде.


 sealed class AppError(var code: String) : RuntimeException() {
    companion object {
        fun from(e: Throwable): AppError = when (e) {
            is AppError -> e
            is SQLException -> DbError
            is IOException -> NetworkError
            else -> UnknownError
        }
    }
}

class ApiError(val status: Int, code: String) : AppError(code)
data object NetworkError : AppError("error_network")
data object DbError : AppError("error_db")
data object UnknownError : AppError("error_unknown")


//--------------------------------------------------------------------------------------------------

//                             < Случаи использования sealed классов >
//
//---------------------- Моделирование конечных состояний
//
// Одно из наиболее распространенных применений для sealed классов — это представление конечного
//
// множества состояний. Например:
//
// Состояния UI: загрузка, ошибка, успех, бездействие.
//
// Состояния сетевого запроса: ожидает, в процессе, успех, неудача.
//
// Состояния игры: играет, на паузе, окончена.
//
// Используя sealed класс, мы можем гарантировать, что все возможные состояния определены в явном виде
// и корректно обрабатываются, предотвращая ошибки в runtime:
//
// sealed class UIState {
//    data class Loading(val isLoading: Boolean) : UIState()
//    data class Error(val message: String) : UIState()
//    data class Success(val data: Any) : UIState()
//    object Idle : UIState()
// }


//---------------------- Расширение сопоставления паттернов
//
// Sealed классы идеально работают с сопоставлением паттернов, позволяя вам полностью проверить
//  все возможные случаи:
//
// fun processUIState(uiState: UIState) {
//    when (uiState) {
//        is UIState.Loading -> showLoadingIndicator()
//        is UIState.Error -> showErrorMessage(uiState.message)
//        is UIState.Success -> showData(uiState.data)
//        is UIState.Idle -> doNothing()
//    }
// }

//--------------------------------------------------------------------------------------------------
