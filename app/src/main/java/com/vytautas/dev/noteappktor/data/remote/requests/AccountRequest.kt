package com.vytautas.dev.noteappktor.data.remote.requests

data class AccountRequest(
    val email: String,
    val password: String
)