package br.edu.iftm.readingmanager

import br.edu.iftm.readingmanager.data.Book
import br.edu.iftm.readingmanager.data.BookStatus
import br.edu.iftm.readingmanager.data.Genre
import br.edu.iftm.readingmanager.data.Session
import br.edu.iftm.readingmanager.ui.performance.BookFinished
import br.edu.iftm.readingmanager.ui.performance.GoalReached
import br.edu.iftm.readingmanager.ui.performance.Period
import br.edu.iftm.readingmanager.ui.performance.achievements
import br.edu.iftm.readingmanager.ui.performance.changePercent
import br.edu.iftm.readingmanager.ui.performance.pagesByDay
import br.edu.iftm.readingmanager.ui.performance.periodStats
import br.edu.iftm.readingmanager.ui.performance.streakDays
import br.edu.iftm.readingmanager.util.toEpochMillis
import java.time.LocalDate
import java.time.YearMonth
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class PerformanceStatsTest {

    private val today = LocalDate.of(2026, 9, 26)

    private val sessions = listOf(
        session(LocalDate.of(2026, 8, 20), pages = 40),
        session(LocalDate.of(2026, 9, 16), pages = 40),
        session(LocalDate.of(2026, 9, 22), pages = 30),
        session(LocalDate.of(2026, 9, 26), pages = 50)
    )

    private val finishedFast = book(
        id = 1,
        start = LocalDate.of(2026, 9, 12),
        finish = LocalDate.of(2026, 9, 18),
        deadline = LocalDate.of(2026, 9, 20)
    )

    private val finishedSlow = book(id = 2, start = LocalDate.of(2026, 9, 1), finish = LocalDate.of(2026, 9, 10))

    @Test
    fun weekHasOneBarPerDayAndHighlightsToday() {
        val stats = periodStats(Period.WEEK, today, pagesByDay(sessions), emptyList())
        assertEquals(80, stats.pages)
        assertEquals(40, stats.previousPages)
        assertEquals(7, stats.bars.size)
        assertEquals(30, stats.bars[1].pages)
        assertEquals(50, stats.bars[5].pages)
        assertTrue(stats.bars[5].current)
        assertEquals(1, stats.bars.count { it.current })
    }

    @Test
    fun monthHasOneBarPerWeekAndCountsFinishedBooks() {
        val books = listOf(finishedFast, finishedSlow)
        val stats = periodStats(Period.MONTH, today, pagesByDay(sessions), books)
        assertEquals(120, stats.pages)
        assertEquals(40, stats.previousPages)
        assertEquals(2, stats.booksFinished)
        assertEquals(listOf(1, 8, 15, 22, 29), stats.bars.map { it.start.dayOfMonth })
        assertTrue(stats.bars[3].current)
    }

    @Test
    fun yearHasTwelveBars() {
        val stats = periodStats(Period.YEAR, today, pagesByDay(sessions), emptyList())
        assertEquals(12, stats.bars.size)
        assertEquals(40, stats.bars[7].pages)
        assertEquals(120, stats.bars[8].pages)
    }

    @Test
    fun changeIsNullWithoutPreviousReading() {
        assertEquals(100, changePercent(80, 40))
        assertEquals(-25, changePercent(30, 40))
        assertNull(changePercent(30, 0))
    }

    @Test
    fun streakKeepsYesterdayWhenTodayHasNoReading() {
        val days = listOf(24, 25).map { session(LocalDate.of(2026, 9, it), pages = 10) }
        assertEquals(2, streakDays(days, today))
        assertEquals(3, streakDays(days + session(today, pages = 5), today))
        assertEquals(0, streakDays(days, today.plusDays(2)))
    }

    @Test
    fun fasterBookBeatsPersonalRecord() {
        val list = achievements(listOf(finishedSlow, finishedFast), emptyMap(), goal = null)
        val latest = list.first() as BookFinished
        assertEquals(1L, latest.book.id)
        assertEquals(7, latest.days)
        assertEquals(3, latest.recordGain)
        assertEquals(true, latest.onTime)
        assertNull((list[1] as BookFinished).recordGain)
    }

    @Test
    fun goalIsReachedOnTheDayTheSumCrossesIt() {
        val list = achievements(emptyList(), pagesByDay(sessions), goal = 100)
        assertEquals(1, list.size)
        val goal = list.first() as GoalReached
        assertEquals(YearMonth.of(2026, 9), goal.month)
        assertEquals(today, goal.date)
        assertEquals(120, goal.pages)
        assertTrue(goal.record)
    }

    /**
     * Cria uma sessão lida às dez da manhã do dia informado.
     *
     * @param date dia da leitura.
     * @param pages páginas lidas.
     * @return sessão de teste.
     */
    private fun session(date: LocalDate, pages: Int): Session = Session(
        bookId = 1,
        startedAt = date.atTime(10, 0).toEpochMillis(),
        durationSeconds = 600,
        startPage = 0,
        endPage = pages
    )

    /**
     * Cria um livro lido entre duas datas.
     *
     * @param id identificador do livro.
     * @param start dia em que a leitura começou.
     * @param finish dia em que a leitura terminou.
     * @param deadline prazo do livro, quando houver.
     * @return livro de teste.
     */
    private fun book(id: Long, start: LocalDate, finish: LocalDate, deadline: LocalDate? = null): Book = Book(
        id = id,
        title = "Livro $id",
        author = "Autor",
        totalPages = 100,
        genre = Genre.NON_FICTION,
        status = BookStatus.READ,
        currentPage = 100,
        deadline = deadline?.atTime(23, 59)?.toEpochMillis(),
        startedAt = start.atTime(9, 0).toEpochMillis(),
        finishedAt = finish.atTime(21, 0).toEpochMillis()
    )
}
