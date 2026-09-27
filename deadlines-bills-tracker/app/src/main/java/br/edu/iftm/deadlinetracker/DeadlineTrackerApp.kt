package br.edu.iftm.deadlinetracker

import android.app.Application
import br.edu.iftm.deadlinetracker.data.AppDatabase
import br.edu.iftm.deadlinetracker.data.ObligationRepository

class DeadlineTrackerApp : Application() {

    val repository by lazy { ObligationRepository(AppDatabase.getInstance(this)) }
}
