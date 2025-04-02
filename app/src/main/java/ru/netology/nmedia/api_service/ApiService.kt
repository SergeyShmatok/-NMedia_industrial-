package ru.netology.nmedia.api_service

import com.google.gson.JsonObject
import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.create
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.GET
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part
import retrofit2.http.Path
import ru.netology.nmedia.BuildConfig
import ru.netology.nmedia.auth.AppAuth
import ru.netology.nmedia.dto.Media
import ru.netology.nmedia.dto.Post


private const val BASE_URL = "${BuildConfig.BASE_URL}/api/slow/"


private val logger = HttpLoggingInterceptor().apply {
    if (BuildConfig.DEBUG)
        level = HttpLoggingInterceptor.Level.BODY
}


private val okhttp = OkHttpClient.Builder()
    .addInterceptor(logger) // Последовательность logger'ов имеет значение (!)
    .addInterceptor{ chain -> // Модификация сетевых запросов через интерцептор
        chain.proceed(
            AppAuth.getInstance().authState.value?.token?.let {
                chain.request().newBuilder().addHeader("Authorization", it)
                    .build() } ?: chain.request()) // Если нет токена, тогда обычный "запрос".
    } // addHeader добавляет заголовок с именем и значением. Предпочитайте этот метод для многозначных заголовков, таких как «Cookie».
    .build()


private val retrofit = Retrofit.Builder()
    .client(okhttp)
    .baseUrl(BASE_URL)
    .addConverterFactory(GsonConverterFactory.create())
    .build()


interface ApiService {
    @FormUrlEncoded
    @POST("users/authentication")
    suspend fun updateUser(@Field("login") login: String, @Field("pass") pass: String): Response<JsonObject>

    @GET("posts")
    suspend fun getAll(): Response<List<Post>>

    @POST("posts")
    suspend fun save(@Body post: Post): Response<Post>

    @DELETE("posts/{id}")
    suspend fun deletePost(@Path("id") id: Long): Response<Unit>

    @POST("posts/{id}/likes")
    suspend fun likeById(@Path("id") id: Long): Response<Post>

    @DELETE("posts/{id}/likes")
    suspend fun removeLike(@Path("id") id: Long): Response<Post>

    @GET("posts/{id}/newer")
    suspend fun getNewer(@Path("id") id: Long): Response<List<Post>>

    @Multipart
    @POST("media")
    suspend fun upload(@Part part: MultipartBody.Part): Media
    // Если важен код ответа, можно использовать Response (обёртку),
    // если нет, то сразу - результат.

    // Когда Retrofit анализирует сигнатуру методов и видит модификатор suspend,
    // тогда понимает, что надо выполнять код в корутинах.
    // suspend вызывает функцию, приостанавливая текущий поток, соответственно,
    // при использовании корутин не нужно использовать коллбэки. - Call убираются,
    // Retrofit сам во всём "разберётся".
}


object PostApi {
    val retrofitService by lazy {
        retrofit.create<ApiService>() // Создайте реализацию конечных точек API,
        // определенных интерфейсом службы. Относительный путь для данного метода получается из аннотации к методу,
        // описывающему тип запроса. Встроенные методы: GET, PUT, POST, PATCH, HEAD, DELETE и OPTIONS.
    }


}



//--------------------------------------------- End
































//  //    @GET("post/{id}")
//    // fun getById(@Path("id") id: Long): Call<List<Post>>