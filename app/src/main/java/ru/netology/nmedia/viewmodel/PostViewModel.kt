package ru.netology.nmedia.viewmodel

import android.app.Application
import android.widget.Toast
import androidx.lifecycle.*
import kotlinx.coroutines.launch
import ru.netology.nmedia.db.AppDb
import ru.netology.nmedia.dto.Post
import ru.netology.nmedia.error.ApiError
import ru.netology.nmedia.error.AppError
import ru.netology.nmedia.error.NetworkError
import ru.netology.nmedia.error.UnknownError
import ru.netology.nmedia.model.FeedModel
import ru.netology.nmedia.model.FeedModelState
import ru.netology.nmedia.repository.*
import ru.netology.nmedia.util.SingleLiveEvent

private val empty = Post(
    id = 0,
    content = "",
    author = "",
    likedByMe = false,
    likes = 0,
    published = "",
    authorAvatar = "",
    attachment = null,
)

// @Deprecated(message = "Не использовать", replaceWith = ReplaceWith...) (Из вебинара)
class PostViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = PostRepositoryImpl(AppDb.getInstance(application).postDao())
    private val _data = repository.data.map { FeedModel(posts = it) }
    val data: LiveData<FeedModel>
        get() = _data

    private val _dataState = MutableLiveData<FeedModelState>()
    val dataState: LiveData<FeedModelState>
        get() = _dataState

    private val edited = MutableLiveData(empty)

    private val _postCreated = SingleLiveEvent<Unit>()
    val postCreated: LiveData<Unit>
        get() = _postCreated


//--------------------------------------------------------------------------------------------------

    init { loadPosts() }

//--------------------------------------------------------------------------------------------------

    fun loadPosts(refreshed: Boolean = false) = viewModelScope.launch {

            // viewModelScope описана на главном потоке (Dispatchers.Main.immediate),
            // но асинхронно с ним самим. "Корутины — это потоки исполнения кода,
            // которые организуются поверх системных потоков".
            // _data.value = (FeedModel(loading = true))

        val state = if (refreshed) (FeedModelState(refreshing = true))
        else (FeedModelState(loading = true))

            try {
                _dataState.value = state
                repository.getAll()
                _dataState.value = FeedModelState()
            } catch (e: Exception) {
                _dataState.value = (FeedModelState(error = true))
                if (e is AppError) when (e) {
                        is ApiError -> {} // Можно поставить разные "флаги" на разные ошибки
                        NetworkError -> {} // --//--
                        UnknownError -> {} // --//--
                    }
            }
        }

//--------------------------------------------------------------------------------------------------

    fun refreshing() = viewModelScope.launch {
        loadPosts(true)
    }

//--------------------------------------------------------------------------------------------------

    fun likeById(id: Long) = viewModelScope.launch {
        // viewModelScope автоматически отменится в случае закрытия активити

        try {
            repository.likeById(id)
            _dataState.value = (FeedModelState(likeError = false))
        } catch (e: Exception) {
            _dataState.value = (FeedModelState(likeError = true))
            if (e is AppError) when (e) {
                is ApiError -> {}
                NetworkError -> {}
                UnknownError -> {}
            }
        }
    }

//--------------------------------------------------------------------------------------------------

    fun removeLike(id: Long) = viewModelScope.launch {
        // viewModelScope автоматически отменится в случае закрытия активити

        try {
            repository.removeLike(id)
            _dataState.value = (FeedModelState(likeError = false))
        } catch (e: Exception) {
            _dataState.value = (FeedModelState(likeError = true))
            if (e is AppError) when (e) {
                is ApiError -> {}
                NetworkError -> {}
                UnknownError -> {}
            }
        }
    }

    // val old = _data.value?.posts.orEmpty()
//--------------------------------------------------------------------------------------------------

    fun likeErrorIsFalse() {
        _dataState.value = _dataState.value?.copy(likeError = false)
    }

//--------------------------------------------------------------------------------------------------

    fun removeById(id: Long) = viewModelScope.launch {

        try {
             repository.removeById(id)
            _dataState.value = _dataState.value?.copy(postIsDeleted = true)

        } catch (e: Exception) {
            _dataState.value = _dataState.value?.copy(postIsDeleted = false)
            if (e is AppError) when (e) {
                is ApiError -> {}
                NetworkError -> {}
                UnknownError -> {}
            }
        }
    }

//--------------------------------------------------------------------------------------------------

    fun postDelIsTrue() {
        _dataState.value = _dataState.value?.copy(postIsDeleted = true)

    }

//--------------------------------------------------------------------------------------------------

    fun save() = viewModelScope.launch {

        try {

            edited.value?.let {
                repository.save(it)
                _postCreated.value = Unit
                _dataState.value = _dataState.value?.copy(postIsAdded = true)
            }

        } catch (e: Exception) {
            _dataState.value = _dataState.value?.copy(postIsAdded = false)
            if (e is AppError) when (e) {
                is ApiError -> {}
                NetworkError -> {}
                UnknownError -> {}
            }
        }
        edited.value = empty
    }

