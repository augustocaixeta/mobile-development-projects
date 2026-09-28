package br.edu.iftm.readingmanager.util

import java.text.NumberFormat
import java.time.LocalDate
import java.time.LocalTime
import java.util.Locale

object Formats {

    private val brazil: Locale = Locale.forLanguageTag("pt-BR")

    private val weekdays = arrayOf("seg", "ter", "qua", "qui", "sex", "sáb", "dom")

    private val months = arrayOf(
        "jan", "fev", "mar", "abr", "mai", "jun",
        "jul", "ago", "set", "out", "nov", "dez"
    )

    private val monthNames = arrayOf(
        "janeiro", "fevereiro", "março", "abril", "maio", "junho",
        "julho", "agosto", "setembro", "outubro", "novembro", "dezembro"
    )

    /**
     * Formata um número inteiro com separador de milhar.
     *
     * @param value número a ser formatado.
     * @return texto como 1.240.
     */
    fun integer(value: Int): String = NumberFormat.getIntegerInstance(brazil).format(value)

    /**
     * Monta o cabeçalho da tela inicial.
     *
     * @param date data exibida.
     * @return texto como qui, 10 set.
     */
    fun header(date: LocalDate): String = "${weekday(date)}, ${shortDate(date)}"

    /**
     * Abrevia o dia da semana em minúsculas.
     *
     * @param date data de referência.
     * @return texto como ter.
     */
    fun weekday(date: LocalDate): String = weekdays[date.dayOfWeek.value - 1]

    /**
     * Formata o dia do mês sempre com dois dígitos.
     *
     * @param date data de referência.
     * @return texto como 08.
     */
    fun day(date: LocalDate): String = "%02d".format(brazil, date.dayOfMonth)

    /**
     * Formata a data sem o ano.
     *
     * @param date data de referência.
     * @return texto como 30 set.
     */
    fun shortDate(date: LocalDate): String = "${date.dayOfMonth} ${months[date.monthValue - 1]}"

    /**
     * Formata a data com o ano.
     *
     * @param date data de referência.
     * @return texto como 30 set 2026.
     */
    fun fullDate(date: LocalDate): String = "${shortDate(date)} ${date.year}"

    /**
     * Formata o horário no padrão de 24 horas.
     *
     * @param time horário de referência.
     * @return texto como 09:30.
     */
    fun time(time: LocalTime): String = "%02d:%02d".format(brazil, time.hour, time.minute)

    /**
     * Devolve o nome do mês por extenso em minúsculas.
     *
     * @param date data de referência.
     * @return texto como setembro.
     */
    fun monthName(date: LocalDate): String = monthNames[date.monthValue - 1]
}
