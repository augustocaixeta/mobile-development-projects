package br.edu.iftm.workouttracker.ui.exercicio

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import br.edu.iftm.workouttracker.data.Exercicio
import br.edu.iftm.workouttracker.data.RegistroComRotina
import br.edu.iftm.workouttracker.repository.WorkoutRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

data class EvolucaoExercicio(
    val exercicio: Exercicio,
    val historicoRecente: List<RegistroComRotina>,
    val cronologico: List<RegistroComRotina>,
    val ultimaCarga: Double?,
    val maiorCarga: Double?,
    val variacao: Double?
)

class ExercicioDetailViewModel(
    repository: WorkoutRepository,
    exercicioId: Long
) : ViewModel() {

    val evolucao: StateFlow<EvolucaoExercicio?> = repository.observarExercicio(exercicioId)
        .map { dados ->
            if (dados == null) return@map null
            val cronologico = dados.historico.sortedWith(
                compareBy<RegistroComRotina>({ it.registro.dataHora }, { it.registro.id })
            )
            val primeira = cronologico.firstOrNull()?.registro?.cargaKg
            val ultima = cronologico.lastOrNull()?.registro?.cargaKg
            EvolucaoExercicio(
                exercicio = dados.exercicio,
                historicoRecente = cronologico.reversed(),
                cronologico = cronologico,
                ultimaCarga = ultima,
                maiorCarga = cronologico.maxOfOrNull { it.registro.cargaKg },
                variacao = if (primeira != null && ultima != null) ultima - primeira else null
            )
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)
}
