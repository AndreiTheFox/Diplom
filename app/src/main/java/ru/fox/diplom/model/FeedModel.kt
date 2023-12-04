package ru.fox.diplom.model

import ru.fox.diplom.dto.Post

data class FeedModel(
    val posts: List<Post> = emptyList(),
    val empty: Boolean = false,
)