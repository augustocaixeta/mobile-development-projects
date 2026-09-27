package br.edu.iftm.deadlinetracker

import br.edu.iftm.deadlinetracker.util.Formats
import br.edu.iftm.deadlinetracker.util.daysUntil
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import org.junit.Assert.assertEquals
import org.junit.Test

class FormatsTest {

    private val thursday = LocalDate.of(2026, 9, 10)

    @Test
    fun currencyUsesBrazilianFormat() {
        assertEquals("R$ 1.240,00", Formats.currency(124_000).replace(' ', ' '))
    }

    @Test
    fun amountHasNoSymbol() {
        assertEquals("312,00", Formats.amount(31_200))
        assertEquals("0,05", Formats.amount(5))
    }

    @Test
    fun headerMatchesFigma() {
        assertEquals("qui, 10 set", Formats.header(thursday))
    }

    @Test
    fun shortAndFullDates() {
        assertEquals("30 set", Formats.shortDate(LocalDate.of(2026, 9, 30)))
        assertEquals("1 set 2026", Formats.fullDate(LocalDate.of(2026, 9, 1)))
        assertEquals("08", Formats.day(LocalDate.of(2026, 9, 8)))
    }

    @Test
    fun timeAndReminderDate() {
        assertEquals("09:30", Formats.time(LocalTime.of(9, 30)))
        assertEquals("29/09 09:00", Formats.dayMonthTime(LocalDateTime.of(2026, 9, 29, 9, 0)))
    }

    @Test
    fun chartAxisLabels() {
        assertEquals("SÁB", Formats.axisDay(LocalDate.of(2026, 9, 12)))
        assertEquals("DEZ", Formats.axisMonth(12))
    }

    @Test
    fun integerUsesThousandsSeparator() {
        assertEquals("3.420", Formats.integer(3_420))
    }

    @Test
    fun daysBetweenDates() {
        assertEquals(3, thursday.daysUntil(LocalDate.of(2026, 9, 13)))
        assertEquals(-2, thursday.daysUntil(LocalDate.of(2026, 9, 8)))
    }
}
