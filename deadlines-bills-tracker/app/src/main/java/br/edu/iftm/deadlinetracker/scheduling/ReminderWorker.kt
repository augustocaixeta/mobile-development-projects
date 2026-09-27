package br.edu.iftm.deadlinetracker.scheduling

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import br.edu.iftm.deadlinetracker.DeadlineTrackerApp
import br.edu.iftm.deadlinetracker.data.ReminderState
import br.edu.iftm.deadlinetracker.data.isCompleted

class ReminderWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    /**
     * Exibe a notificação do lembrete apenas se ele continua agendado e a obrigação segue pendente.
     *
     * @return sempre sucesso, já que um lembrete obsoleto não precisa ser repetido.
     */
    override suspend fun doWork(): Result {
        val repository = (applicationContext as DeadlineTrackerApp).repository
        val reminderId = inputData.getLong(KEY_REMINDER_ID, -1L)
        val reminder = repository.findReminder(reminderId) ?: return Result.success()
        if (reminder.state != ReminderState.SCHEDULED) {
            return Result.success()
        }
        val obligation = repository.find(reminder.obligationId) ?: return Result.success()
        if (obligation.isCompleted) {
            return Result.success()
        }
        Notifications.showReminder(applicationContext, obligation)
        repository.markFired(reminder.id)
        return Result.success()
    }

    companion object {
        const val KEY_REMINDER_ID = "reminder_id"
    }
}
