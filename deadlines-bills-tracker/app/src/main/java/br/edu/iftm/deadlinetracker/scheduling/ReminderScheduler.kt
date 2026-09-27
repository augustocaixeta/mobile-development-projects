package br.edu.iftm.deadlinetracker.scheduling

import android.content.Context
import androidx.core.app.NotificationManagerCompat
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.workDataOf
import br.edu.iftm.deadlinetracker.data.Reminder
import java.time.Duration
import java.time.LocalDateTime
import java.util.concurrent.TimeUnit

class ReminderScheduler(context: Context) {

    private val workManager = WorkManager.getInstance(context)
    private val notificationManager = NotificationManagerCompat.from(context)

    /**
     * Enfileira o disparo do lembrete no WorkManager para o horário calculado.
     * Cada lembrete tem um nome único, então reagendar substitui o trabalho anterior.
     *
     * @param reminder lembrete já gravado no banco, com id definido.
     */
    fun schedule(reminder: Reminder) {
        val delay = (reminder.triggerAt - System.currentTimeMillis()).coerceAtLeast(0)
        val request = OneTimeWorkRequestBuilder<ReminderWorker>()
            .setInitialDelay(delay, TimeUnit.MILLISECONDS)
            .setInputData(workDataOf(ReminderWorker.KEY_REMINDER_ID to reminder.id))
            .addTag(tagFor(reminder.obligationId))
            .build()
        workManager.enqueueUniqueWork(REMINDER_PREFIX + reminder.id, ExistingWorkPolicy.REPLACE, request)
    }

    /**
     * Cancela os lembretes pendentes da obrigação e remove da tela a notificação dela, se houver.
     *
     * @param obligationId identificador da obrigação.
     */
    fun cancel(obligationId: Long) {
        workManager.cancelAllWorkByTag(tagFor(obligationId))
        notificationManager.cancel(Notifications.idFor(obligationId))
    }

    /**
     * Agenda a verificação diária de atrasos, sempre perto das 9h. Se já existir, mantém a atual.
     */
    fun scheduleDailyCheck() {
        val now = LocalDateTime.now()
        val todayAtTime = now.toLocalDate().atTime(CHECK_HOUR, 0)
        val next = if (todayAtTime.isAfter(now)) todayAtTime else todayAtTime.plusDays(1)
        val request = PeriodicWorkRequestBuilder<OverdueCheckWorker>(1, TimeUnit.DAYS)
            .setInitialDelay(Duration.between(now, next).toMillis(), TimeUnit.MILLISECONDS)
            .build()
        workManager.enqueueUniquePeriodicWork(
            DAILY_CHECK_NAME,
            ExistingPeriodicWorkPolicy.KEEP,
            request
        )
    }

    /**
     * Monta a tag que agrupa os trabalhos de uma obrigação.
     *
     * @param obligationId identificador da obrigação.
     * @return tag usada no WorkManager.
     */
    private fun tagFor(obligationId: Long) = "obligation_$obligationId"

    companion object {
        private const val REMINDER_PREFIX = "reminder_"
        private const val DAILY_CHECK_NAME = "overdue_check"
        private const val CHECK_HOUR = 9
    }
}
