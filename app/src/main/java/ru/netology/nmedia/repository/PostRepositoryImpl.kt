package ru.netology.nmedia.repository

import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import ru.netology.nmedia.api_service.PostApi
import ru.netology.nmedia.dto.Post
import ru.netology.nmedia.repository.PostRepositoryFun.*
import java.io.IOException

class PostRepositoryImpl : PostRepositoryFun {


    private fun <T> objCallback(callback: NMediaCallback<T>) = object : Callback<T> {
        override fun onResponse(call: Call<T>, response: Response<T>) {
            val body = response.body() ?: run {
                callback.onError(RuntimeException("body is null"))
                return
            }
            if (response.isSuccessful)
                callback.onSuccess(body)
            else
                callback.onError(RuntimeException("Unsuccessful response from the server"))
        }

        override fun onFailure(call: Call<T>, e: Throwable) {
            callback.onError(IOException(e))
        }

    }


    override fun getAllAsync(callback: NMediaCallback<List<Post>>) {
        PostApi.service.getAll().enqueue(objCallback(callback))
    }

    override fun save(post: Post, callback: NMediaCallback<Post>) {
        PostApi.service.save(post).enqueue(objCallback(callback))
    }

    override fun likeById(id: Long, callback: NMediaCallback<Post>) {
        PostApi.service.likeById(id).enqueue(objCallback(callback))
    }

    override fun removeLike(id: Long, callback: NMediaCallback<Post>) {
        PostApi.service.removeLike(id).enqueue(objCallback(callback))

    }

    override fun removeById(id: Long, callback: NMediaCallback<Unit>) {
        PostApi.service.removeById(id).enqueue(objCallback(callback))
    }


}


//--------------------------------------------------------------------------------------------------

//    println(Thread.currentThread().name) // (!)
//    // Вывод в консоль имени текущего потока (onResponse),
//    // ответ будет: main.
//    // Т.е. мы можем работать от сюда как из главного потока (postValue в дальнейшем можно не писать).
//    // По архитектуре это влияет так, что из главного потока мы, например, не можем обратиться к серверу,
//    // но можем обратиться к элементам интерфейса.

//--------------------------------------------------------------------------------------------------
//
//    private fun <T> forOnResponse(response: Response<T>, callback: NMediaCallback<T>) {
//        // Эта функция только для сокращения кода в onRespons'ах (!)
//        val body = response.body() ?: run {
//            callback.onError(RuntimeException("body is null"))
//            return
//        }
//        if (response.isSuccessful)
//            callback.onSuccess(body)
//        else
//            callback.onError(RuntimeException("Unsuccessful response from the server"))
//    }
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
//-------------------------------------END

//            - Версия с "parse" c передачей функционального типа -
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
//