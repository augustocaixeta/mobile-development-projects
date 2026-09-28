package br.edu.iftm.readingmanager

import br.edu.iftm.readingmanager.data.Book
import br.edu.iftm.readingmanager.data.BookStatus
import br.edu.iftm.readingmanager.data.Genre
import br.edu.iftm.readingmanager.ui.session.SessionDraft
import br.edu.iftm.readingmanager.ui.session.elapsedSeconds
import br.edu.iftm.readingmanager.ui.session.finishedSession
import br.edu.iftm.readingmanager.ui.session.isRunning
import br.edu.iftm.readingmanager.ui.session.nextMark
import br.edu.iftm.readingmanager.ui.session.paused
import br.edu.iftm.readingmanager.ui.session.reachedPage
import br.edu.iftm.readingmanager.ui.session.resumed
import br.edu.iftm.readingmanager.ui.session.sessionClock
import br.edu.iftm.readingmanager.ui.session.suggestedMarks
import br.edu.iftm.readingmanager.ui.session.toggled
import br.edu.iftm.readingmanager.ui.session.withFinalPage
import br.edu.iftm.readingmanager.ui.session.withMark
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Test

class SessionDraftTest {

    private val book = Book(
        id = 3,
        title = "Sapiens",
        author = "Yuval Noah Harari",
        totalPages = 200,
        genre = Genre.NON_FICTION,
        status = BookStatus.READING,
        currentPage = 45,
        startedAt = 0
    )

    @Test
    fun pauseKeepsTimeAndResumeContinuesCounting() {
        val draft = SessionDraft(startedAt = 0)
        val paused = draft.paused(now = 90_000)
        assertFalse(paused.isRunning)
        assertEquals(90L, paused.elapsedSeconds(now = 500_000))
        val resumed = paused.resumed(now = 600_000)
        assertEquals(120L, resumed.elapsedSeconds(now = 630_000))
    }

    @Test
    fun checkingMarkAlsoChecksPreviousOnes() {
        val draft = SessionDraft(startedAt = 0, marks = listOf(55, 65, 75)).toggled(65)
        assertEquals(setOf(55, 65), draft.done)
        assertEquals(65, draft.reachedPage(startPage = 45))
        assertEquals(75, draft.nextMark(startPage = 45))
    }

    @Test
    fun uncheckingMarkUndoesLaterOnes() {
        val draft = SessionDraft(startedAt = 0, marks = listOf(55, 65, 75)).toggled(75).toggled(65)
        assertEquals(setOf(55), draft.done)
        assertEquals(55, draft.reachedPage(startPage = 45))
    }

    @Test
    fun marksStaySortedWithoutRepetition() {
        val draft = SessionDraft(startedAt = 0, marks = listOf(60, 80)).withMark(70).withMark(60)
        assertEquals(listOf(60, 70, 80), draft.marks)
    }

    @Test
    fun suggestedMarksRoundToFiveAndStopAtTotal() {
        assertEquals(listOf(55, 65, 75), suggestedMarks(startPage = 43, totalPages = 200))
        assertEquals(listOf(195, 200), suggestedMarks(startPage = 182, totalPages = 200))
        assertEquals(emptyList<Int>(), suggestedMarks(startPage = 200, totalPages = 200))
    }

    @Test
    fun clockShowsHoursOnlyWhenNeeded() {
        assertEquals("12:45", sessionClock(765))
        assertEquals("1:05:30", sessionClock(3930))
    }

    @Test
    fun finishingRecordsPagesAndTime() {
        val after = book.withFinalPage(page = 90, now = 1_000)
        val session = finishedSession(book, after, startedAt = 0, durationSeconds = 1_500)
        assertEquals(45, session?.startPage)
        assertEquals(90, session?.endPage)
        assertEquals(1_500L, session?.durationSeconds)
    }

    @Test
    fun shortSessionWithoutProgressIsNotRecorded() {
        assertNull(finishedSession(book, book, startedAt = 0, durationSeconds = 30))
        assertEquals(45, finishedSession(book, book, startedAt = 0, durationSeconds = 600)?.endPage)
    }

    @Test
    fun finishingOnLastPageMarksBookAsRead() {
        val after = book.withFinalPage(page = 200, now = 1_000)
        assertEquals(BookStatus.READ, after.status)
        assertEquals(1_000L, after.finishedAt)
    }
}
