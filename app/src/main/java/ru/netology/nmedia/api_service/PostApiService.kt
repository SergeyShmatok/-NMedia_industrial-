package ru.netology.nmedia.api_service

import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import okhttp3.logging.HttpLoggingInterceptor.Level
import retrofit2.Call
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.create
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import ru.netology.nmedia.BuildConfig
import ru.netology.nmedia.dto.Post

private val logger = HttpLoggingInterceptor().apply {
    if (BuildConfig.DEBUG)
    level = Level.BODY
}

private val client = OkHttpClient.Builder()
    .addInterceptor(logger)
    .build()

private val retrofit = Retrofit.Builder()
    .client(client)
    .baseUrl("${BuildConfig.BASE_URL}/api/slow/")
    .addConverterFactory(GsonConverterFactory.create())
    .build()

interface PostApiService {
    @GET("posts")
    fun getAll(): Call<List<Post>>

//    @GET("post/{id}")
//    fun getById(@Path("id") id: Long): Call<List<Post>>

    @DELETE("posts/{id}")
    fun removeById(@Path("id") id: Long): Call<Unit>

    @DELETE("posts/{id}/likes")
    fun removeLike(@Path("id") id: Long): Call<Post>

    @POST("posts/{id}/likes")
    fun likeById(@Path("id") id: Long): Call<Post>

    @POST("posts")
    fun save(@Body post: Post): Call<Post>

}

object PostApi {
    val service by lazy {
        retrofit.create<PostApiService>() // Создайте реализацию конечных точек API,
        // определенных интерфейсом службы. Относительный путь для данного метода получается из аннотации к методу,
        // описывающему тип запроса. Встроенные методы: GET, PUT, POST, PATCH, HEAD, DELETE и OPTIONS.

    }
}