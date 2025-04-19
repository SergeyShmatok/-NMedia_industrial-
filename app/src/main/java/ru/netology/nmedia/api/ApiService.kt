package ru.netology.nmedia.api

import com.google.gson.JsonObject
import okhttp3.MultipartBody
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.GET
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part
import retrofit2.http.Path
import ru.netology.nmedia.dto.Media
import ru.netology.nmedia.dto.Post
import ru.netology.nmedia.dto.PushToken


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

    @POST("users/push-tokens")
    suspend fun sendPushToken(@Body token: PushToken)
//     Если код ответа неважен, если нужен только successful (200-299),
//     можно "Response" не возвращать.








    // Если важен код ответа, можно использовать Response (обёртку),
    // если нет, то сразу - результат.

    // Когда Retrofit анализирует сигнатуру методов и видит модификатор suspend,
    // тогда понимает, что надо выполнять код в корутинах.
    // suspend вызывает функцию, приостанавливая текущий поток, соответственно,
    // при использовании корутин не нужно использовать коллбэки. - Call убираются,
    // Retrofit сам во всём "разберётся".
}


// object Api {
//    val retrofitService by lazy {
//        retrofit.create<ApiService>() // Создайте реализацию конечных точек API,
//        // определенных интерфейсом службы. Относительный путь для данного метода получается из аннотации к методу,
//        // описывающему тип запроса. Встроенные методы: GET, PUT, POST, PATCH, HEAD, DELETE и OPTIONS.
//    }
//
//
//}



//--------------------------------------------- End
































//  //    @GET("post/{id}")
//    // fun getById(@Path("id") id: Long): Call<List<Post>>