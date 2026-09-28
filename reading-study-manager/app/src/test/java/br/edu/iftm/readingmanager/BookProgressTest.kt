package br.edu.iftm.readingmanager

import br.edu.iftm.readingmanager.data.Book
import br.edu.iftm.readingmanager.data.BookStatus
import br.edu.iftm.readingmanager.data.Genre
import br.edu.iftm.readingmanager.data.pagesRead
import br.edu.iftm.readingmanager.data.progressSession
import br.edu.iftm.readingmanager.data.withCurrentPage
import br.edu.iftm.readingmanager.data.withStatus
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class BookProgressTest {

    private val now = 10_000L

    @Test
    fun firstPageStartsReading() {
        val updated = book().withCurrentPage(40, now)

        assertEquals(BookStatus.READING, updated.status)
        assertEquals(40, updated.currentPage)
        assertEquals(now, updated.startedAt)
        assertNull(updated.finishedAt)
    }

    @Test
    fun lastPageMarksAsRead() {
        val updated = book(status = BookStatus.READING, currentPage = 400, startedAt = 1L).withCurrentPage(442, now)

        assertEquals(BookStatus.READ, updated.status)
        assertEquals(442, updated.currentPage)
        assertEquals(1L, updated.startedAt)
        assertEquals(now, updated.finishedAt)
    }

    @Test
    fun pageIsLimitedToTotal() {
        val updated = book(status = BookStatus.READING).withCurrentPage(900, now)

        assertEquals(442, updated.currentPage)
        assertEquals(BookStatus.READ, updated.status)
    }

    @Test
    fun goingBackFromLastPageReopensReading() {
        val read = book(status = BookStatus.READ, currentPage = 442, startedAt = 1L, finishedAt = 5L)

        val updated = read.withCurrentPage(300, now)

        assertEquals(BookStatus.READING, updated.status)
        assertNull(updated.finishedAt)
    }

    @Test
    fun markingAsReadFillsLastPageAndDates() {
        val updated = book(status = BookStatus.READING, currentPage = 120).withStatus(BookStatus.READ, now)

        assertEquals(442, updated.currentPage)
        assertEquals(now, updated.startedAt)
        assertEquals(now, updated.finishedAt)
    }

    @Test
    fun startingKeepsExistingStartDate() {
        val updated = book(startedAt = 3L).withStatus(BookStatus.READING, now)

        assertEquals(BookStatus.READING, updated.status)
        assertEquals(3L, updated.startedAt)
    }

    @Test
    fun wantToReadClearsFinishDate() {
        val updated = book(status = BookStatus.READ, finishedAt = 5L).withStatus(BookStatus.WANT_TO_READ, now)

        assertEquals(BookStatus.WANT_TO_READ, updated.status)
        assertNull(updated.finishedAt)
    }

    @Test
    fun sessionCountsOnlyAdvances() {
        val before = book(status = BookStatus.READING, currentPage = 100)

        val forward = progressSession(before, before.copy(currentPage = 130), now)
        val backward = progressSession(before, before.copy(currentPage = 90), now)

        assertEquals(30, forward?.pagesRead)
        assertNull(backward)
    }

    private fun book(
        status: BookStatus = BookStatus.WANT_TO_READ,
        currentPage: Int = 0,
        startedAt: Long? = null,
        finishedAt: Long? = null
    ) = Book(
        id = 7,
        title = "Sapiens",
        author = "Yuval Noah Harari",
        totalPages = 442,
        genre = Genre.NON_FICTION,
        status = status,
        currentPage = currentPage,
        startedAt = startedAt,
        finishedAt = finishedAt
    )
}
