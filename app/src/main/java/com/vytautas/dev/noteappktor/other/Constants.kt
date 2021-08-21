package com.vytautas.dev.noteappktor.other

object Constants {

    const val DATABASE_NAME = "notes_database"
    const val BASE_URL = "http://10.0.2.2:8080"
    const val BASE_URL_SSL = "https://10.0.2.2:8081"
    const val ENCRYPTED_SHARED_PREF_NAME = "enc_shared_pref"

    const val NO_EMAIL = "NO_EMAIL"
    const val NO_PASSWORD = "NO_PASSWORD"

    const val DEFAULT_NOTE_COLOR = "FFA500"

    const val KEY_LOGGED_IN_EMAIL = "KEY_LOGGED_IN_EMAIL"
    const val KEY_LOGGED_IN_PASSWORD = "KEY_LOGGED_IN_PASSWORD"

    val IGNORE_AUTH_URLS = listOf("/login", "/register")
}