package ru.netology.nmedia.viewmodel

import android.content.Context
import android.net.Uri
import android.widget.Toast
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.SharingStarted.Companion.WhileSubscribed
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import ru.netology.nmedia.auth.AppAuth
import ru.netology.nmedia.dto.Post
import ru.netology.nmedia.error.ApiError
import ru.netology.nmedia.error.AppError
import ru.netology.nmedia.error.DbError
import ru.netology.nmedia.error.NetworkError
import ru.netology.nmedia.model.FeedModel
import ru.netology.nmedia.model.FeedModelState
import ru.netology.nmedia.model.PhotoModel
import ru.netology.nmedia.repository.PostRepositoryFun
import java.io.File
import javax.inject.Inject


private val empty = Post(
    id = 0,
    content = "",
    author = "",
    likedByMe = false,
    likes = 0,
    published = "",
    authorAvatar = "",
    attachment = null,
    authorId = 0,
)

// @Deprecated(message = "Не использовать", replaceWith = ReplaceWith...) (Из вебинара)
@HiltViewModel
class PostViewModel @Inject constructor (
    private val repository: PostRepositoryFun,
    private val appAuth: AppAuth,
    private val applicationContext: Context,

    ) : ViewModel() {

        private val _pagingDate: Flow<PagingData<Post>> =
        repository.pagingDate
            .cachedIn(viewModelScope)
            .catch { e -> throw AppError.from(e)} // В этом
        // задании данные об авторстве поста (ownedByMe) рассчитываются на сервере.

    val pagingData: Flow<PagingData<Post>>
        get() = _pagingDate

    private val _data: StateFlow<FeedModel> =
        repository.data.map { posts->
            FeedModel(
                posts.map { it }
            )
        }.catch { e -> throw AppError.from(e)}
            .stateIn(
        scope = viewModelScope,
        started = WhileSubscribed(5000),
            initialValue = FeedModel())

    val data: StateFlow<FeedModel>
        get() = _data

    private val _dataState = MutableStateFlow(FeedModelState())
    val dataState: StateFlow<FeedModelState>
        get() = _dataState

    private val edited = MutableStateFlow(empty)

    private val _postCreated = MutableStateFlow<Unit?>(null)
    val postCreated: Flow<Unit>
        get() = _postCreated.asStateFlow().filterNotNull()

    @OptIn(ExperimentalCoroutinesApi::class)
    private val _newerCount = data.flatMapLatest { // Этот механизм можно применять для других задач,
        // например, оповещать в UI о наличии интернета
        // (как-то отображать через элементы интерфейса).
        repository.getNewerCount(it.posts.firstOrNull()?.id ?: 0L)
            .catch { e ->
                if (e is NetworkError) {
                    cleanNewPost(); println(e)
                    _dataState.value = FeedModelState(error = true)
                } else throw AppError.from(e)
            }
    } .stateIn(
        scope = viewModelScope,
        started = SharingStarted.Lazily,
        false)

    val newerCount: StateFlow<Any>
        get() = _newerCount

    private var _newPostData = repository.newPost

    val newPostData: StateFlow<List<Post>?>
        get() = _newPostData

    private val _photo = MutableStateFlow<PhotoModel?>(null)
    val photo: StateFlow<PhotoModel?>
        get() = _photo


//--------------------------------------------------------------------------------------------------

    init { loadPosts() }

//--------------------------------------------------------------------------------------------------

    fun changePhoto (uri: Uri, file: File) {
        _photo.value = PhotoModel(uri, file)
    }

    fun removePhoto () {
        _photo.value = null
    }

//--------------------------------------------------------------------------------------------------

    fun loadPosts(refreshed: Boolean = false) = viewModelScope.launch {

        // viewModelScope описана на главном потоке (Dispatchers.Main.immediate),
        // но асинхронно с ним самим. "Корутины — это потоки исполнения кода,
        // которые организуются поверх системных потоков".
        // _data.value = (FeedModel(loading = true))

        val state = if (refreshed) FeedModelState(refreshing = true)
        else FeedModelState(loading = true)

        try {
            _dataState.value = state
            repository.getAll()
            _dataState.value = FeedModelState()
        } catch (e: Exception) {
            _dataState.value = FeedModelState(error = true)
            if (e is AppError) when (e) {
                is ApiError -> {} // Можно поставить разные "флаги" на разные ошибки
                is NetworkError -> {} // --//--
                is DbError -> {}
                else -> {} // --//--

            }
        }
    }
//--------------------------------------------------------------------------------------------------

   fun newPostsIsVisible() = viewModelScope.launch {
       repository.addNewPostsToRoom()
   }

//--------------------------------------------------------------------------------------------------

    fun refreshing() {
        loadPosts(true)

    }

//--------------------------------------------------------------------------------------------------

    fun likeById(id: Long) = viewModelScope.launch {
        // viewModelScope автоматически отменится в случае закрытия активити

        try {
            repository.likeById(id)
            _dataState.value = FeedModelState(likeError = false)
        } catch (e: Exception) {
            _dataState.value = FeedModelState(likeError = true)

            }
        }


//--------------------------------------------------------------------------------------------------

    fun removeLike(id: Long) = viewModelScope.launch {


        try {
            repository.removeLike(id)
            _dataState.value = FeedModelState(likeError = false)
        } catch (e: Exception) {
            _dataState.value = FeedModelState(likeError = true)

        }
    }

//--------------------------------------------------------------------------------------------------

    fun cleanModel() {
        _dataState.value = FeedModelState()
    }

    fun cleanNewPost() {
        repository.cleanNewPostInRepo()
    }

    fun postCreatedIsNull() {
        _postCreated.value = null
    }

//--------------------------------------------------------------------------------------------------

    fun removeById(id: Long) = viewModelScope.launch {

        try {
            repository.removeById(id)
            _dataState.value = FeedModelState(postIsDeleted = true)

        } catch (e: Exception) {
            _dataState.value = FeedModelState(postIsDeleted = false)

        }
    }

//--------------------------------------------------------------------------------------------------

    fun save() = viewModelScope.launch {

        try {
            edited.value.let { post ->
                _postCreated.value = Unit
                 photo.value?.let {
                    repository.saveWithAttachment(post, it.file)
                } ?: repository.save(post)

                _dataState.value = FeedModelState(postIsAdded = true)
            }

        } catch (e: Exception) {
            _dataState.value = FeedModelState(postIsAdded = false)

        }
        edited.value = empty
    }

//--------------------------------------------------------------------------------------------------

    fun toastFun(refreshing: Boolean = false, pickError: Boolean = false) {
        val refreshedPhrase = "Data Refreshed"
        val pickErrorPhrase = "Photo pick error"
        val phrase = listOf(
            "Не удалось, попробуйте позже",
            "Ошибка :(",
            "Что-то пошло нет так..попробуйте снова",
            "Ошибка соединения",
        )

        val randomPhrase = phrase.random()
        val text = when {
                refreshing -> refreshedPhrase
                pickError -> pickErrorPhrase
                else -> randomPhrase
        }
        Toast.makeText(applicationContext, text, Toast.LENGTH_LONG).show()
    }

//--------------------------------------------------------------------------------------------------

    fun edit(post: Post) {
        edited.value = post
    }

    fun changeContent(content: String) {
        val text = content.trim()
        if (edited.value.content == text) {
            return
        }

        edited.value = edited.value.copy(content = text)
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