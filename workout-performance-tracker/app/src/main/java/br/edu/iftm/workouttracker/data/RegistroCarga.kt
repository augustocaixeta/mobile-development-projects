package br.edu.iftm.workouttracker.data

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "registro_carga",
    foreignKeys = [
        ForeignKey(
            entity = Rotina::class,
            parentColumns = ["id"],
            childColumns = ["rotinaId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = Exercicio::class,
            parentColumns = ["id"],
            childColumns = ["exercicioId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("rotinaId"), Index("exercicioId")]
)
data class RegistroCarga(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val rotinaId: Long,
    val exercicioId: Long,
    val cargaKg: Double,
    val series: Int,
    val repeticoes: Int,
    val dataHora: Long = System.currentTimeMillis()
)
