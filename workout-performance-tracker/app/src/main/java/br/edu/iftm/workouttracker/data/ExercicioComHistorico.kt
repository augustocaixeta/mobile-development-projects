package br.edu.iftm.workouttracker.data

import androidx.room.Embedded
import androidx.room.Relation

data class RegistroComRotina(
    @Embedded val registro: RegistroCarga,
    @Relation(parentColumn = "rotinaId", entityColumn = "id")
    val rotina: Rotina
)

data class ExercicioComHistorico(
    @Embedded val exercicio: Exercicio,
    @Relation(
        parentColumn = "id",
        entityColumn = "exercicioId",
        entity = RegistroCarga::class
    )
    val historico: List<RegistroComRotina>
)
