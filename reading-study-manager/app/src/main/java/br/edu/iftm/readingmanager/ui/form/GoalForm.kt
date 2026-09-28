package br.edu.iftm.readingmanager.ui.form

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import br.edu.iftm.readingmanager.R
import br.edu.iftm.readingmanager.ReadingApp
import br.edu.iftm.readingmanager.data.GoalRepository
import br.edu.iftm.readingmanager.data.SessionRepository
import br.edu.iftm.readingmanager.data.pagesRead
import br.edu.iftm.readingmanager.ui.components.BottomActions
import br.edu.iftm.readingmanager.ui.components.FieldLabel
import br.edu.iftm.readingmanager.ui.components.LargeNumberField
import br.edu.iftm.readingmanager.ui.components.Pill
import br.edu.iftm.readingmanager.ui.components.PrimaryButton
import br.edu.iftm.readingmanager.ui.theme.ReadingTheme
import br.edu.iftm.readingmanager.util.Formats
import br.edu.iftm.readingmanager.util.toLocalDateTime
import java.time.LocalDate
import java.time.YearMonth
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

private val GoalSuggestions = listOf(300, 500, 800, 1000)

data class GoalFormState(
    val pages: String = "",
    val monthPages: Int = 0,
    val month: LocalDate = LocalDate.now(),
    val error: Boolean = false,
    val loaded: Boolean = false,
    val saved: Boolean = false
)

private data class GoalDraft(
    val pages: String? = null,
    val error: Boolean = false,
    val saved: Boolean = false
)

class GoalFormViewModel(
    private val goals: GoalRepository,
    sessions: SessionRepository
) : ViewModel() {

    private val draft = MutableStateFlow(GoalDraft())

    val state: StateFlow<GoalFormState> =
        combine(goals.observeMonthlyPages(), sessions.observeAll(), draft) { goal, list, current ->
            val month = YearMonth.now()
            GoalFormState(
                pages = current.pages ?: goal?.toString().orEmpty(),
                monthPages = list
                    .filter { YearMonth.from(it.startedAt.toLocalDateTime()) == month }
                    .sumOf { it.pagesRead },
                month = month.atDay(1),
                error = current.error,
                loaded = true,
                saved = current.saved
            )
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), GoalFormState())

    /**
     * Atualiza a meta digitada ou escolhida nas sugestões.
     *
     * @param pages texto da meta, só com dígitos.
     */
    fun change(pages: String) {
        draft.value = draft.value.copy(pages = pages, error = false)
    }

    /**
     * Grava a meta mensal quando ela é maior que zero.
     */
    fun save() {
        viewModelScope.launch {
            val typed = draft.value.pages ?: goals.observeMonthlyPages().first()?.toString().orEmpty()
            val pages = typed.toIntOrNull()
            if (pages == null || pages <= 0) {
                draft.value = draft.value.copy(error = true)
                return@launch
            }
            goals.setMonthlyPages(pages)
            draft.value = draft.value.copy(saved = true)
        }
    }

    companion object {
        val Factory = viewModelFactory {
            initializer {
                val app = this[APPLICATION_KEY] as ReadingApp
                GoalFormViewModel(app.goals, app.sessions)
            }
        }
    }
}

/**
 * Segmento Meta do Novo registro: define quantas páginas ler por mês, com sugestões prontas
 * e o quanto já foi lido no mês atual.
 *
 * @param onSaved chamado depois que a meta é gravada.
 * @param modifier modificador aplicado ao segmento.
 * @param viewModel estado do segmento, criado pela fábrica do app.
 */
@Composable
fun GoalForm(
    onSaved: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: GoalFormViewModel = viewModel(factory = GoalFormViewModel.Factory)
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    LaunchedEffect(state.saved) {
        if (state.saved) {
            onSaved()
        }
    }
    GoalFormContent(state = state, onChange = viewModel::change, onSave = viewModel::save, modifier = modifier)
}

/**
 * Campos do segmento Meta, sem depender do ViewModel.
 *
 * @param state estado atual do segmento.
 * @param onChange recebe a meta digitada ou sugerida.
 * @param onSave grava a meta.
 * @param modifier modificador aplicado ao segmento.
 */
@Composable
private fun GoalFormContent(
    state: GoalFormState,
    onChange: (String) -> Unit,
    onSave: () -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = ReadingTheme.colors
    Column(modifier = modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp)
        ) {
            LargeNumberField(
                label = stringResource(R.string.goal_dialog_label),
                value = state.pages,
                onValueChange = onChange,
                modifier = Modifier.padding(top = 28.dp),
                error = if (state.error) stringResource(R.string.error_goal_zero) else null,
                imeAction = ImeAction.Done
            )
            Text(
                text = stringResource(
                    R.string.goal_form_month_progress,
                    Formats.monthName(state.month),
                    pluralStringResource(R.plurals.pages_count, state.monthPages, Formats.integer(state.monthPages))
                ),
                modifier = Modifier.padding(top = 4.dp),
                style = ReadingTheme.typography.caption,
                color = colors.text3
            )
            Column(modifier = Modifier.padding(top = 20.dp)) {
                FieldLabel(label = stringResource(R.string.goal_form_suggestions), error = null)
                FlowRow(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 10.dp)
                        .selectableGroup(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    GoalSuggestions.forEach { pages ->
                        Pill(
                            text = pluralStringResource(R.plurals.pages_count, pages, Formats.integer(pages)),
                            selected = state.pages == pages.toString(),
                            onClick = { onChange(pages.toString()) }
                        )
                    }
                }
            }
            Text(
                text = stringResource(R.string.goal_form_hint),
                modifier = Modifier.padding(top = 20.dp, bottom = 8.dp),
                style = ReadingTheme.typography.caption,
                color = colors.text3
            )
        }
        BottomActions {
            PrimaryButton(text = stringResource(R.string.action_save), onClick = onSave, enabled = state.loaded)
        }
    }
}
