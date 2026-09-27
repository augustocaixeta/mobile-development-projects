package br.edu.iftm.workouttracker.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "corrida")
data class Corrida(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val distanciaKm: Double,
    val tempoSegundos: Long,
    val dataHora: Long = System.currentTimeMillis()
)
