package ru.netology.nmedia.api_service

import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import okhttp3.logging.HttpLoggingInterceptor.Level
import retrofit2.Response
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
    suspend fun getAll(): Response <List<Post>>


    @GET("posts/{id}/newer")
    suspend fun getNewer(@Path("id") id: Long): Response<List<Post>>



    @DELETE("posts/{id}")
    suspend fun removeById(@Path("id") id: Long): Response <Unit>

    @DELETE("posts/{id}/likes")
    suspend fun removeLike(@Path("id") id: Long): Response <Post>

    @POST("posts/{id}/likes")
    suspend fun likeById(@Path("id") id: Long): Response <Post>

    @POST("posts")
    suspend fun save(@Body post: Post): Response <Post>

    // Когда Retrofit анализирует сигнатуру методов и видит модификатор suspend,
    // тогда понимает, что надо выполнять код в корутинах.
    // suspend вызывает функцию, приостанавливая текущий поток, соответственно,
    // при использовании корутин не нужно использовать коллбэки. - Call убираются,
    // Retrofit сам во всём "разберётся".
}

object PostApi {
    val retrofitService by lazy {
        retrofit.create<PostApiService>() // Создайте реализацию конечных точек API,
        // определенных интерфейсом службы. Относительный путь для данного метода получается из аннотации к методу,
        // описывающему тип запроса. Встроенные методы: GET, PUT, POST, PATCH, HEAD, DELETE и OPTIONS.

    }
}


//  //    @GET("post/{id}")
//    // fun getById(@Path("id") id: Long): Call<List<Post>>