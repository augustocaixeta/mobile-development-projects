package br.edu.iftm.deadlinetracker.ui.summary

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import br.edu.iftm.deadlinetracker.DeadlineTrackerApp
import br.edu.iftm.deadlinetracker.data.Obligation
import br.edu.iftm.deadlinetracker.data.ObligationRepository
import br.edu.iftm.deadlinetracker.data.ObligationType
import br.edu.iftm.deadlinetracker.data.isCompleted
import br.edu.iftm.deadlinetracker.util.Formats
import br.edu.iftm.deadlinetracker.util.toLocalDateTime
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.LocalDateTime
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn

enum class Period {
    WEEK,
    MONTH,
    YEAR
}

data class Bar(
    val label: String,
    val value: Long,
    val highlighted: Boolean
)

data class SummaryUiState(
    val period: Period = Period.WEEK,
    val totalPaid: Long = 0,
    val totalReceived: Long = 0,
    val bars: List<Bar> = emptyList(),
    val completed: Int = 0,
    val pending: Int = 0,
    val overdue: Int = 0,
    val recent: List<Obligation> = emptyList(),
    val now: LocalDateTime = LocalDateTime.now()
)

class SummaryViewModel(repository: ObligationRepository) : ViewModel() {

    private val period = MutableStateFlow(Period.WEEK)

    val state: StateFlow<SummaryUiState> =
        combine(repository.observeAll(), period) { obligations, currentPeriod ->
            buildState(obligations, currentPeriod, LocalDateTime.now())
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), SummaryUiState())

    /**
     * Troca o período usado no total, no gráfico e nos cartões.
     *
     * @param selected período escolhido na aba.
     */
    fun selectPeriod(selected: Period) {
        period.value = selected
    }

    /**
     * Soma o que foi pago e recebido no período e conta concluídos, pendentes e atrasados.
     *
     * @param obligations todas as obrigações do banco.
     * @param currentPeriod período selecionado.
     * @param now instante de referência.
     * @return estado pronto para a tela.
     */
    private fun buildState(
        obligations: List<Obligation>,
        currentPeriod: Period,
        now: LocalDateTime
    ): SummaryUiState {
        val today = now.toLocalDate()
        val (start, end) = rangeOf(currentPeriod, today)
        val inRange = { date: LocalDate -> !date.isBefore(start) && date.isBefore(end) }
        val completed = obligations.filter { obligation ->
            obligation.completedAt?.let { inRange(it.toLocalDateTime().toLocalDate()) } == true
        }
        val paid = completed.filter { it.type == ObligationType.PAYABLE }
        return SummaryUiState(
            period = currentPeriod,
            totalPaid = paid.sumOf { it.amountCents ?: 0L },
            totalReceived = completed
                .filter { it.type == ObligationType.RECEIVABLE }
                .sumOf { it.amountCents ?: 0L },
            bars = buildBars(currentPeriod, today, paid),
            completed = completed.size,
            pending = obligations.count {
                !it.isCompleted && inRange(it.dueAt.toLocalDateTime().toLocalDate())
            },
            overdue = obligations.count { !it.isCompleted && it.dueAt.toLocalDateTime().isBefore(now) },
            recent = obligations
                .filter { it.isCompleted }
                .sortedByDescending { it.completedAt }
                .take(RECENT_COUNT),
            now = now
        )
    }

    /**
     * Calcula o intervalo do período atual.
     *
     * @param currentPeriod período selecionado.
     * @param today data de referência.
     * @return primeiro dia do período e o dia seguinte ao último, para comparar sem incluir o fim.
     */
    private fun rangeOf(currentPeriod: Period, today: LocalDate): Pair<LocalDate, LocalDate> =
        when (currentPeriod) {
            Period.WEEK -> today.with(DayOfWeek.MONDAY).let { it to it.plusWeeks(1) }
            Period.MONTH -> today.withDayOfMonth(1).let { it to it.plusMonths(1) }
            Period.YEAR -> today.withDayOfYear(1).let { it to it.plusYears(1) }
        }

    /**
     * Monta as barras do gráfico. Semana mostra os sete dias, mês mostra as semanas e ano os doze meses.
     * A barra do dia, da semana ou do mês atual fica em destaque.
     *
     * @param currentPeriod período selecionado.
     * @param today data de referência.
     * @param paid faturas pagas dentro do período.
     * @return barras na ordem de exibição.
     */
    private fun buildBars(currentPeriod: Period, today: LocalDate, paid: List<Obligation>): List<Bar> {
        val byDate = paid.mapNotNull { obligation ->
            obligation.completedAt?.let { it.toLocalDateTime().toLocalDate() to (obligation.amountCents ?: 0L) }
        }
        val sum = { matches: (LocalDate) -> Boolean ->
            byDate.filter { (date, _) -> matches(date) }.sumOf { (_, value) -> value }
        }
        return when (currentPeriod) {
            Period.WEEK -> {
                val monday = today.with(DayOfWeek.MONDAY)
                (0L until 7L).map { offset ->
                    val day = monday.plusDays(offset)
                    Bar(Formats.axisDay(day), sum { it == day }, day == today)
                }
            }
            Period.MONTH -> {
                val weeks = (today.lengthOfMonth() + 6) / 7
                val currentWeek = weekOfMonth(today)
                (1..weeks).map { week ->
                    Bar(
                        "S$week",
                        sum { it.year == today.year && it.month == today.month && weekOfMonth(it) == week },
                        week == currentWeek
                    )
                }
            }
            Period.YEAR -> (1..12).map { month ->
                Bar(
                    Formats.axisMonth(month),
                    sum { it.year == today.year && it.monthValue == month },
                    month == today.monthValue
                )
            }
        }
    }

    /**
     * Calcula a semana do mês contando blocos de sete dias a partir do dia 1.
     *
     * @param date data de referência.
     * @return número da semana, de 1 a 5.
     */
    private fun weekOfMonth(date: LocalDate): Int = (date.dayOfMonth - 1) / 7 + 1

    companion object {
        private const val RECENT_COUNT = 5

        val Factory = viewModelFactory {
            initializer {
                SummaryViewModel((this[APPLICATION_KEY] as DeadlineTrackerApp).repository)
            }
        }
    }
}
