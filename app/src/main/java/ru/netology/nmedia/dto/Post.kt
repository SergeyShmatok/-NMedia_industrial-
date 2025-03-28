package ru.netology.nmedia.dto

data class Post(
    val id: Long,
    val author: String,
    val content: String,
    val published: String,
    val likedByMe: Boolean,
    val likes: Int = 0,
    val authorAvatar: String,
    var attachment: Attachment? = null,

    )

data class Attachment(
    val url: String,
    val type: AttachmentType,
    // val description: String?,
    )

enum class AttachmentType {
    IMAGE
}
