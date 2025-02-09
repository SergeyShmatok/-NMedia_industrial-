package ru.netology.nmedia.repository

import kotlinx.coroutines.flow.Flow
import ru.netology.nmedia.dto.Post

interface PostRepositoryFun {
    val data: Flow<List<Post>>

    suspend fun getAll()
    suspend fun save(post: Post)
    suspend fun likeById(id: Long)
    suspend fun removeById(id: Long)
    suspend fun removeLike(id: Long)

    fun getNewerCount(id: Long): Flow<Int>
}

//--------------------------------------------------------------------------------------------------
//                               - Прошлая версия (с коллбэками) -
//
// interface PostRepositoryFun {
//
//    // fun getAll(): List<Post>
//    suspend fun getAllAsync(callback: NMediaCallback<List<Post>>)
//    suspend fun save(post: Post, callback: NMediaCallback<Post>)
//
//    suspend fun likeById(id: Long, callback: NMediaCallback<Post>)
//    suspend fun removeById(id: Long, callback: NMediaCallback<Unit>)
//
//    suspend fun removeLike(id: Long, callback: NMediaCallback<Post>)
//
//interface NMediaCallback <T> {
//    fun onSuccess(data: T)
//    fun onError(e: Exception)
//  }
//
//  С корутинами коллбэки☝️ больше не нужны.
//}
//-------------------------------------- End




