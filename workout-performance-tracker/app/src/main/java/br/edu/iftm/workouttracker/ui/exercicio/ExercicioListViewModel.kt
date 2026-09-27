package br.edu.iftm.workouttracker.ui.exercicio

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import br.edu.iftm.workouttracker.data.ExercicioResumo
import br.edu.iftm.workouttracker.repository.WorkoutRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

class ExercicioListViewModel(repository: WorkoutRepository) : ViewModel() {

    private val exercicios = repository.observarResumosExercicios()

    val busca = MutableStateFlow("")
    val grupoSelecionado = MutableStateFlow(GRUPO_TODOS)

    val grupos: StateFlow<List<String>> = exercicios
        .map { lista -> listOf(GRUPO_TODOS) + lista.map { it.exercicio.grupoMuscular }.distinct().sorted() }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), listOf(GRUPO_TODOS))

    val exerciciosFiltrados: StateFlow<List<ExercicioResumo>> = combine(
        exercicios,
        busca,
        grupoSelecionado
    ) { lista, query, grupo ->
        lista.filter {
            (grupo == GRUPO_TODOS || it.exercicio.grupoMuscular == grupo) &&
                (query.isBlank() || it.exercicio.nome.contains(query, ignoreCase = true))
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun atualizarBusca(texto: String) {
        busca.value = texto
    }

    fun selecionarGrupo(grupo: String) {
        grupoSelecionado.value = grupo
    }

    companion object {
        const val GRUPO_TODOS = "Todos"
    }
}
