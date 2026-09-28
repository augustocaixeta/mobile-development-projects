package br.edu.iftm.readingmanager.ui.form

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDefaults
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.SelectableDates
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimePicker
import androidx.compose.material3.TimePickerDefaults
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import br.edu.iftm.readingmanager.R
import br.edu.iftm.readingmanager.ui.components.CardDialog
import br.edu.iftm.readingmanager.ui.components.PlainButton
import br.edu.iftm.readingmanager.ui.components.PrimaryButton
import br.edu.iftm.readingmanager.ui.theme.ReadingTheme
import java.time.Instant
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneOffset

enum class DateLimit {
    UNTIL_TODAY,
    FROM_TODAY
}

/**
 * Calendário dos formulários, com as cores do app e um limite em relação a hoje.
 *
 * @param date dia escolhido no momento.
 * @param limit se o calendário aceita só dias até hoje ou só a partir de hoje.
 * @param onDismiss fecha o calendário sem trocar.
 * @param onConfirm recebe o novo dia.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FormDateDialog(date: LocalDate, limit: DateLimit, onDismiss: () -> Unit, onConfirm: (LocalDate) -> Unit) {
    val colors = ReadingTheme.colors
    val todayMillis = remember { LocalDate.now().toUtcMillis() }
    val state = rememberDatePickerState(
        initialSelectedDateMillis = date.toUtcMillis(),
        selectableDates = remember(todayMillis, limit) { LimitedDates(todayMillis, limit) }
    )
    val pickerColors = DatePickerDefaults.colors(containerColor = colors.surface)
    DatePickerDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(
                onClick = {
                    val millis = state.selectedDateMillis
                    if (millis != null) {
                        onConfirm(Instant.ofEpochMilli(millis).atZone(ZoneOffset.UTC).toLocalDate())
                    } else {
                        onDismiss()
                    }
                }
            ) {
                Text(text = stringResource(R.string.action_save), color = colors.text)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(text = stringResource(R.string.action_cancel), color = colors.text2)
            }
        },
        colors = pickerColors
    ) {
        DatePicker(state = state, colors = pickerColors)
    }
}

/**
 * Relógio dos formulários, no formato de 24 horas.
 *
 * @param title título do cartão, como Início ou Horário.
 * @param time horário escolhido no momento.
 * @param onDismiss fecha o relógio sem trocar.
 * @param onConfirm recebe o novo horário.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FormTimeDialog(title: String, time: LocalTime, onDismiss: () -> Unit, onConfirm: (LocalTime) -> Unit) {
    val colors = ReadingTheme.colors
    val state = rememberTimePickerState(initialHour = time.hour, initialMinute = time.minute, is24Hour = true)
    CardDialog(title = title, onDismiss = onDismiss) {
        TimePicker(
            state = state,
            modifier = Modifier.align(Alignment.CenterHorizontally),
            colors = TimePickerDefaults.colors(
                clockDialColor = colors.surface2,
                selectorColor = colors.accent,
                containerColor = colors.surface,
                timeSelectorSelectedContainerColor = colors.surface2,
                timeSelectorUnselectedContainerColor = colors.bg,
                timeSelectorSelectedContentColor = colors.text,
                timeSelectorUnselectedContentColor = colors.text2
            )
        )
        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            PrimaryButton(
                text = stringResource(R.string.action_save),
                onClick = { onConfirm(LocalTime.of(state.hour, state.minute)) }
            )
            PlainButton(text = stringResource(R.string.action_cancel), onClick = onDismiss)
        }
    }
}

/**
 * Converte o dia para o início dele em UTC, que é como o calendário do Material trabalha.
 *
 * @receiver dia do calendário.
 * @return milissegundos da meia-noite em UTC.
 */
private fun LocalDate.toUtcMillis(): Long = atStartOfDay(ZoneOffset.UTC).toInstant().toEpochMilli()

@OptIn(ExperimentalMaterial3Api::class)
private class LimitedDates(private val todayMillis: Long, private val limit: DateLimit) : SelectableDates {

    /**
     * Libera no calendário só os dias do lado permitido de hoje, incluindo o próprio dia.
     *
     * @param utcTimeMillis início do dia em UTC, como o calendário informa.
     * @return true quando o dia pode ser escolhido.
     */
    override fun isSelectableDate(utcTimeMillis: Long): Boolean = when (limit) {
        DateLimit.UNTIL_TODAY -> utcTimeMillis <= todayMillis
        DateLimit.FROM_TODAY -> utcTimeMillis >= todayMillis
    }

    /**
     * Libera no calendário só os anos que ainda têm dias permitidos.
     *
     * @param year ano exibido no seletor de anos.
     * @return true quando o ano pode ser escolhido.
     */
    override fun isSelectableYear(year: Int): Boolean {
        val current = LocalDate.now().year
        return when (limit) {
            DateLimit.UNTIL_TODAY -> year <= current
            DateLimit.FROM_TODAY -> year >= current
        }
    }
}
