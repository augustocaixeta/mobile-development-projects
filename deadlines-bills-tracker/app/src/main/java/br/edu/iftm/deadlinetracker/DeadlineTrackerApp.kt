package br.edu.iftm.deadlinetracker

import android.app.Application
import br.edu.iftm.deadlinetracker.data.AppDatabase
import br.edu.iftm.deadlinetracker.data.ObligationRepository
import br.edu.iftm.deadlinetracker.scheduling.Notifications
import br.edu.iftm.deadlinetracker.scheduling.ReminderScheduler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob

class DeadlineTrackerApp : Application() {

    val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    val scheduler by lazy { ReminderScheduler(this) }

    val repository by lazy { ObligationRepository(AppDatabase.getInstance(this), scheduler) }

    /**
     * Cria os canais de notificação e garante que a verificação diária de atrasos esteja agendada.
     */
    override fun onCreate() {
        super.onCreate()
        Notifications.createChannels(this)
        scheduler.scheduleDailyCheck()
    }
}
