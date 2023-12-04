package ru.fox.diplom.repository

import androidx.paging.PagingData
import kotlinx.coroutines.flow.Flow
import ru.fox.diplom.dto.FeedItem
import ru.fox.diplom.dto.Media
import ru.fox.diplom.dto.MediaUpload
import ru.fox.diplom.dto.Post

interface PostRepository {
    val data: Flow<PagingData<FeedItem>>
    fun getNewPostsCount(id: Long): Flow<Int>
 //   suspend fun getAll()
    suspend fun save(post: Post)
    suspend fun saveWithAttachment(post: Post, upload: MediaUpload)
    suspend fun removeById(id: Long)
    suspend fun likePost(post: Post)
    suspend fun updateFeed()
    suspend fun upload(upload: MediaUpload): Media
}