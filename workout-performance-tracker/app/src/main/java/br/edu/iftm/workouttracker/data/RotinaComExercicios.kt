package br.edu.iftm.workouttracker.data

import androidx.room.Embedded
import androidx.room.Relation

data class RotinaComExercicios(
    @Embedded val rotina: Rotina,
    @Relation(
        parentColumn = "id",
        entityColumn = "rotinaId",
        entity = RegistroCarga::class
    )
    val exercicios: List<RotinaExercicio>
)
