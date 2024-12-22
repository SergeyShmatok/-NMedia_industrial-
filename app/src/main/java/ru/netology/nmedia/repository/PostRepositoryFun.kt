package ru.netology.nmedia.repository

import ru.netology.nmedia.dto.Post

interface PostRepositoryFun {
    //fun getAll(): List<Post>
    fun getAllAsync(callback: NMediaCallback<List<Post>>)
    fun save(post: Post, callback: NMediaCallback<Post>)

    fun likeById(id: Long, callback: NMediaCallback<Post>)
    fun removeById(id: Long, callback: NMediaCallback<Unit>)

    fun removeLike(id: Long, callback: NMediaCallback<Post>)

interface NMediaCallback <T> {
    fun onSuccess(data: T)
    fun onError(e: Exception)
}

}


