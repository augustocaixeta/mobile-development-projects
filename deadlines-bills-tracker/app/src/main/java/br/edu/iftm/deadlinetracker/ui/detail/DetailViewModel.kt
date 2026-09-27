package br.edu.iftm.deadlinetracker.ui.detail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.createSavedStateHandle
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import br.edu.iftm.deadlinetracker.DeadlineTrackerApp
import br.edu.iftm.deadlinetracker.data.ObligationRepository
import br.edu.iftm.deadlinetracker.data.ObligationWithReminders
import br.edu.iftm.deadlinetracker.data.isCompleted
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

sealed interface DetailState {
    data object Loading : DetailState
    data object Removed : DetailState
    data class Ready(val data: ObligationWithReminders) : DetailState
}

enum class DetailEvent {
    COMPLETED,
    REOPENED
}

class DetailViewModel(
    private val repository: ObligationRepository,
    val id: Long
) : ViewModel() {

    val state: StateFlow<DetailState> = repository.observe(id)
        .map { data -> if (data == null) DetailState.Removed else DetailState.Ready(data) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), DetailState.Loading)

    private val eventChannel = Channel<DetailEvent>(Channel.BUFFERED)
    val events: Flow<DetailEvent> = eventChannel.receiveAsFlow()

    /**
     * Liquida a obrigação pendente ou reabre a que já foi concluída.
     */
    fun toggleCompletion() {
        val current = (state.value as? DetailState.Ready)?.data?.obligation ?: return
        viewModelScope.launch {
            if (current.isCompleted) {
                repository.reopen(id)
                eventChannel.send(DetailEvent.REOPENED)
            } else {
                repository.settle(id)
                eventChannel.send(DetailEvent.COMPLETED)
            }
        }
    }

    /**
     * Exclui a obrigação. A tela fecha sozinha quando o banco deixa de encontrar o registro.
     */
    fun delete() {
        viewModelScope.launch {
            repository.delete(id)
        }
    }

    companion object {
        val Factory = viewModelFactory {
            initializer {
                val app = this[APPLICATION_KEY] as DeadlineTrackerApp
                val id = createSavedStateHandle().get<Long>(DetailActivity.EXTRA_ID) ?: -1L
                DetailViewModel(app.repository, id)
            }
        }
    }
}
