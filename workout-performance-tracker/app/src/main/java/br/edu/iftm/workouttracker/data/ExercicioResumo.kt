package br.edu.iftm.workouttracker.data

import androidx.room.Embedded

data class ExercicioResumo(
    @Embedded val exercicio: Exercicio,
    val totalRegistros: Int,
    val ultimaCarga: Double?
)
