package br.edu.iftm.workouttracker.ui.exercicio

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import br.edu.iftm.workouttracker.data.Exercicio
import br.edu.iftm.workouttracker.repository.WorkoutRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class NovoExercicioViewModel(
    private val repository: WorkoutRepository,
    private val exercicioId: Long
) : ViewModel() {

    private val _exercicioOriginal = MutableStateFlow<Exercicio?>(null)
    val exercicioOriginal: StateFlow<Exercicio?> = _exercicioOriginal.asStateFlow()

    init {
        if (exercicioId != 0L) {
            viewModelScope.launch {
                _exercicioOriginal.value = repository.obterExercicio(exercicioId)
            }
        }
    }

    fun salvar(nome: String, grupoMuscular: String, aoConcluir: () -> Unit) {
        viewModelScope.launch {
            val exercicio = _exercicioOriginal.value?.copy(nome = nome, grupoMuscular = grupoMuscular)
                ?: Exercicio(nome = nome, grupoMuscular = grupoMuscular)
            repository.salvarExercicio(exercicio)
            aoConcluir()
        }
    }

    fun excluir(aoConcluir: () -> Unit) {
        val exercicio = _exercicioOriginal.value ?: return
        viewModelScope.launch {
            repository.excluirExercicio(exercicio)
            aoConcluir()
        }
    }
}
