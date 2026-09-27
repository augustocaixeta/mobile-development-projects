package br.edu.iftm.workouttracker.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "exercicio")
data class Exercicio(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val nome: String,
    val grupoMuscular: String
)
