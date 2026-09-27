package br.edu.iftm.workouttracker.ui.rotina

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import br.edu.iftm.workouttracker.data.RotinaComExercicios
import br.edu.iftm.workouttracker.repository.WorkoutRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn

class RotinaDetailViewModel(
    repository: WorkoutRepository,
    rotinaId: Long
) : ViewModel() {

    val rotina: StateFlow<RotinaComExercicios?> = repository.observarRotina(rotinaId)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)
}
