package com.vytautas.dev.noteappktor.data.remote.requests

data class AddOwnerRequest(
    val owner: String,
    val noteId: String
)