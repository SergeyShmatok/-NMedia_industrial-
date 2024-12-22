package ru.netology.nmedia.viewmodel

import android.app.Application
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
    published = ""
)

class PostViewModel(application: Application) : AndroidViewModel(application) {
    // упрощённый вариант
    private val repository: PostRepositoryFun = PostRepositoryImpl()
    private val _data = MutableLiveData(FeedModel())
    val data: LiveData<FeedModel>
        get() = _data
    private val edited = MutableLiveData(empty)
    private val _postCreated = SingleLiveEvent<Unit>()
    val postCreated: LiveData<Unit>
        get() = _postCreated

    init {
        loadPosts()
    }

    fun loadPosts() {
        // thread { - убираем thread, потому что многопоточность теперь будет происходить на стороне OkHttp.
        // Начинаем загрузку
        _data.postValue(FeedModel(loading = true))

        repository.getAllAsync(object : NMediaCallback<List<Post>> {
            override fun onSuccess(data: List<Post>) {
                _data.postValue(FeedModel(posts = data, empty = data.isEmpty()))
            }

            override fun onError(e: Exception) {
                _data.postValue(FeedModel(error = true))
            }

        }
        )

    }

    fun save() {
        edited.value?.let {

            repository.save(it, object : NMediaCallback<Post> {
                override fun onSuccess(data: Post) {}

                override fun onError(e: Exception) {
                    _data.postValue(FeedModel(postIsAdded = false))
                }

            }
            )
            _postCreated.postValue(Unit)

        }
        edited.value = empty
    }


    fun likeById(id: Long) {

        repository.likeById(id, object : NMediaCallback<Post> {
            val old = _data.value?.posts.orEmpty()

            override fun onSuccess(data: Post) {
                _data.postValue(
                    _data.value?.copy(posts = _data.value?.posts.orEmpty()
                        .map { post -> if (post.id != id) post else data })
                )
            }

            override fun onError(e: Exception) {
                _data.postValue(_data.value?.copy(posts = old))
            }

        }
        )

    }

    fun removeLike(id: Long) {

        repository.removeById(id, object : NMediaCallback<Post> {
            val old = _data.value?.posts.orEmpty()

            override fun onSuccess(data: Post) {
                _data.postValue(
                    _data.value?.copy(posts = _data.value?.posts.orEmpty()
                        .map { post -> if (post.id != id) post else data })
                )
            }

            override fun onError(e: Exception) {
                _data.postValue(_data.value?.copy(posts = old))
            }

        }
        )

    }

    fun removeById(id: Long) {
        repository.removeById(id, object : NMediaCallback<Post> {
            val old = _data.value?.posts.orEmpty()

            override fun onSuccess(data: Post) {
                _data.postValue(
                    _data.value?.copy(posts = _data.value?.posts.orEmpty()
                        .filter { it.id != id }
                    )
                )
            }

            override fun onError(e: Exception) {
                _data.postValue(_data.value?.copy(posts = old))
            }

        })

    }

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

//                      функция loadPosts реализованная через threads в прошлый раз
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