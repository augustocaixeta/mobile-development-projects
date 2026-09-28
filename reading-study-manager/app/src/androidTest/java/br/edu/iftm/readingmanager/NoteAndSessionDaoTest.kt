package br.edu.iftm.readingmanager

import androidx.room.Room
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import br.edu.iftm.readingmanager.data.AppDatabase
import br.edu.iftm.readingmanager.data.Book
import br.edu.iftm.readingmanager.data.BookStatus
import br.edu.iftm.readingmanager.data.Genre
import br.edu.iftm.readingmanager.data.Note
import br.edu.iftm.readingmanager.data.NoteType
import br.edu.iftm.readingmanager.data.Session
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class NoteAndSessionDaoTest {

    private lateinit var database: AppDatabase

    @Before
    fun openDatabase() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        database = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java).build()
    }

    @After
    fun closeDatabase() {
        database.close()
    }

    @Test
    fun notesComeNewestFirstPerBook() = runBlocking {
        val sapiens = database.bookDao().insert(book("Sapiens"))
        val duna = database.bookDao().insert(book("Duna"))
        val notes = database.noteDao()
        notes.insert(Note(bookId = sapiens, type = NoteType.ANNOTATION, text = "primeira", createdAt = 1_000))
        notes.insert(Note(bookId = sapiens, type = NoteType.QUOTE, text = "segunda", createdAt = 2_000))
        notes.insert(Note(bookId = duna, type = NoteType.INSIGHT, text = "outro livro", createdAt = 3_000))

        val list = notes.observeByBook(sapiens).first()

        assertEquals(listOf("segunda", "primeira"), list.map { it.text })
    }

    @Test
    fun deletingBookRemovesNotesAndSessions() = runBlocking {
        val id = database.bookDao().insert(book("Sapiens"))
        database.noteDao().insert(Note(bookId = id, type = NoteType.INSIGHT, text = "ideia"))
        database.sessionDao().insert(Session(bookId = id, startedAt = 1_000, startPage = 0, endPage = 30))

        database.bookDao().delete(id)

        assertEquals(0, database.noteDao().observeByBook(id).first().size)
        assertEquals(0, database.sessionDao().observeAll().first().size)
    }

    @Test
    fun recordUpdatesBookAndStoresSession() = runBlocking {
        val id = database.bookDao().insert(book("Sapiens"))
        val saved = database.bookDao().find(id)!!

        database.sessionDao().record(
            saved.copy(status = BookStatus.READING, currentPage = 60),
            Session(bookId = id, startedAt = 1_000, startPage = 0, endPage = 60)
        )

        assertEquals(60, database.bookDao().find(id)!!.currentPage)
        assertEquals(listOf(60), database.sessionDao().observeByBook(id).first().map { it.endPage })
    }

    private fun book(title: String) = Book(
        title = title,
        author = "Autor",
        totalPages = 442,
        genre = Genre.NON_FICTION
    )
}
