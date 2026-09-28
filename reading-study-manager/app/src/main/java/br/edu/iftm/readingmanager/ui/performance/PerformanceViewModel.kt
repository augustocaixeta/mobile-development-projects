package br.edu.iftm.readingmanager.ui.performance

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.createSavedStateHandle
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import br.edu.iftm.readingmanager.ReadingApp
import br.edu.iftm.readingmanager.data.BookRepository
import br.edu.iftm.readingmanager.data.GoalRepository
import br.edu.iftm.readingmanager.data.SessionRepository
import java.time.LocalDate
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class PerformanceUiState(
    val period: Period = Period.WEEK,
    val stats: PeriodStats = PeriodStats(),
    val streak: Int = 0,
    val achievements: List<Achievement> = emptyList(),
    val goal: Int? = null,
    val loaded: Boolean = false
)

class PerformanceViewModel(
    private val savedStateHandle: SavedStateHandle,
    books: BookRepository,
    sessions: SessionRepository,
    private val goals: GoalRepository
) : ViewModel() {

    private val period = savedStateHandle.getStateFlow(KEY_PERIOD, Period.WEEK)
    private val today = MutableStateFlow(LocalDate.now())

    val state: StateFlow<PerformanceUiState> =
        combine(
            sessions.observeAll(),
            books.observeFiltered(null, null),
            goals.observeMonthlyPages(),
            period,
            today
        ) { sessionList, bookList, goal, selected, date ->
            val daily = pagesByDay(sessionList)
            PerformanceUiState(
                period = selected,
                stats = periodStats(selected, date, daily, bookList),
                streak = streakDays(sessionList, date),
                achievements = achievements(bookList, daily, goal),
                goal = goal,
                loaded = true
            )
        }
            .flowOn(Dispatchers.Default)
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), PerformanceUiState())

    /**
     * Troca o período exibido nas abas.
     *
     * @param selected semana, mês ou ano.
     */
    fun selectPeriod(selected: Period) {
        savedStateHandle[KEY_PERIOD] = selected
    }

    /**
     * Grava a meta mensal de páginas, usada nas conquistas.
     *
     * @param pages meta já validada, maior que zero.
     */
    fun setGoal(pages: Int) {
        viewModelScope.launch {
            goals.setMonthlyPages(pages)
        }
    }

    /**
     * Atualiza a data de referência, para o gráfico acompanhar a virada do dia.
     */
    fun refreshDate() {
        today.value = LocalDate.now()
    }

    companion object {
        private const val KEY_PERIOD = "performance_period"

        val Factory = viewModelFactory {
            initializer {
                val app = this[APPLICATION_KEY] as ReadingApp
                PerformanceViewModel(createSavedStateHandle(), app.books, app.sessions, app.goals)
            }
        }
    }
}
