package br.edu.iftm.readingmanager

import br.edu.iftm.readingmanager.data.Book
import br.edu.iftm.readingmanager.data.BookStatus
import br.edu.iftm.readingmanager.data.Genre
import br.edu.iftm.readingmanager.ui.form.BookFormError
import br.edu.iftm.readingmanager.ui.form.BookInput
import br.edu.iftm.readingmanager.ui.form.bookFromInput
import br.edu.iftm.readingmanager.ui.form.deadline
import br.edu.iftm.readingmanager.ui.form.validateBook
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class BookFormRulesTest {

    private val now = LocalDateTime.of(2026, 9, 26, 10, 0)

    private val valid = BookInput(pages = "320", title = "Sapiens", author = "Yuval Noah Harari")

    @Test
    fun emptyFormReportsRequiredFields() {
        val errors = validateBook(BookInput(), currentPage = 0, deadlineChanged = false, now = now)
        assertEquals(
            setOf(BookFormError.PAGES_REQUIRED, BookFormError.TITLE_REQUIRED, BookFormError.AUTHOR_REQUIRED),
            errors
        )
    }

    @Test
    fun totalCannotDropBelowCurrentPage() {
        val errors = validateBook(valid.copy(pages = "100"), currentPage = 150, deadlineChanged = false, now = now)
        assertEquals(setOf(BookFormError.PAGES_BELOW_CURRENT), errors)
    }

    @Test
    fun pastDeadlineOnlyMattersWhenChanged() {
        val past = valid.copy(deadlineDate = LocalDate.of(2026, 9, 20))
        assertEquals(setOf(BookFormError.DEADLINE_PAST), validateBook(past, 0, deadlineChanged = true, now = now))
        assertTrue(validateBook(past, 0, deadlineChanged = false, now = now).isEmpty())
    }

    @Test
    fun deadlineWithoutTimeEndsTheDay() {
        val input = valid.copy(deadlineDate = LocalDate.of(2026, 9, 30))
        assertEquals(LocalDateTime.of(2026, 9, 30, 23, 59), input.deadline())
        val withTime = input.copy(deadlineTime = LocalTime.of(9, 30))
        assertEquals(LocalDateTime.of(2026, 9, 30, 9, 30), withTime.deadline())
    }

    @Test
    fun newBookIsTrimmedAndWantsToBeRead() {
        val book = bookFromInput(null, valid.copy(title = "  Sapiens  "), now = 1_000)
        assertEquals("Sapiens", book.title)
        assertEquals(320, book.totalPages)
        assertEquals(BookStatus.WANT_TO_READ, book.status)
    }

    @Test
    fun shrinkingTotalToCurrentPageFinishesTheBook() {
        val reading = Book(
            id = 7,
            title = "Sapiens",
            author = "Yuval Noah Harari",
            totalPages = 320,
            genre = Genre.NON_FICTION,
            status = BookStatus.READING,
            currentPage = 300
        )
        val edited = bookFromInput(reading, valid.copy(pages = "300"), now = 5_000)
        assertEquals(BookStatus.READ, edited.status)
        assertEquals(5_000L, edited.finishedAt)
        assertEquals(7L, edited.id)
    }

    @Test
    fun readBookStaysComplete() {
        val read = Book(
            id = 8,
            title = "Sapiens",
            author = "Yuval Noah Harari",
            totalPages = 300,
            genre = Genre.NON_FICTION,
            status = BookStatus.READ,
            currentPage = 300,
            finishedAt = 2_000
        )
        val edited = bookFromInput(read, valid.copy(pages = "320"), now = 5_000)
        assertEquals(320, edited.currentPage)
        assertEquals(2_000L, edited.finishedAt)
    }
}
