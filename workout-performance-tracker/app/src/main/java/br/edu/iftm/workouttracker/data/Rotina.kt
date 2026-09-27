package br.edu.iftm.workouttracker.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "rotina")
data class Rotina(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val nome: String,
    val grupoMuscular: String,
    val realizadoEm: Long = System.currentTimeMillis()
)
