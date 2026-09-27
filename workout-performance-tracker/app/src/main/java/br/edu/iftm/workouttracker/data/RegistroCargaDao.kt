package br.edu.iftm.workouttracker.data

import androidx.room.Dao
import androidx.room.Query

@Dao
interface RegistroCargaDao {

    @Query(
        """
        SELECT * FROM registro_carga r
        WHERE r.id = (
            SELECT r2.id FROM registro_carga r2
            WHERE r2.exercicioId = r.exercicioId
            ORDER BY r2.dataHora DESC, r2.id DESC
            LIMIT 1
        )
        """
    )
    suspend fun ultimosPorExercicio(): List<RegistroCarga>
}
