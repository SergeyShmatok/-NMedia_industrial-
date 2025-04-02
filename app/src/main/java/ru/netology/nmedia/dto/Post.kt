package ru.netology.nmedia.dto


// Иногда в названии указывают прямо, что это объект именно для передачи данных.
// (например, PostDto, а не просто Post)
data class Post( val id: Long,
    val authorId: Long,
    val author: String,
    val content: String,
    val published: String,
    val likedByMe: Boolean,
    val likes: Int = 0,
    val authorAvatar: String,
    var attachment: Attachment? = null,
    val ownedByMe: Boolean = false,

    )

data class Attachment(
    val url: String,
    val type: AttachmentType,
    // val description: String?,
    )

enum class AttachmentType {
    IMAGE
}
