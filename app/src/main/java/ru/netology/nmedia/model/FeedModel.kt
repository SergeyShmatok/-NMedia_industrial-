package ru.netology.nmedia.model

import ru.netology.nmedia.dto.Post

data class FeedModel(
    val posts: List<Post> = emptyList(),
    val loading: Boolean = false,
    val error: Boolean = false,
    val empty: Boolean = false,
    val refreshing: Boolean = false,
    val postIsAdded: Boolean = true,
    val likeError: Boolean = false,
    val postIsDeleted: Boolean = true,
)


