package ru.fox.diplom.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import dagger.hilt.android.lifecycle.HiltViewModel
import ru.fox.diplom.auth.AppAuth
import ru.fox.diplom.auth.AuthState
import javax.inject.Inject
@HiltViewModel
class AuthViewModel @Inject constructor(
    private val appAuth: AppAuth
)  : ViewModel() {
    val state: LiveData<AuthState> = appAuth
        .authStateFlow
        .asLiveData()
    val authorized: Boolean
        get() = appAuth.authStateFlow.value.id != 0L
}