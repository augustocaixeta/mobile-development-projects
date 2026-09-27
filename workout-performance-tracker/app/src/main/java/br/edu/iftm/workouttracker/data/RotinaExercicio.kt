package br.edu.iftm.workouttracker.data

import androidx.room.Embedded
import androidx.room.Relation

data class RotinaExercicio(
    @Embedded val registro: RegistroCarga,
    @Relation(parentColumn = "exercicioId", entityColumn = "id")
    val exercicio: Exercicio
)
