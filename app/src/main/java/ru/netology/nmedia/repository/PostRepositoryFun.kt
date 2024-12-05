package ru.netology.nmedia.repository

import ru.netology.nmedia.dto.Post

interface PostRepositoryFun {
    fun getAll(): List<Post>
    fun likeById(id: Long): Post
    fun save(post: Post)
    fun removeById(id: Long)
    fun removeLike(id: Long): Post
}
