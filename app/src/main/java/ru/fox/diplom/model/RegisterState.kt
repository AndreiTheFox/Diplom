package ru.fox.diplom.model

data class RegisterState(
    val userAlreadyExists: Boolean = false,
    val error: Boolean = false,
)

