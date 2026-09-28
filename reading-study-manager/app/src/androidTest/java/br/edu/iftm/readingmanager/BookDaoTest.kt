package br.edu.iftm.readingmanager

import androidx.room.Room
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import br.edu.iftm.readingmanager.data.AppDatabase
import br.edu.iftm.readingmanager.data.Book
import br.edu.iftm.readingmanager.data.BookDao
import br.edu.iftm.readingmanager.data.BookStatus
import br.edu.iftm.readingmanager.data.Genre
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class BookDaoTest {

    private lateinit var database: AppDatabase
    private lateinit var dao: BookDao

    @Before
    fun openDatabase() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        database = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java).build()
        dao = database.bookDao()
    }

    @After
    fun closeDatabase() {
        database.close()
    }

    @Test
    fun filtersByStatusAndGenre() = runBlocking {
        dao.insert(book("Sapiens", Genre.NON_FICTION, BookStatus.READING))
        dao.insert(book("Duna", Genre.FICTION, BookStatus.READING))
        dao.insert(book("O Hobbit", Genre.FANTASY, BookStatus.WANT_TO_READ))

        val reading = dao.observeFiltered(BookStatus.READING, null).first()
        val fiction = dao.observeFiltered(null, Genre.FICTION).first()
        val both = dao.observeFiltered(BookStatus.READING, Genre.NON_FICTION).first()
        val all = dao.observeFiltered(null, null).first()

        assertEquals(listOf("Duna", "Sapiens"), reading.map { it.title })
        assertEquals(listOf("Duna"), fiction.map { it.title })
        assertEquals(listOf("Sapiens"), both.map { it.title })
        assertEquals(3, all.size)
    }

    @Test
    fun filteredListReactsToUpdates() = runBlocking {
        val id = dao.insert(book("Sapiens", Genre.NON_FICTION, BookStatus.READING))
        assertEquals(0, dao.observeFiltered(BookStatus.READ, null).first().size)

        val saved = dao.find(id)!!
        dao.update(saved.copy(status = BookStatus.READ, finishedAt = 1_000))

        assertEquals(listOf("Sapiens"), dao.observeFiltered(BookStatus.READ, null).first().map { it.title })
    }

    @Test
    fun finishedBetweenKeepsOnlyBooksReadInRange() = runBlocking {
        dao.insert(book("Antes", Genre.FICTION, BookStatus.READ, finishedAt = 500))
        dao.insert(book("Dentro", Genre.FICTION, BookStatus.READ, finishedAt = 1_500))
        dao.insert(book("Depois", Genre.FICTION, BookStatus.READ, finishedAt = 2_000))
        dao.insert(book("Lendo", Genre.FICTION, BookStatus.READING))

        val finished = dao.observeFinishedBetween(from = 1_000, until = 2_000).first()

        assertEquals(listOf("Dentro"), finished.map { it.title })
    }

    @Test
    fun duplicatesIgnoreCaseAndEditedBook() = runBlocking {
        val id = dao.insert(book("Sapiens", Genre.NON_FICTION, BookStatus.READING))

        assertEquals(1, dao.countDuplicates("sapiens", "yuval noah harari", ignoreId = 0))
        assertEquals(0, dao.countDuplicates("Sapiens", "Yuval Noah Harari", ignoreId = id))
    }

    @Test
    fun deleteRemovesBook() = runBlocking {
        val id = dao.insert(book("Sapiens", Genre.NON_FICTION, BookStatus.READING))

        dao.delete(id)

        assertNull(dao.find(id))
    }

    private fun book(
        title: String,
        genre: Genre,
        status: BookStatus,
        finishedAt: Long? = null
    ) = Book(
        title = title,
        author = "Yuval Noah Harari",
        totalPages = 442,
        genre = genre,
        status = status,
        finishedAt = finishedAt
    )
}
