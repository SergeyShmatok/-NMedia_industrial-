package ru.netology.nmedia.repository

import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import okhttp3.Call
import okhttp3.Callback
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Response
import okhttp3.internal.EMPTY_REQUEST
import ru.netology.nmedia.dto.Post
import ru.netology.nmedia.repository.PostRepositoryFun.*
import java.io.IOException
import java.util.concurrent.TimeUnit


class PostRepositoryImpl : PostRepositoryFun {
    private val client = OkHttpClient.Builder() // HTTP-клиент
        .connectTimeout(30, TimeUnit.SECONDS)
        .build()
    private val gson = Gson()
    private val typeToken1 = object : TypeToken<List<Post>>() {}
    private val typeToken2 = object : TypeToken<Post>() {}


    companion object {
        private const val BASE_URL = "http://10.0.2.2:9999"
        private val jsonType = "application/json".toMediaType() // Метод toMediaType() в Kotlin
        // нужен для получения объекта MediaType, который "описывает" тип содержимого тела запроса
        // или ответа.
    }

    override fun getAllAsync(callback: NMediaCallback<List<Post>>) {
        val request: Request = Request.Builder()
            .url("${BASE_URL}/api/slow/posts") // тут тип запроса не указывается,
            // по умолчанию тип запроса - Get.
            .build()

        return client.newCall(request)
            .enqueue(object : Callback {
                override fun onResponse(call: Call, response: Response) {
                    try {
                        callback.onSuccess(gson.fromJson(response.body?.string(), typeToken1.type))
                    } catch (e: Exception) {
                        callback.onError(e)
                    }
                }
                override fun onFailure(call: Call, e: IOException) {
                    callback.onError(e)
                }

            })

    }

    override fun save(post: Post, callback: NMediaCallback<Post>) {
        val request: Request = Request.Builder()
            .post(gson.toJson(post).toRequestBody(jsonType))
            .url("${BASE_URL}/api/slow/posts")
            .build()


        return client.newCall(request)
            .enqueue(object : Callback {
                override fun onResponse(call: Call, response: Response) {

                }
                override fun onFailure(call: Call, e: IOException) {
                    callback.onError(e)
                }

            })

    }

    override fun likeById(id: Long, callback: NMediaCallback<Post>) {
        val request: Request = Request.Builder()
            .post(EMPTY_REQUEST)
            .url("${BASE_URL}/api/posts/$id/likes")
            .build()

        return client.newCall(request)
            .enqueue(object : Callback {
                override fun onResponse(call: Call, response: Response) {
                    try {
                        callback.onSuccess(gson.fromJson(response.body?.string(), typeToken2.type))
                    } catch (e: Exception) {
                        callback.onError(e)
                    }
                }
                override fun onFailure(call: Call, e: IOException) {
                    callback.onError(e)
                }

            })

    }


    override fun removeLike(id: Long, callback: NMediaCallback<Post>) {
        val request: Request = Request.Builder()
            .delete()
            .url("${BASE_URL}/api/posts/$id/likes")
            .build()

        return client.newCall(request)
            .enqueue(object : Callback {
                override fun onResponse(call: Call, response: Response) {
                    try {
                        callback.onSuccess(gson.fromJson(response.body?.string(), typeToken2.type))
                    } catch (e: Exception) {
                        callback.onError(e)
                    }
                }
                override fun onFailure(call: Call, e: IOException) {
                    callback.onError(e)
                }

            })
    }


    override fun removeById(id: Long, callback: NMediaCallback<Post>) {
        val request: Request = Request.Builder()
            .delete()
            .url("${BASE_URL}/api/slow/posts/$id")
            .build()

        return client.newCall(request)
            .enqueue(object : Callback {
                override fun onResponse(call: Call, response: Response) {
                    try {
                        callback.onSuccess(gson.fromJson(response.body?.string(), typeToken2.type))
                    } catch (e: Exception) {
                        callback.onError(e)
                    }
                }
                override fun onFailure(call: Call, e: IOException) {
                    callback.onError(e)
                }

            })


    }


}


//    override fun getAll(): List<Post> {
//        val request: Request = Request.Builder()
//            .url("${BASE_URL}/api/slow/posts") // тут тип запроса не указывается,
//            // по умолчанию тип запроса - Get.
//            .build()
//
//        return client.newCall(request)
//            .execute()
//            .let { it.body?.string() ?: throw RuntimeException("body is null") }
//            .let {
//                gson.fromJson(it, typeToken1.type) // первым параметром принимает String
//            }
//    }