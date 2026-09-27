package br.edu.iftm.deadlinetracker.ui.home

import androidx.annotation.StringRes
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import br.edu.iftm.deadlinetracker.DeadlineTrackerApp
import br.edu.iftm.deadlinetracker.R
import br.edu.iftm.deadlinetracker.data.Obligation
import br.edu.iftm.deadlinetracker.data.ObligationRepository
import br.edu.iftm.deadlinetracker.data.ObligationType
import br.edu.iftm.deadlinetracker.data.isCompleted
import br.edu.iftm.deadlinetracker.util.toEpochMillis
import br.edu.iftm.deadlinetracker.util.toLocalDateTime
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.YearMonth
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn

enum class Filter(val type: ObligationType?) {
    ALL(null),
    PAYABLE(ObligationType.PAYABLE),
    RECEIVABLE(ObligationType.RECEIVABLE),
    DEADLINES(ObligationType.DEADLINE)
}

sealed interface ListItem {
    data class Section(@param:StringRes val title: Int) : ListItem
    data class Entry(val obligation: Obligation, val now: LocalDateTime) : ListItem
}

data class HomeUiState(
    val today: LocalDate = LocalDate.now(),
    val filter: Filter = Filter.ALL,
    val totalPayable: Long = 0,
    val completedThisMonth: Int = 0,
    val percent: Int = 0,
    val items: List<ListItem> = emptyList(),
    val loaded: Boolean = false
)

class HomeViewModel(repository: ObligationRepository) : ViewModel() {

    private val filter = MutableStateFlow(Filter.ALL)
    private val clock = MutableStateFlow(LocalDateTime.now())

    val state: StateFlow<HomeUiState> =
        combine(repository.observeAll(), filter, clock) { obligations, currentFilter, now ->
            buildState(obligations, currentFilter, now)
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), HomeUiState())

    /**
     * Troca a aba de filtro exibida na lista.
     *
     * @param selected filtro escolhido pelo usuário.
     */
    fun selectFilter(selected: Filter) {
        filter.value = selected
    }

    /**
     * Atualiza o horário de referência usado para calcular atrasos e textos como em 2 dias.
     */
    fun refreshClock() {
        clock.value = LocalDateTime.now()
    }

    /**
     * Calcula o resumo do mês e separa a lista em seções de acordo com a urgência.
     *
     * @param obligations todas as obrigações do banco.
     * @param currentFilter filtro da aba selecionada.
     * @param now instante de referência.
     * @return estado pronto para a tela.
     */
    private fun buildState(
        obligations: List<Obligation>,
        currentFilter: Filter,
        now: LocalDateTime
    ): HomeUiState {
        val month = YearMonth.from(now)
        val thisMonth = obligations.filter { YearMonth.from(it.dueAt.toLocalDateTime()) == month }
        val totalPayable = thisMonth
            .filter { it.type == ObligationType.PAYABLE && !it.isCompleted }
            .sumOf { it.amountCents ?: 0L }
        val completedThisMonth = obligations.count { obligation ->
            obligation.completedAt?.let { YearMonth.from(it.toLocalDateTime()) == month } == true
        }
        val percent = if (thisMonth.isEmpty()) 0 else thisMonth.count { it.isCompleted } * 100 / thisMonth.size
        val visible = obligations.filter { currentFilter.type == null || it.type == currentFilter.type }
        return HomeUiState(
            today = now.toLocalDate(),
            filter = currentFilter,
            totalPayable = totalPayable,
            completedThisMonth = completedThisMonth,
            percent = percent,
            items = group(visible, now),
            loaded = true
        )
    }

    /**
     * Monta as seções atrasados, hoje, próximos 7 dias, mais adiante e concluídos.
     * Concluídos continuam visíveis por 30 dias depois da liquidação.
     *
     * @param obligations obrigações já filtradas pela aba.
     * @param now instante de referência.
     * @return itens da lista com os cabeçalhos de seção.
     */
    private fun group(obligations: List<Obligation>, now: LocalDateTime): List<ListItem> {
        val today = now.toLocalDate()
        val weekEnd = today.plusDays(UPCOMING_DAYS)
        val completedCutoff = now.minusDays(COMPLETED_DAYS).toEpochMillis()
        val pending = obligations.filter { !it.isCompleted }
        val sections = linkedMapOf(
            R.string.section_overdue to pending.filter {
                it.dueAt.toLocalDateTime().isBefore(now)
            },
            R.string.section_today to pending.filter {
                val due = it.dueAt.toLocalDateTime()
                !due.isBefore(now) && due.toLocalDate() == today
            },
            R.string.section_next_days to pending.filter {
                val date = it.dueAt.toLocalDateTime().toLocalDate()
                date.isAfter(today) && !date.isAfter(weekEnd)
            },
            R.string.section_later to pending.filter {
                it.dueAt.toLocalDateTime().toLocalDate().isAfter(weekEnd)
            },
            R.string.section_completed to obligations
                .filter { it.isCompleted && (it.completedAt ?: 0L) >= completedCutoff }
                .sortedByDescending { it.completedAt }
        )
        return sections
            .filterValues { it.isNotEmpty() }
            .flatMap { (title, entries) ->
                listOf(ListItem.Section(title)) + entries.map { ListItem.Entry(it, now) }
            }
    }

    companion object {
        private const val UPCOMING_DAYS = 7L
        private const val COMPLETED_DAYS = 30L

        val Factory = viewModelFactory {
            initializer {
                HomeViewModel((this[APPLICATION_KEY] as DeadlineTrackerApp).repository)
            }
        }
    }
}
