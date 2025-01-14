package ru.netology.nmedia.viewmodel

import android.app.Application
import android.view.Gravity
import android.widget.Toast
import androidx.lifecycle.*
import ru.netology.nmedia.dto.Post
import ru.netology.nmedia.model.FeedModel
import ru.netology.nmedia.repository.*
import ru.netology.nmedia.repository.PostRepositoryFun.*
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
    private val repository: PostRepositoryFun = PostRepositoryImpl()
    private val _data = MutableLiveData(FeedModel())
    val data: LiveData<FeedModel>
        get() = _data
    private val edited = MutableLiveData(empty)
    private val _postCreated = SingleLiveEvent<Unit>()
    val postCreated: LiveData<Unit>
        get() = _postCreated

//--------------------------------------------------------------------------------------------------

    init {
        loadPosts()
    }

    fun loadPosts() {
        _data.value = (FeedModel(loading = true))

        repository.getAllAsync(object : NMediaCallback<List<Post>> {
            override fun onSuccess(data: List<Post>) {
                _data.value = (FeedModel(posts = data, empty = data.isEmpty()))
            }

            override fun onError(e: Exception) {
                _data.postValue(FeedModel(error = true))
            }

        })

    }

//--------------------------------------------------------------------------------------------------

    fun likeById(id: Long) {

        val old = _data.value?.posts.orEmpty()

        _data.value = (
                _data.value?.copy(posts = _data.value?.posts.orEmpty()
                    .map { post -> if (post.id != id) post else post.copy(
                        likedByMe = !post.likedByMe,
                        likes = post.likes + 1)}, likeError = false)
                   )

        repository.likeById(id, object : NMediaCallback<Post> {

            override fun onSuccess(data: Post) {}

            override fun onError(e: Exception) {

                _data.value = _data.value?.copy(posts = old, likeError = true)

            }
        })

    }

//--------------------------------------------------------------------------------------------------

    fun likeErrorIsFalse() {
        _data.postValue(_data.value?.copy(likeError = false))
    }

//--------------------------------------------------------------------------------------------------

    fun removeLike(id: Long) {

        val old = _data.value?.posts.orEmpty()

        _data.value = (
                _data.value?.copy(posts = _data.value?.posts.orEmpty()
                    .map { post -> if (post.id != id) post else post.copy(
                        likedByMe = !post.likedByMe,
                        likes = post.likes - 1)}, likeError = false)
                )

        repository.removeLike(id, object : NMediaCallback<Post> {

            override fun onSuccess(data: Post) {}

            override fun onError(e: Exception) {

                _data.value = _data.value?.copy(posts = old, likeError = true)

            }
        })

    }

//--------------------------------------------------------------------------------------------------

    fun removeById(id: Long) {
        repository.removeById(id, object : NMediaCallback<Unit> {

            //val old = _data.value?.posts.orEmpty()

            override fun onSuccess(data: Unit) {
                _data.value = (
                    _data.value?.copy(posts = _data.value?.posts.orEmpty()
                        .filter { it.id != id }, postIsDeleted = true)
                        )
            }

            override fun onError(e: Exception) {
                _data.postValue(_data.value?.copy(postIsDeleted = false))
            }
        })

    }

//--------------------------------------------------------------------------------------------------

    fun postDelIsTrue() {
        _data.postValue(_data.value?.copy(postIsDeleted = true))
    }

//--------------------------------------------------------------------------------------------------

    fun save() {
        edited.value?.let {

            repository.save(it, object : NMediaCallback<Post> {
                override fun onSuccess(data: Post) {
                    _postCreated.value = Unit
                    _data.value = _data.value?.copy(postIsAdded = true)
                }

                override fun onError(e: Exception) {
                    _data.postValue(_data.value?.copy(postIsAdded = false))
                    // Можно создать SingleLiveEvent и отсюда менять его состояние. (Из вебинара)
                }

            })

        }
        edited.value = empty
    }

//--------------------------------------------------------------------------------------------------

    fun postIsAddedTrue() {
        _data.postValue(_data.value?.copy(postIsAdded = true))
    }

//--------------------------------------------------------------------------------------------------

    fun toastFun(refreshed: Boolean = false) {
        val refreshedPhrase = "Data Refreshed"
        val phrase = listOf(
            "Не удалось, попробуйте позже",
            "Ошибка :(",
            "Что-то пошло нет так..попробуйте снова",
            "Нет связи с сервером",
        )
        val randomPhrase = phrase.random()
        val text = if (!refreshed) randomPhrase else refreshedPhrase
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