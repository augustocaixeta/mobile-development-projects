package br.edu.iftm.workouttracker.ui.rotina

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import br.edu.iftm.workouttracker.data.RotinaComExercicios
import br.edu.iftm.workouttracker.repository.WorkoutRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn

class RotinaListViewModel(repository: WorkoutRepository) : ViewModel() {

    val rotinas: StateFlow<List<RotinaComExercicios>> = repository.observarRotinas()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
}