//--------------------------------------------------------------------------------------------------

    fun postAddedIsTrue() {
        _dataState.value = _dataState.value?.copy(postIsAdded = true)

    }

//--------------------------------------------------------------------------------------------------

    fun toastFun(refreshing: Boolean = false) {
        val refreshedPhrase = "Data Refreshed"
        val phrase = listOf(
            "Не удалось, попробуйте позже",
            "Ошибка :(",
            "Что-то пошло нет так..попробуйте снова",
            "Нет связи с сервером",
        )
        val randomPhrase = phrase.random()
        val text = if (!refreshing) randomPhrase else refreshedPhrase
        Toast.makeText(getApplication(), text, Toast.LENGTH_LONG).show()
    }

//--------------------------------------------------------------------------------------------------

    fun edit(post: Post) {
        edited.value = post
    }

    fun changeContent(content: String) {
        val text = content.trim()
        if (edited.value?.content == text) {
            return
        }

        edited.value = edited.value?.copy(content = text)
    }
}

//------------------------------------ End


//                                       - Old code -
//            repository.save(it, object : NMediaCallback<Post> {
//                override fun onSuccess(data: Post) {
//                    _postCreated.value = Unit
//                    _data.value = _data.value?.copy(postIsAdded = true)
//                }
//
//                override fun onError(e: Exception) {
//                    _data.postValue(_data.value?.copy(postIsAdded = false))
//                    // Можно создать SingleLiveEvent и отсюда менять его состояние. (Из вебинара)
//                }
//
//            })

//        repository.removeById(id, object : NMediaCallback<Unit> {
//
//
//            //val old = _data.value?.posts.orEmpty()
//
//            override fun onSuccess(data: Unit) {
//                _data.value = (
//                        _data.value?.copy(posts = _data.value?.posts.orEmpty()
//                            .filter { it.id != id }, postIsDeleted = true
//                        )
//                        )
//            }
//
//            override fun onError(e: Exception) {
//                _data.postValue(_data.value?.copy(postIsDeleted = false))
//
//            }
//        })

//    fun removeLike(id: Long) = viewModelScope.launch {
//
//         val old = _data.value?.posts.orEmpty()
//
//        try {
//            repository.removeLike(id)
//            _dataState.value = (FeedModelState(error = false))
//        } catch (e: Exception) {
//            _dataState.value = (FeedModelState(error = true))
//
//        }
//

//                      функция loadPosts реализованная через threads
//        try {
//            // Данные успешно получены
//            val posts = repository.getAll()
//            FeedModel(posts = posts, empty = posts.isEmpty())
//        } catch (e: IOException) {
//            // Получена ошибка
//            FeedModel(error = true)
//        }.also(_data::postValue)
//}
//        или так (решение из вебинара):
//        val feedModel = try {
//            // Данные успешно получены
//            val posts = repository.getAll()
//            FeedModel(posts = posts, empty = posts.isEmpty())
//        } catch (e: IOException) {
//            // Получена ошибка
//            FeedModel(error = true)
//        }
//        _data.postValue(feedModel)
//        _data.value = feedModel // - так не сработает, будет IllegalStateException: Cannot..

//              функция likeById
//        val old = _data.value?.posts.orEmpty()
//
//        try {
//            repository.likeById(id).let {
//                _data.postValue(
//                    _data.value?.copy(posts = _data.value?.posts.orEmpty()
//                        .map { post -> if (post.id != id) post else it })
//                )
//            }
//
//        } catch (e: IOException) {
//            _data.postValue(_data.value?.copy(posts = old))
//
//        }
//       функция  removeById
//        thread {
//            // Оптимистичная модель
//            val old = _data.value?.posts.orEmpty()
//            _data.postValue(
//                _data.value?.copy(posts = _data.value?.posts.orEmpty()
//                    .filter { it.id != id }
//                )
//            )
//            try {
//                repository.removeById(id)
//            } catch (e: IOException) {
//                _data.postValue(_data.value?.copy(posts = old))
//            }
//        }
//                 функция removeLike
//        thread {
//            val old = _data.value?.posts.orEmpty()
//
//            try {
//                repository.removeLike(id).let {
//                    _data.postValue(
//                        _data.value?.copy(posts = _data.value?.posts.orEmpty()
//                            .map { post -> if (post.id != id) post else it })
//                    )
//                }
//            } catch (e: IOException) {
//                _data.postValue(_data.value?.copy(posts = old))
//            }
//
//
//        }

//                _data.postValue(_data.value?.copy(posts = old
//                    .map { post -> if (post.id != id) post else post.apply { likedByMe != likedByMe }
//                    }))