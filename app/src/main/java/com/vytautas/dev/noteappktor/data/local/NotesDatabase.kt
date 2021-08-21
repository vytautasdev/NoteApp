package com.vytautas.dev.noteappktor.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.vytautas.dev.noteappktor.data.local.entities.LocallyDeletedNoteId
import com.vytautas.dev.noteappktor.data.local.entities.Note

@Database(entities = [Note::class, LocallyDeletedNoteId::class], version = 1, exportSchema = false)
@TypeConverters(Converters::class)
abstract class NotesDatabase : RoomDatabase() {

    abstract fun noteDao(): NoteDao
}