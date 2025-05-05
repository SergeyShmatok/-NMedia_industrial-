package ru.netology.nmedia.repository

import androidx.paging.Pager
import androidx.paging.PagingConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import ru.netology.nmedia.api.ApiService
import ru.netology.nmedia.auth.AppAuth
import ru.netology.nmedia.dao.PostDao
import ru.netology.nmedia.dto.Attachment
import ru.netology.nmedia.dto.AttachmentType
import ru.netology.nmedia.dto.Media
import ru.netology.nmedia.dto.Post
import ru.netology.nmedia.entity.PostEntity
import ru.netology.nmedia.entity.toDto
import ru.netology.nmedia.entity.toEntity
import ru.netology.nmedia.error.ApiError
import ru.netology.nmedia.error.AppError
import ru.netology.nmedia.error.NetworkError
import ru.netology.nmedia.error.UnknownError
import java.io.File
import java.io.IOException
import javax.inject.Inject


class PostRepository @Inject constructor(
    private val dao: PostDao,
    private val apiService: ApiService,
    ) : PostRepositoryFun {

      @Inject
      lateinit var appAuth: AppAuth

//--------------------------------------------------------------------------------------------------

    override var newPost = MutableStateFlow<List<Post>>(emptyList())

//     private val mutex = Mutex()
//    Объект👆, который можно использовать для синхронизации (lock'а) (**)
//    в обращающихся к переменной функциях (если есть несколько)
//    Например:
//        mutex.withLock {
//            newPost.value = null
//        }


    override val data = dao.getAll().map { it.toDto() }.flowOn(Dispatchers.Default)

    override val pagingDate = Pager(
        config = PagingConfig(pageSize = 10, enablePlaceholders = false),
        pagingSourceFactory = { PostPagingSource(apiService, dao) } ).flow


//--------------------------------------------------------------------------------------------------

    override suspend fun getAll() {

        try {

            val response = apiService.getAll()

            if (!response.isSuccessful) throw ApiError(response.code(), response.message())

            val posts = response.body() ?: throw UnknownError
            dao.insert(posts.toEntity())

        } catch (e: IOException) {
             throw NetworkError
        } catch (e: ApiError) {
            throw e
        } catch (e: Exception) {
            throw UnknownError
        }
    }

//--------------------------------------------------------------------------------------------------

    override fun getNewerCount(id: Long) = flow {

        while (true) {  // Цикл прерывается вызовом - CancellationException -
        delay(15_000L)

            val response = apiService.getNewer(id)

//            println(response.code())
//            println(response.message())

            if (!response.isSuccessful) throw ApiError(response.code(), response.message())

            val body = response.body() ?: throw ApiError(response.code(), response.message())
            newPost.value += body

            emit(body.size) } }
        .catch { e -> throw AppError.from(e) }
        .flowOn(Dispatchers.Default)

//--------------------------------------------------------------------------------------------------

    override suspend fun addNewPostsToRoom() {
        newPost.value.toEntity().let { dao.insert(it) }

//            mutex.withLock {        (**)
//            newPost.value = null
//        }
        newPost.value = emptyList()
    }

//--------------------------------------------------------------------------------------------------

     override fun cleanNewPostInRepo() {
    newPost.value = emptyList()
     }

//--------------------------------------------------------------------------------------------------

    override suspend fun likeById(id: Long) {
            dao.likeById(id)
        try {

            val response = apiService.likeById(id)
            if (!response.isSuccessful) throw ApiError(response.code(), response.message())

            response.body() ?: throw UnknownError

        } catch (e: IOException) {
            dao.removeLike(id)
            throw NetworkError
        } catch (e: ApiError) {
            dao.removeLike(id)
            throw e
        } catch (e: Exception) {
            dao.removeLike(id)
            throw UnknownError
        }

    }

//--------------------------------------------------------------------------------------------------

    override suspend fun removeLike(id: Long) {
            dao.removeLike(id)
        try {

            val response = apiService.removeLike(id)
            if (!response.isSuccessful) throw ApiError(response.code(), response.message())

            response.body() ?: throw UnknownError

        } catch (e: IOException) {
            dao.likeById(id)
            throw NetworkError
        } catch (e: ApiError) {
            dao.likeById(id)
            throw e
        } catch (e: Exception) {
            dao.likeById(id)
            throw UnknownError
        }

    }


//--------------------------------------------------------------------------------------------------

    override suspend fun removeById(id: Long) {

        val currentList = dao.getSimpleList()

        dao.removeById(id)

        try {

            val response = apiService.deletePost(id)
            if (!response.isSuccessful) throw ApiError(response.code(), response.message())

            response.body() ?: throw UnknownError

        } catch (e: IOException) {
            dao.insert(currentList)
            throw NetworkError
        } catch (e: ApiError) {
            dao.insert(currentList)
            throw e
        } catch (e: Exception) {
            dao.insert(currentList)
            throw UnknownError
        }
    }

//--------------------------------------------------------------------------------------------------

    override suspend fun save(post: Post) {

       // val currentList = dao.getSimpleList()
       // dao.insert(PostEntity.fromDto(post))

        try {

            val response = apiService.save(post)

            if (!response.isSuccessful) throw ApiError(response.code(), response.message())

            val body = response.body() ?: throw UnknownError

            dao.insert(PostEntity.fromDto(body))

        } catch (e: IOException) {
            throw NetworkError
        } catch (e: ApiError) {
            throw e
        } catch (e: Exception) {
            throw UnknownError
        }
    }

    override suspend fun saveWithAttachment(post: Post, file: File) {

        try {

            val media = upload(file)

            val response = apiService.save(post.copy(attachment = Attachment(media.id, AttachmentType.IMAGE)))

            if (!response.isSuccessful) throw ApiError(response.code(), response.message())

            val body = response.body() ?: throw UnknownError

            dao.insert(PostEntity.fromDto(body))

        } catch (e: IOException) {
            throw NetworkError
        } catch (e: ApiError) {
            throw e
        } catch (e: Exception) {
            throw UnknownError
        }

    }

    private suspend fun upload(file: File): Media =
        apiService.upload(MultipartBody.Part.createFormData("file", file.name, file.asRequestBody()))
                                                // имя сервер будет поставлять своё👆
      // MultipartBody.Part.createFormData — метод, который создаёт экземпляр MultipartBody.Part
      // из библиотеки okhttp3. При использовании этого метода нужно указать имя части (обычно «файл»)
      // и созданный RequestBody. Метод используется для работы с форматом Multipart/Form-Data,
      // который позволяет отправлять двоичные данные и несколько типов данных за один запрос.



     override suspend fun updateUser(login: String, pass: String) {

         try {

             val response = apiService.updateUser(login, pass)

             if (!response.isSuccessful) throw ApiError(response.code(), response.message())

             val body = response.body() ?: throw UnknownError

//             isInitialized()

             appAuth.setAuth(body.get("id").asLong, body.get("token").asString)

         } catch (e: IOException) {
             throw NetworkError
         } catch (e: ApiError) {
             throw e
         } catch (e: Exception) {
             throw UnknownError
         }

     }


}

