package br.edu.iftm.deadlinetracker.util

import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.temporal.ChronoUnit

/**
 * Converte um instante em milissegundos para data e hora no fuso do aparelho.
 *
 * @receiver instante em milissegundos desde a época Unix.
 * @return data e hora locais.
 */
fun Long.toLocalDateTime(): LocalDateTime =
    LocalDateTime.ofInstant(Instant.ofEpochMilli(this), ZoneId.systemDefault())

/**
 * Converte data e hora locais para milissegundos, formato usado no banco.
 *
 * @receiver data e hora no fuso do aparelho.
 * @return instante em milissegundos desde a época Unix.
 */
fun LocalDateTime.toEpochMillis(): Long =
    atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()

/**
 * Calcula quantos dias corridos existem entre duas datas.
 *
 * @receiver data inicial.
 * @param other data final.
 * @return diferença em dias, negativa quando a data final já passou.
 */
fun LocalDate.daysUntil(other: LocalDate): Int =
    ChronoUnit.DAYS.between(this, other).toInt()
