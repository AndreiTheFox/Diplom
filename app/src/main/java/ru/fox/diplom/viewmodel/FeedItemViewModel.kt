package ru.fox.diplom.viewmodel

import android.net.Uri
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import androidx.paging.insertSeparators
import androidx.paging.map
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import ru.fox.diplom.auth.AppAuth

import ru.fox.diplom.dto.Ad
import ru.fox.diplom.dto.FeedItem
import ru.fox.diplom.dto.MediaUpload
import ru.fox.diplom.dto.Post
import ru.fox.diplom.model.FeedModelState
import ru.fox.diplom.model.PhotoModel
import ru.fox.diplom.repository.PostRepository
import ru.fox.diplom.util.SingleLiveEvent
import java.io.File
import javax.inject.Inject
import kotlin.random.Random

private val empty = Post(id = 0)

@HiltViewModel
class FeedItemViewModel @Inject constructor
    (
    private val repository: PostRepository,
    appAuth: AppAuth
) : ViewModel() {
    private val cached: Flow<PagingData<FeedItem>> = repository
        .data
        .map { pagingData ->
            pagingData.insertSeparators(
                generator = { before, _ ->
                    if (before == null) {
                        //the end of the list
                        return@insertSeparators null
                    }

                    if (before.id.rem(5) == 0L) {
                        Ad(
                            Random.nextLong(),
                            "https://netology.ru",
                            "figma.jpg"
                        )
                    } else null
                }
            )
        }
        .cachedIn(viewModelScope)

    val data: Flow<PagingData<FeedItem>> = appAuth.authStateFlow
        .flatMapLatest { token ->
            cached.map { pagingData ->
                pagingData.map { item ->
                    if (item is Post) {
                        item.copy(ownedByMe = item.authorId == token.id)
                    } else item
                }
            }
        }.flowOn(Dispatchers.Default)

//    val newPostsCount: LiveData<Int> = data.switchMap {
//        repository.getNewPostsCount(it.posts.firstOrNull()?.id ?: 0L)
//            .asLiveData(Dispatchers.Default)
//    }

//    fun updateFeed() = viewModelScope.launch {
//        repository.updateFeed()
//    }

    //Feed state: loading, error, refreshing
    private val _dataState = MutableLiveData<FeedModelState>()
    val dataState: LiveData<FeedModelState>
        get() = _dataState

    //Photo attachment
    private val _photo = MutableLiveData<PhotoModel>()
    val photo: LiveData<PhotoModel>
        get() = _photo

    //Post for edit
    private val edited = MutableLiveData(empty)
    private val _postCreated = SingleLiveEvent<Unit>()
    val postCreated: LiveData<Unit>
        get() = _postCreated

    fun setPhoto(uri: Uri?, file: File?) {
        _photo.value = PhotoModel(uri, file)
    }

    fun clearPhoto() {
        _photo.value = PhotoModel()
    }

    fun removeById(id: Long) = viewModelScope.launch {
        try {
            _dataState.value = FeedModelState(refreshing = true)
            repository.removeById(id)
            _dataState.value = FeedModelState()
        } catch (e: Exception) {
            _dataState.value = FeedModelState(error = true)
        }
    }

    fun likeById(post: Post) = viewModelScope.launch {
        try {
            repository.likePost(post)
            _dataState.value = FeedModelState()
        } catch (e: Exception) {
            _dataState.value = FeedModelState(error = true)
        }
    }

    fun save() {
        edited.value?.let { post ->
            viewModelScope.launch {
                try {
                    photo.value?.file?.let {
                        repository.saveWithAttachment(post, MediaUpload(it))
                    } ?: repository.save(post)
                    _postCreated.value = Unit
                    edited.value = empty
                    _dataState.value = FeedModelState()
                } catch (e: Exception) {
                    _dataState.value = FeedModelState(error = true)
                }
            }
        }
    }

    fun edit(post: Post) {
        edited.value = post
    }

    fun changeContent(content: String) {
        val text = content.trim()
        if (edited.value?.content == text) {
            return
        }
        edited.value = edited.value?.copy(content = text)
    }
}