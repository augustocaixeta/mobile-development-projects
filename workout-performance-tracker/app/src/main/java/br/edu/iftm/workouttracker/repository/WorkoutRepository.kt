package br.edu.iftm.workouttracker.repository

import br.edu.iftm.workouttracker.data.Exercicio
import br.edu.iftm.workouttracker.data.ExercicioComHistorico
import br.edu.iftm.workouttracker.data.ExercicioDao
import br.edu.iftm.workouttracker.data.ExercicioResumo
import br.edu.iftm.workouttracker.data.RegistroCarga
import br.edu.iftm.workouttracker.data.RegistroCargaDao
import br.edu.iftm.workouttracker.data.Rotina
import br.edu.iftm.workouttracker.data.RotinaComExercicios
import br.edu.iftm.workouttracker.data.RotinaDao
import kotlinx.coroutines.flow.Flow

class WorkoutRepository(
    private val rotinaDao: RotinaDao,
    private val exercicioDao: ExercicioDao,
    private val registroCargaDao: RegistroCargaDao
) {

    fun observarRotinas(): Flow<List<RotinaComExercicios>> =
        rotinaDao.observarTodasComExercicios()

    fun observarRotina(rotinaId: Long): Flow<RotinaComExercicios?> =
        rotinaDao.observarComExercicios(rotinaId)

    suspend fun obterRotina(rotinaId: Long): RotinaComExercicios? =
        rotinaDao.obterComExercicios(rotinaId)

    suspend fun salvarRotina(rotina: Rotina, registros: List<RegistroCarga>): Long =
        rotinaDao.salvarComRegistros(rotina, registros)

    suspend fun excluirRotina(rotina: Rotina) =
        rotinaDao.excluir(rotina)

    fun observarExercicios(): Flow<List<Exercicio>> =
        exercicioDao.observarTodos()

    fun observarResumosExercicios(): Flow<List<ExercicioResumo>> =
        exercicioDao.observarResumos()

    fun observarExercicio(exercicioId: Long): Flow<ExercicioComHistorico?> =
        exercicioDao.observarComHistorico(exercicioId)

    suspend fun obterExercicio(exercicioId: Long): Exercicio? =
        exercicioDao.obter(exercicioId)

    suspend fun salvarExercicio(exercicio: Exercicio) {
        if (exercicio.id == 0L) {
            exercicioDao.inserir(exercicio)
        } else {
            exercicioDao.atualizar(exercicio)
        }
    }

    suspend fun excluirExercicio(exercicio: Exercicio) =
        exercicioDao.excluir(exercicio)

    suspend fun ultimosRegistrosPorExercicio(): Map<Long, RegistroCarga> =
        registroCargaDao.ultimosPorExercicio().associateBy { it.exercicioId }
}
