package ru.netology.nmedia.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import ru.netology.nmedia.dto.Post

@Entity
data class PostEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long,
    val author: String,
    val content: String,
    val published: String,
    val likedByMe: Boolean,
    val likes: Int = 0,
    val authorAvatar: String,
    // var attachment: Attachment? = null
    ) {

    fun toDto() = Post(
        id, author, content, published, likedByMe, likes,
        authorAvatar,
    ) // По сути, функция просто возвращает инстанс Post
      // и инициализирует его поля полями своего класса (экземпляра).
      // Хорошая практика в связке с функциями (**) расширения👇

    companion object {
        fun fromDto(dto: Post) =
            PostEntity(dto.id, dto.author, dto.content,
                dto.published, dto.likedByMe, dto.likes, dto.authorAvatar)
    }

}

fun List<PostEntity>.toDto(): List<Post> = map(PostEntity::toDto) // (**)
// fun List<PostEntity>.toDto(): List<Post> = map { it.toDto() } - или так
fun List<Post>.toEntity(): List<PostEntity> = map(PostEntity::fromDto)


//--------------------------------------------------------------------------------------------------
//                                      - Второй вариант -
//  fun toDto(entity: PostEntity) = Post(
//        entity.id, entity.author, entity.content, entity.published, entity.likedByMe, entity.likes,
//        authorAvatar = "",
//    )
//
//    companion object {
//        fun fromDto(dto: Post) =
//            PostEntity(dto.id, dto.author, dto.content, dto.published, dto.likedByMe, dto.likes)
//    }
//--------------------------------------------------------------------------------------------------