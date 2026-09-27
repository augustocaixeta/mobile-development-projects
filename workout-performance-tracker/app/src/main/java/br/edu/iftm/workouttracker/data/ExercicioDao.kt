package br.edu.iftm.workouttracker.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface ExercicioDao {

    @Insert
    suspend fun inserir(exercicio: Exercicio): Long

    @Update
    suspend fun atualizar(exercicio: Exercicio)

    @Delete
    suspend fun excluir(exercicio: Exercicio)

    @Query("SELECT * FROM exercicio WHERE id = :exercicioId")
    suspend fun obter(exercicioId: Long): Exercicio?

    @Query("SELECT * FROM exercicio ORDER BY nome COLLATE NOCASE ASC")
    fun observarTodos(): Flow<List<Exercicio>>

    @Query(
        """
        SELECT e.id, e.nome, e.grupoMuscular,
            (SELECT COUNT(*) FROM registro_carga r WHERE r.exercicioId = e.id) AS totalRegistros,
            (SELECT r.cargaKg FROM registro_carga r WHERE r.exercicioId = e.id
                ORDER BY r.dataHora DESC LIMIT 1) AS ultimaCarga
        FROM exercicio e
        ORDER BY e.nome COLLATE NOCASE ASC
        """
    )
    fun observarResumos(): Flow<List<ExercicioResumo>>

    @Transaction
    @Query("SELECT * FROM exercicio WHERE id = :exercicioId")
    fun observarComHistorico(exercicioId: Long): Flow<ExercicioComHistorico?>
}
