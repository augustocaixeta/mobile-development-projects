package br.edu.iftm.workouttracker.ui.corrida

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import br.edu.iftm.workouttracker.data.Corrida
import br.edu.iftm.workouttracker.repository.CorridaRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class CorridaViewModel(private val repository: CorridaRepository) : ViewModel() {

    val corridas: StateFlow<List<Corrida>> = repository.observarCorridas()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun registrar(distanciaKm: Double, tempoSegundos: Long) {
        viewModelScope.launch {
            repository.registrarCorrida(distanciaKm, tempoSegundos)
        }
    }
}
