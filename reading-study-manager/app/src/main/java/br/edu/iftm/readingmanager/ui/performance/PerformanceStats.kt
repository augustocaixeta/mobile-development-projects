package br.edu.iftm.readingmanager.ui.performance

import br.edu.iftm.readingmanager.data.Book
import br.edu.iftm.readingmanager.data.Session
import br.edu.iftm.readingmanager.data.isRead
import br.edu.iftm.readingmanager.data.pagesRead
import br.edu.iftm.readingmanager.util.toLocalDateTime
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.YearMonth
import java.time.temporal.ChronoUnit
import java.time.temporal.TemporalAdjusters
import kotlin.math.roundToInt

private const val DAYS_IN_WEEK = 7L
private const val MAX_ACHIEVEMENTS = 5

enum class Period {
    WEEK,
    MONTH,
    YEAR
}

data class ChartBar(
    val start: LocalDate,
    val index: Int,
    val pages: Int,
    val current: Boolean
)

data class PeriodStats(
    val pages: Int = 0,
    val previousPages: Int = 0,
    val booksFinished: Int = 0,
    val bars: List<ChartBar> = emptyList()
)

sealed interface Achievement {
    val date: LocalDate
}

data class BookFinished(
    override val date: LocalDate,
    val book: Book,
    val days: Int,
    val recordGain: Int?,
    val onTime: Boolean?
) : Achievement

data class GoalReached(
    override val date: LocalDate,
    val month: YearMonth,
    val pages: Int,
    val goal: Int,
    val record: Boolean
) : Achievement

/**
 * Primeiro dia do período que contém a data. A semana começa na segunda-feira.
 *
 * @receiver período escolhido nas abas.
 * @param date data de referência.
 * @return início do período.
 */
fun Period.startOf(date: LocalDate): LocalDate = when (this) {
    Period.WEEK -> date.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))
    Period.MONTH -> date.withDayOfMonth(1)
    Period.YEAR -> date.withDayOfYear(1)
}

/**
 * Primeiro dia do período seguinte, usado como limite exclusivo.
 *
 * @receiver período escolhido nas abas.
 * @param start início do período.
 * @return início do próximo período.
 */
fun Period.next(start: LocalDate): LocalDate = when (this) {
    Period.WEEK -> start.plusWeeks(1)
    Period.MONTH -> start.plusMonths(1)
    Period.YEAR -> start.plusYears(1)
}

/**
 * Primeiro dia do período anterior, usado na comparação.
 *
 * @receiver período escolhido nas abas.
 * @param start início do período atual.
 * @return início do período anterior.
 */
fun Period.previous(start: LocalDate): LocalDate = when (this) {
    Period.WEEK -> start.minusWeeks(1)
    Period.MONTH -> start.minusMonths(1)
    Period.YEAR -> start.minusYears(1)
}

/**
 * Soma as páginas lidas em cada dia a partir das sessões registradas.
 *
 * @param sessions sessões de leitura.
 * @return páginas lidas por data.
 */
fun pagesByDay(sessions: List<Session>): Map<LocalDate, Int> =
    sessions
        .groupBy { it.startedAt.toLocalDateTime().toLocalDate() }
        .mapValues { (_, list) -> list.sumOf { it.pagesRead } }

/**
 * Monta os números do período: páginas lidas, comparação com o período anterior,
 * livros concluídos e as barras do gráfico.
 * A semana tem uma barra por dia, o mês uma por semana e o ano uma por mês.
 *
 * @param period período escolhido nas abas.
 * @param today data atual.
 * @param daily páginas lidas por data.
 * @param books livros cadastrados.
 * @return estatísticas do período.
 */
fun periodStats(period: Period, today: LocalDate, daily: Map<LocalDate, Int>, books: List<Book>): PeriodStats {
    val start = period.startOf(today)
    val end = period.next(start)
    val previousStart = period.previous(start)
    val finished = books.count { book ->
        val date = book.finishedDate() ?: return@count false
        !date.isBefore(start) && date.isBefore(end)
    }
    return PeriodStats(
        pages = pagesBetween(daily, start, end),
        previousPages = pagesBetween(daily, previousStart, start),
        booksFinished = finished,
        bars = bars(period, start, end, today, daily)
    )
}

/**
 * Variação percentual entre o período atual e o anterior.
 *
 * @param current páginas do período atual.
 * @param previous páginas do período anterior.
 * @return variação arredondada, ou null quando não houve leitura no período anterior.
 */
fun changePercent(current: Int, previous: Int): Int? {
    if (previous <= 0) {
        return null
    }
    return ((current - previous) * 100.0 / previous).roundToInt()
}

/**
 * Conta os dias seguidos com leitura até hoje. Se ainda não houve leitura hoje,
 * a sequência de ontem continua valendo.
 *
 * @param sessions sessões de leitura.
 * @param today data atual.
 * @return quantidade de dias seguidos.
 */
fun streakDays(sessions: List<Session>, today: LocalDate): Int {
    val days = sessions
        .filter { it.pagesRead > 0 || it.durationSeconds > 0 }
        .map { it.startedAt.toLocalDateTime().toLocalDate() }
        .toSet()
    var day = if (today in days) today else today.minusDays(1)
    var count = 0
    while (day in days) {
        count++
        day = day.minusDays(1)
    }
    return count
}

