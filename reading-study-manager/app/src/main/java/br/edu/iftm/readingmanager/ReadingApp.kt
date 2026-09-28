package br.edu.iftm.readingmanager

import android.app.Application
import br.edu.iftm.readingmanager.data.AppDatabase
import br.edu.iftm.readingmanager.data.BookRepository
import br.edu.iftm.readingmanager.data.GoalRepository
import br.edu.iftm.readingmanager.data.NoteRepository
import br.edu.iftm.readingmanager.data.SessionRepository

class ReadingApp : Application() {

    val database by lazy { AppDatabase.getInstance(this) }

    val books by lazy { BookRepository(database.bookDao()) }

    val goals by lazy { GoalRepository(this) }

    val notes by lazy { NoteRepository(database.noteDao()) }

    val sessions by lazy { SessionRepository(database.sessionDao()) }
}
