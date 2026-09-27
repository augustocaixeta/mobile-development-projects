package br.edu.iftm.workouttracker

import android.app.Application
import br.edu.iftm.workouttracker.data.AppDatabase
import br.edu.iftm.workouttracker.repository.CorridaRepository
import br.edu.iftm.workouttracker.repository.WorkoutRepository

class WorkoutApp : Application() {

    val database by lazy { AppDatabase.obterInstancia(this) }

    val workoutRepository by lazy {
        WorkoutRepository(
            database.rotinaDao(),
            database.exercicioDao(),
            database.registroCargaDao()
        )
    }

    val corridaRepository by lazy { CorridaRepository(database.corridaDao()) }
}