/**
 * Lista as conquistas mais recentes: livros concluídos, com o recorde de rapidez,
 * e meses em que a meta de páginas foi batida.
 *
 * @param books livros cadastrados.
 * @param daily páginas lidas por data.
 * @param goal meta mensal de páginas, ou null quando não definida.
 * @return até cinco conquistas, das mais recentes para as mais antigas.
 */
fun achievements(books: List<Book>, daily: Map<LocalDate, Int>, goal: Int?): List<Achievement> =
    (finishedBooks(books) + goalsReached(daily, goal))
        .sortedByDescending { it.date }
        .take(MAX_ACHIEVEMENTS)

/**
 * Conquistas de livros concluídos. Um livro bate o recorde quando é lido em menos dias
 * do que todos os concluídos antes dele.
 *
 * @param books livros cadastrados.
 * @return uma conquista por livro lido, em ordem de conclusão.
 */
private fun finishedBooks(books: List<Book>): List<BookFinished> {
    var best: Int? = null
    return books
        .filter { it.isRead && it.finishedAt != null }
        .sortedBy { it.finishedAt }
        .map { book ->
            val finish = book.finishedDate() ?: LocalDate.MIN
            val begin = book.startedAt?.toLocalDateTime()?.toLocalDate() ?: finish
            val days = (ChronoUnit.DAYS.between(begin, finish).toInt() + 1).coerceAtLeast(1)
            val previousBest = best
            val gain = if (previousBest != null && days < previousBest) previousBest - days else null
            best = if (previousBest == null) days else minOf(previousBest, days)
            val deadline = book.deadline?.toLocalDateTime()?.toLocalDate()
            BookFinished(
                date = finish,
                book = book,
                days = days,
                recordGain = gain,
                onTime = deadline?.let { !finish.isAfter(it) }
            )
        }
}

/**
 * Conquistas de meta mensal. A data é o dia em que a soma do mês alcançou a meta, e o mês
 * é recorde quando supera todos os meses anteriores.
 *
 * @param daily páginas lidas por data.
 * @param goal meta mensal de páginas, ou null quando não definida.
 * @return uma conquista por mês em que a meta foi batida.
 */
private fun goalsReached(daily: Map<LocalDate, Int>, goal: Int?): List<GoalReached> {
    if (goal == null || goal <= 0) {
        return emptyList()
    }
    var best = 0
    return daily.entries
        .groupBy { YearMonth.from(it.key) }
        .toSortedMap()
        .mapNotNull { (month, entries) ->
            val total = entries.sumOf { it.value }
            val previousBest = best
            best = maxOf(best, total)
            var sum = 0
            val reachedOn = entries.sortedBy { it.key }.firstOrNull { entry ->
                sum += entry.value
                sum >= goal
            }?.key ?: return@mapNotNull null
            GoalReached(
                date = reachedOn,
                month = month,
                pages = total,
                goal = goal,
                record = previousBest > 0 && total > previousBest
            )
        }
}

/**
 * Soma as páginas lidas entre duas datas.
 *
 * @param daily páginas lidas por data.
 * @param from primeiro dia, incluído.
 * @param until último limite, não incluído.
 * @return total de páginas no intervalo.
 */
private fun pagesBetween(daily: Map<LocalDate, Int>, from: LocalDate, until: LocalDate): Int =
    daily.entries.filter { !it.key.isBefore(from) && it.key.isBefore(until) }.sumOf { it.value }

/**
 * Divide o período em barras e soma as páginas de cada uma.
 *
 * @param period período escolhido nas abas.
 * @param start início do período.
 * @param end início do período seguinte.
 * @param today data atual, que destaca a barra em andamento.
 * @param daily páginas lidas por data.
 * @return barras do gráfico em ordem cronológica.
 */
private fun bars(
    period: Period,
    start: LocalDate,
    end: LocalDate,
    today: LocalDate,
    daily: Map<LocalDate, Int>
): List<ChartBar> {
    val starts = when (period) {
        Period.WEEK -> (0 until DAYS_IN_WEEK).map { start.plusDays(it) }
        Period.MONTH -> generateSequence(start) { it.plusDays(DAYS_IN_WEEK) }.takeWhile { it.isBefore(end) }.toList()
        Period.YEAR -> (0L until 12L).map { start.plusMonths(it) }
    }
    return starts.mapIndexed { index, barStart ->
        val barEnd = starts.getOrNull(index + 1) ?: end
        ChartBar(
            start = barStart,
            index = index,
            pages = pagesBetween(daily, barStart, barEnd),
            current = !today.isBefore(barStart) && today.isBefore(barEnd)
        )
    }
}

/**
 * Data de conclusão do livro, quando ele já foi lido.
 *
 * @receiver livro cadastrado.
 * @return data de conclusão, ou null para livros ainda não lidos.
 */
private fun Book.finishedDate(): LocalDate? =
    if (isRead) finishedAt?.toLocalDateTime()?.toLocalDate() else null