//------------------------------------ End






























//    override suspend fun removeLike(id: Long) {
//        PostApi.retrofitService.removeLike(id)



// suspend в Kotlin — это аналог коллбэка, реализованный на уровне языка.

// Корутины, к которым относится и suspend, представляют собой идею приостанавливаемых вычислений,
// то есть функцию, которая может приостановить своё выполнение в какой-то момент и возобновить позже.

// Компилятор Kotlin берёт suspend функции и преобразовывает их в оптимизированную версию коллбэков
// с использованием конечной машины состояний.


//--------------------------------------------------------------------------------------------------
//                                  - Вариант с коллбэками -
// class PostRepositoryImpl : PostRepositoryFun {
//
//    private fun <T> objCallback(callback: NMediaCallback<T>) = object : Callback<T> {
//
//        override fun onResponse(call: Call<T>, response: Response<T>) {
//            val body = response.body() ?: run {
//                callback.onError(RuntimeException("body is null"))
//                return
//            }
//            if (response.isSuccessful)
//                callback.onSuccess(body)
//            else
//                callback.onError(RuntimeException("Unsuccessful response from the server"))
//        }
//
//        override fun onFailure(call: Call<T>, e: Throwable) {
//            callback.onError(IOException(e))
//        }
//
//    }
//
//    override fun getAllAsync(callback: NMediaCallback<List<Post>>) {
//        PostApi.service.getAll().enqueue(objCallback(callback))
//    }
//
//    override fun save(post: Post, callback: NMediaCallback<Post>) {
//        PostApi.service.save(post).enqueue(objCallback(callback))
//    }
//
//    override fun likeById(id: Long, callback: NMediaCallback<Post>) {
//        PostApi.service.likeById(id).enqueue(objCallback(callback))
//    }
//
//    override fun removeLike(id: Long, callback: NMediaCallback<Post>) {
//        PostApi.service.removeLike(id).enqueue(objCallback(callback))
//    }
//
//    override fun removeById(id: Long, callback: NMediaCallback<Unit>) {
//        PostApi.service.removeById(id).enqueue(objCallback(callback))
//    }
//
//}
//--------------------------------------------------------------------------------------------------
//    println(Thread.currentThread().name) // (!)
//    // Вывод в консоль имени текущего потока (onResponse),
//    // ответ будет: main.
//    // Т.е. мы можем работать от сюда как из главного потока (postValue в дальнейшем можно не писать).
//    // По архитектуре это влияет так, что из главного потока мы, например, не можем обратиться к серверу,
//    // но можем обратиться к элементам интерфейса.
//
//--------------------------------------------------------------------------------------------------
//
//                               - Old code (OkHttp-implementation) -
//
//--------------------------------------------------------------------------------------------------
//
//import okhttp3.Call
//import okhttp3.Callback
//import okhttp3.OkHttpClient
//import okhttp3.Request
//import okhttp3.RequestBody.Companion.toRequestBody
//import okhttp3.Response
//import okhttp3.internal.EMPTY_REQUEST
//
// private val client = OkHttpClient.Builder() // HTTP-клиент
//        .connectTimeout(30, TimeUnit.SECONDS)
//        .build()
//    private val gson = Gson()
//
//    // private val typeToken1 = object : TypeToken<List<Post>>() {}
//    private val typeToken2 = object : TypeToken<Post>() {}
//
//
//    companion object {
//        private const val BASE_URL = "http://10.0.2.2:9999"
//        private val jsonType = "application/json".toMediaType() // Метод toMediaType() в Kotlin
//        // нужен для получения объекта MediaType, который "описывает" тип содержимого тела запроса
//        // или ответа.}
//
//--------------------------------------------------------------------------------------------------
//
//     override fun getAllAsync(callback: NMediaCallback<List<Post>>) {
//        val request: Request = Request.Builder()
//            .url("${BASE_URL}/api/slow/posts") // тут тип запроса не указывается,
//            // по умолчанию тип запроса - Get.
//            .build()
//
//        return client.newCall(request)
//            .enqueue(object : Callback {
//                override fun onResponse(call: Call, response: Response) {
//
//                    try {
//                        callback.onSuccess(gson.fromJson(response.body?.string(), typeToken1.type))
//                    } catch (e: Exception) {
//                        callback.onError(e)
//                    }
//                }
//                override fun onFailure(call: Call, e: IOException) {
//                    callback.onError(e)
//                }
//
//            })
//    }
//
//--------------------------------------------------------------------------------------------------
//
//     override fun save(post: Post, callback: NMediaCallback<Post>) {
//           val request: Request = Request.Builder()
//            .post(gson.toJson(post).toRequestBody(jsonType))
//            .url("${BASE_URL}/api/slow/posts")
//            .build()
//
//
//        return client.newCall(request)
//            .enqueue(object : Callback {
//                override fun onResponse(call: Call, response: Response) {
//                    try {
//                        callback.onSuccess(gson.fromJson(response.body?.string(), typeToken2.type))
//                    } catch (e: Exception) {
//                        callback.onError(e)
//                    }
//                }
//
//                override fun onFailure(call: Call, e: IOException) {
//                    callback.onError(e)
//                }
//
//            })
//    }
//
// --------------------------------------------------------------------------------------------------
//
//    override fun likeById(id: Long, callback: NMediaCallback<Post>) {
//        val request: Request = Request.Builder()
//            .post(EMPTY_REQUEST)
//            .url("${BASE_URL}/api/posts/$id/likes")
//            .build()
//
//        return client.newCall(request)
//            .enqueue(object : Callback {
//                override fun onResponse(call: Call, response: Response) {
//                    try {
//                        callback.onSuccess(gson.fromJson(response.body?.string(), typeToken2.type))
//                    } catch (e: Exception) {
//                        callback.onError(e)
//                    }
//                }
//
//                override fun onFailure(call: Call, e: IOException) {
//                    callback.onError(e)
//                }
//
//            })
//    }
//
//--------------------------------------------------------------------------------------------------
//
//    override fun removeLike(id: Long, callback: NMediaCallback<Post>) {
//        val request: Request = Request.Builder()
//            .delete()
//            .url("${BASE_URL}/api/posts/$id/likes")
//            .build()
//
//        return client.newCall(request)
//            .enqueue(object : Callback {
//                override fun onResponse(call: Call, response: Response) {
//                    try {
//                        callback.onSuccess(gson.fromJson(response.body?.string(), typeToken2.type))
//                    } catch (e: Exception) {
//                        callback.onError(e)
//                    }
//                }
//
//                override fun onFailure(call: Call, e: IOException) {
//                    callback.onError(e)
//                }
//
//            })
//    }
//
//--------------------------------------------------------------------------------------------------
//
//    override fun removeById(id: Long, callback: NMediaCallback<Unit>) {
//        val request: Request = Request.Builder()
//            .delete()
//            .url("${BASE_URL}/api/slow/posts/$id")
//            .build()
//
//        return client.newCall(request)
//            .enqueue(object : Callback {
//                override fun onResponse(call: Call, response: Response) {
//                    try {
//                        callback.onSuccess(data = Unit)
//                    } catch (e: Exception) {
//                        callback.onError(e)
//                    }
//                }
//
//                override fun onFailure(call: Call, e: IOException) {
//                    callback.onError(e)
//                }
//
//            })
//    }
//
//--------------------------------------------------------------------------------------------------
//                      - Версия с "parse" c передачей функционального типа -
//    private fun <T> forOnResponseFun (response: Response<T>, callback: NMediaCallback<T>, parse: (T) -> T) {
//        // Эта функция только для сокращения кода
//        val body = response.body() ?: run {
//            callback.onError(RuntimeException("body is null"))
//            return }
//        if (response.isSuccessful)
//            callback.onSuccess(parse(body))
//        else
//            callback.onError(RuntimeException("Unsuccessful response from the server"))
//    }
//-------------------------------------- End







