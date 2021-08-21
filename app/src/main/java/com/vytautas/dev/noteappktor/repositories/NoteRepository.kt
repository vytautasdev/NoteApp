package com.vytautas.dev.noteappktor.repositories

import android.app.Application
import com.vytautas.dev.noteappktor.data.local.NoteDao
import com.vytautas.dev.noteappktor.data.local.entities.LocallyDeletedNoteId
import com.vytautas.dev.noteappktor.data.local.entities.Note
import com.vytautas.dev.noteappktor.data.remote.NoteApi
import com.vytautas.dev.noteappktor.data.remote.requests.AccountRequest
import com.vytautas.dev.noteappktor.data.remote.requests.AddOwnerRequest
import com.vytautas.dev.noteappktor.data.remote.requests.DeleteNoteRequest
import com.vytautas.dev.noteappktor.other.Resource
import com.vytautas.dev.noteappktor.other.checkForInternetConnection
import com.vytautas.dev.noteappktor.other.networkBoundResource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import retrofit2.Response
import javax.inject.Inject

class NoteRepository @Inject constructor(
    private val noteDao: NoteDao,
    private val noteApi: NoteApi,
    private val context: Application
) {

    fun getAllNotes(): Flow<Resource<List<Note>>> {
        return networkBoundResource(
            query = {
                noteDao.getAllNotes()
            },
            fetch = {
                syncNotes()
                currentNotesResponse
            },
            saveFetchResult = { response ->
                response?.body()?.let { note ->
                    insertNotes(note.onEach { note -> note.isSynced = true })
                }
            },
            shouldFetch = {
                checkForInternetConnection(context)
            }
        )
    }

    suspend fun insertNote(note: Note) {
        val response = try {
            noteApi.addNote(note)
        } catch (e: Exception) {
            null
        }
        if (response != null && response.isSuccessful) {
            noteDao.insertNote(note.apply { isSynced = true })
        } else {
            noteDao.insertNote(note)
        }
    }

    suspend fun insertNotes(notes: List<Note>) {
        notes.forEach { insertNote(it) }
    }

    suspend fun deleteNote(noteID: String) {
        val response = try {
            noteApi.deleteNote(DeleteNoteRequest(noteID))
        } catch (e: Exception) {
            null
        }
        noteDao.deleteNoteById(noteID)
        if (response == null || !response.isSuccessful) {
            noteDao.insertLocallyDeleteNoteId(LocallyDeletedNoteId(noteID))
        } else {
            deleteLocallyDeletedNoteID(noteID)
        }
    }

    fun observeNoteByID(noteID: String) = noteDao.observeNoteById(noteID)

    suspend fun deleteLocallyDeletedNoteID(deletedNoteID: String) {
        noteDao.deleteLocallyDeletedNoteId(deletedNoteID)
    }


    suspend fun getNoteById(noteId: String) = noteDao.getNoteById(noteId)

    private var currentNotesResponse: Response<List<Note>>? = null

    suspend fun syncNotes() {
        val locallyDeletedNoteIDs = noteDao.getAllLocallyDeletedNoteIds()
        locallyDeletedNoteIDs.forEach { id -> deleteNote(id.deletedNoteId) }

        val unsyncedNotes = noteDao.getAllUnsyncedNotes()
        unsyncedNotes.forEach { note -> insertNote(note) }

        currentNotesResponse = noteApi.getNotes()
        currentNotesResponse?.body()?.let { notes ->
            noteDao.deleteAllNotes()
            insertNotes(notes.onEach { note -> note.isSynced = true })
        }
    }

    suspend fun addOwnerToNote(owner: String, noteID: String) = withContext(Dispatchers.IO) {
        try {
            val response = noteApi.addOwnerToNote(AddOwnerRequest(owner, noteID))
            if (response.isSuccessful && response.body()!!.isSuccessful) {
                Resource.success(response.body()?.message)
            } else {
                Resource.error(response.body()?.message ?: response.message(), null)
            }
        } catch (e: Exception) {
            Resource.error("Couldn't connect to the servers. Check your internet connection", null)
        }
    }

    suspend fun login(email: String, password: String) = withContext(Dispatchers.IO) {
        try {
            val response = noteApi.login(AccountRequest(email, password))
            if (response.isSuccessful && response.body()!!.isSuccessful) {
                Resource.success(response.body()?.message)
            } else {
                Resource.error(response.body()?.message ?: response.message(), null)
            }
        } catch (e: Exception) {
            Resource.error("Couldn't connect to the servers. Check your internet connection", null)
        }
    }

    suspend fun register(email: String, password: String) = withContext(Dispatchers.IO) {
        try {
            val response = noteApi.register(AccountRequest(email, password))
            if (response.isSuccessful && response.body()!!.isSuccessful) {
                Resource.success(response.body()?.message)
            } else {
                Resource.error(response.body()?.message ?: response.message(), null)
            }
        } catch (e: Exception) {
            Resource.error("Couldn't connect to the servers. Check your internet connection", null)
        }
    }
}