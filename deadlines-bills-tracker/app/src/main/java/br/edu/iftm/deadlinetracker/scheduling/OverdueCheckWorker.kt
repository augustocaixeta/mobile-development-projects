package br.edu.iftm.deadlinetracker.scheduling

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import br.edu.iftm.deadlinetracker.DeadlineTrackerApp

class OverdueCheckWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    /**
     * Roda uma vez por dia e avisa quando existe algum registro pendente que já venceu.
     *
     * @return sempre sucesso, a próxima execução acontece no dia seguinte.
     */
    override suspend fun doWork(): Result {
        val overdue = (applicationContext as DeadlineTrackerApp).repository.listOverdue()
        if (overdue.isNotEmpty()) {
            Notifications.showOverdue(applicationContext, overdue)
        }
        return Result.success()
    }
}
