package br.edu.iftm.readingmanager.ui.performance

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.LifecycleResumeEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import br.edu.iftm.readingmanager.R
import br.edu.iftm.readingmanager.ui.components.BarIconButton
import br.edu.iftm.readingmanager.ui.components.CardDialog
import br.edu.iftm.readingmanager.ui.components.DateLeading
import br.edu.iftm.readingmanager.ui.components.FilterTabs
import br.edu.iftm.readingmanager.ui.components.Labels
import br.edu.iftm.readingmanager.ui.components.LargeNumberField
import br.edu.iftm.readingmanager.ui.components.ListRow
import br.edu.iftm.readingmanager.ui.components.PlainButton
import br.edu.iftm.readingmanager.ui.components.PrimaryButton
import br.edu.iftm.readingmanager.ui.components.TopBar
import br.edu.iftm.readingmanager.ui.components.TrailingValue
import br.edu.iftm.readingmanager.ui.theme.ReadingTheme
import br.edu.iftm.readingmanager.util.Formats
import java.util.Locale

private val TileShape = RoundedCornerShape(16.dp)
private val BarShape = RoundedCornerShape(6.dp)
private val ChartHeight = 130.dp
private val MinBarHeight = 4.dp
private val BrazilLocale: Locale = Locale.forLanguageTag("pt-BR")

/**
 * Desempenho de leitura por semana, mês e ano, com gráfico de páginas, números e conquistas.
 *
 * @param onBack volta para o Início.
 * @param onOpenBook abre o detalhe do livro de uma conquista.
 * @param viewModel estado da tela, criado pela fábrica do app.
 */
@Composable
fun PerformanceScreen(
    onBack: () -> Unit,
    onOpenBook: (Long) -> Unit,
    viewModel: PerformanceViewModel = viewModel(factory = PerformanceViewModel.Factory)
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    LifecycleResumeEffect(viewModel) {
        viewModel.refreshDate()
        onPauseOrDispose { }
    }
    PerformanceContent(
        state = state,
        onBack = onBack,
        onOpenBook = onOpenBook,
        onSelectPeriod = viewModel::selectPeriod,
        onSetGoal = viewModel::setGoal
    )
}

/**
 * Conteúdo do desempenho, sem depender do ViewModel.
 *
 * @param state estado atual da tela.
 * @param onBack volta para o Início.
 * @param onOpenBook abre o detalhe de um livro.
 * @param onSelectPeriod troca o período das abas.
 * @param onSetGoal grava a meta mensal de páginas.
 */
@Composable
private fun PerformanceContent(
    state: PerformanceUiState,
    onBack: () -> Unit,
    onOpenBook: (Long) -> Unit,
    onSelectPeriod: (Period) -> Unit,
    onSetGoal: (Int) -> Unit
) {
    val colors = ReadingTheme.colors
    var showGoalDialog by rememberSaveable { mutableStateOf(false) }
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.bg)
            .statusBarsPadding()
    ) {
        TopBar(
            navigationIcon = painterResource(R.drawable.ic_arrow_back),
            navigationDescription = stringResource(R.string.cd_back),
            onNavigate = onBack,
            actions = { GoalMenu(onSetGoal = { showGoalDialog = true }) }
        )
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .navigationBarsPadding()
                .padding(start = 16.dp, top = 8.dp, end = 16.dp, bottom = 24.dp)
        ) {
            Text(
                text = stringResource(R.string.performance_title),
                style = ReadingTheme.typography.title,
                color = colors.text
            )
            FilterTabs(
                labels = listOf(
                    stringResource(R.string.performance_tab_week),
                    stringResource(R.string.performance_tab_month),
                    stringResource(R.string.performance_tab_year)
                ),
                selectedIndex = state.period.ordinal,
                onSelect = { index -> onSelectPeriod(Period.entries[index]) },
                modifier = Modifier.padding(top = 20.dp)
            )
            PagesHeadline(period = state.period, stats = state.stats, modifier = Modifier.padding(top = 20.dp))
            PagesChart(period = state.period, bars = state.stats.bars, modifier = Modifier.padding(top = 28.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 24.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                StatTile(
                    value = Formats.integer(state.stats.booksFinished),
                    label = stringResource(R.string.performance_stat_books),
                    modifier = Modifier.weight(1f)
                )
                StatTile(
                    value = Formats.integer(state.stats.pages),
                    label = stringResource(R.string.performance_stat_pages),
                    modifier = Modifier.weight(1f)
                )
                StatTile(
                    value = Formats.integer(state.streak),
                    label = stringResource(R.string.performance_stat_streak),
                    modifier = Modifier.weight(1f)
                )
            }
            Achievements(
                achievements = state.achievements,
                loaded = state.loaded,
                onOpenBook = onOpenBook,
                modifier = Modifier.padding(top = 28.dp)
            )
        }
    }
    if (showGoalDialog) {
        GoalDialog(
            goal = state.goal,
            onDismiss = { showGoalDialog = false },
            onConfirm = { pages ->
                showGoalDialog = false
                onSetGoal(pages)
            }
        )
    }
}

/**
 * Ícone de mais opções com o item de definir a meta mensal.
 *
 * @param onSetGoal abre o diálogo da meta.
 */
@Composable
private fun GoalMenu(onSetGoal: () -> Unit) {
    var expanded by rememberSaveable { mutableStateOf(false) }
    Box {
        BarIconButton(
            painter = painterResource(R.drawable.ic_more_horiz),
            contentDescription = stringResource(R.string.cd_more_options),
            onClick = { expanded = true }
        )
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            containerColor = ReadingTheme.colors.surface2
        ) {
            DropdownMenuItem(
                text = {
                    Text(
                        text = stringResource(R.string.action_set_goal),
                        style = ReadingTheme.typography.body,
                        color = ReadingTheme.colors.text
                    )
                },
                onClick = {
                    expanded = false
                    onSetGoal()
                }
            )
        }
    }
}

/**
 * Total de páginas do período em destaque, com a comparação com o período anterior.
 *
 * @param period período escolhido nas abas.
 * @param stats números do período.
 * @param modifier modificador aplicado ao bloco.
 */
@Composable
private fun PagesHeadline(period: Period, stats: PeriodStats, modifier: Modifier = Modifier) {
    val colors = ReadingTheme.colors
    val typography = ReadingTheme.typography
    val change = changePercent(stats.pages, stats.previousPages)
    val previousLabel = stringResource(
        when (period) {
            Period.WEEK -> R.string.performance_previous_week
            Period.MONTH -> R.string.performance_previous_month
            Period.YEAR -> R.string.performance_previous_year
        }
    )
    val changeText = when {
        change == null -> stringResource(R.string.performance_no_previous, previousLabel)
        change >= 0 -> stringResource(R.string.performance_change_up, change, previousLabel)
        else -> stringResource(R.string.performance_change_down, change, previousLabel)
    }
    val changeColor = when {
        change == null -> colors.text3
        change >= 0 -> colors.positive
        else -> colors.accent
    }
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(text = stringResource(R.string.performance_pages_read), style = typography.caption, color = colors.text2)
        Text(
            text = pluralStringResource(R.plurals.pages_count, stats.pages, Formats.integer(stats.pages)),
            style = typography.display,
            color = colors.text
        )
        Text(text = changeText, style = typography.caption, color = changeColor)
    }
}

/**
 * Gráfico de barras com as páginas de cada parte do período. A barra em andamento fica clara.
 *
 * @param period período escolhido, que define os rótulos.
 * @param bars barras em ordem cronológica.
 * @param modifier modificador aplicado ao gráfico.
 */
@Composable
private fun PagesChart(period: Period, bars: List<ChartBar>, modifier: Modifier = Modifier) {
    val colors = ReadingTheme.colors
    val highest = bars.maxOfOrNull { it.pages } ?: 0
    Column(modifier = modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(ChartHeight),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.Bottom
        ) {
            bars.forEach { bar ->
                val label = barLabel(period, bar)
                val description = stringResource(
                    R.string.performance_bar_description,
                    label,
                    pluralStringResource(R.plurals.pages_count, bar.pages, Formats.integer(bar.pages))
                )
                val fraction = if (highest > 0) bar.pages.toFloat() / highest else 0f
                val barHeight = maxOf(MinBarHeight, ChartHeight * fraction)
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .semantics { contentDescription = description },
                    contentAlignment = Alignment.BottomCenter
                ) {
                    Box(
                        modifier = Modifier
                            .widthIn(max = 28.dp)
                            .fillMaxWidth()
                            .height(barHeight)
                            .clip(BarShape)
                            .background(if (bar.current) colors.text else colors.surface)
                    )
                }
            }
        }
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            bars.forEach { bar ->
                Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.Center) {
                    Text(
                        text = barLabel(period, bar),
                        style = ReadingTheme.typography.overline.copy(
                            fontSize = 9.sp,
                            lineHeight = 12.sp,
                            letterSpacing = 0.5.sp
                        ),
                        color = if (bar.current) colors.text else colors.text3,
                        maxLines = 1
                    )
                }
            }
        }
    }
}

/**
 * Rótulo de uma barra: dia da semana, semana do mês ou mês do ano, em maiúsculas.
 *
 * @param period período escolhido nas abas.
 * @param bar barra do gráfico.
 * @return rótulo curto exibido abaixo da barra.
 */
@Composable
private fun barLabel(period: Period, bar: ChartBar): String = when (period) {
    Period.WEEK -> Formats.weekday(bar.start)
    Period.MONTH -> stringResource(R.string.performance_week_label, bar.index + 1)
    Period.YEAR -> Formats.monthName(bar.start).take(3)
}.uppercase(BrazilLocale)

/**
 * Cartão com um número em destaque e a legenda abaixo.
 *
 * @param value número exibido.
 * @param label legenda do número.
 * @param modifier modificador aplicado ao cartão.
 */
@Composable
private fun StatTile(value: String, label: String, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .clip(TileShape)
            .background(ReadingTheme.colors.surface)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Text(text = value, style = ReadingTheme.typography.number, color = ReadingTheme.colors.text, maxLines = 1)
        Text(text = label, style = ReadingTheme.typography.caption, color = ReadingTheme.colors.text2, maxLines = 1)
    }
}

/**
 * Lista de conquistas recentes, ou uma dica quando ainda não há nenhuma.
 *
 * @param achievements conquistas das mais recentes para as mais antigas.
 * @param loaded true depois que os dados chegaram do banco.
 * @param onOpenBook abre o detalhe do livro concluído.
 * @param modifier modificador aplicado à seção.
 */
@Composable
private fun Achievements(
    achievements: List<Achievement>,
    loaded: Boolean,
    onOpenBook: (Long) -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = ReadingTheme.colors
    Column(modifier = modifier.fillMaxWidth()) {
        Text(
            text = stringResource(R.string.performance_achievements),
            style = ReadingTheme.typography.overline,
            color = colors.text3
        )
        if (loaded && achievements.isEmpty()) {
            Text(
                text = stringResource(R.string.performance_achievements_empty),
                modifier = Modifier.padding(top = 12.dp),
                style = ReadingTheme.typography.caption,
                color = colors.text3
            )
        }
        achievements.forEach { achievement ->
            when (achievement) {
                is BookFinished -> BookFinishedRow(achievement, onClick = { onOpenBook(achievement.book.id) })
                is GoalReached -> GoalReachedRow(achievement)
            }
        }
    }
}

/**
 * Linha de livro concluído, com o tempo de leitura ou o ganho sobre o recorde anterior.
 *
 * @param achievement conquista do livro.
 * @param onClick abre o detalhe do livro.
 */
@Composable
private fun BookFinishedRow(achievement: BookFinished, onClick: () -> Unit) {
    val colors = ReadingTheme.colors
    val gain = achievement.recordGain
    val value = if (gain != null) {
        pluralStringResource(R.plurals.achievement_record_gain, gain, gain)
    } else {
        pluralStringResource(R.plurals.achievement_days, achievement.days, achievement.days)
    }
    val status = stringResource(
        when {
            gain != null -> R.string.achievement_record
            achievement.onTime == true -> R.string.achievement_on_time
            achievement.onTime == false -> R.string.achievement_late
            else -> R.string.achievement_reading_time
        }
    )
    ListRow(
        title = stringResource(R.string.achievement_book_title, achievement.book.title),
        subtitle = stringResource(Labels.genre(achievement.book.genre)),
        onClick = onClick,
        leading = {
            DateLeading(day = Formats.day(achievement.date), weekday = Formats.weekday(achievement.date))
        },
        trailing = {
            TrailingValue(
                value = value,
                status = status,
                valueColor = if (gain != null || achievement.onTime == true) colors.positive else colors.text
            )
        }
    )
}

/**
 * Linha de meta mensal batida, com as páginas além da meta.
 *
 * @param achievement conquista do mês.
 */
@Composable
private fun GoalReachedRow(achievement: GoalReached) {
    val extra = (achievement.pages - achievement.goal).coerceAtLeast(0)
    val monthDate = achievement.month.atDay(1)
    ListRow(
        title = stringResource(R.string.achievement_goal_title),
        subtitle = stringResource(
            R.string.achievement_goal_subtitle,
            pluralStringResource(R.plurals.pages_count, achievement.pages, Formats.integer(achievement.pages)),
            Formats.monthName(monthDate)
        ),
        onClick = { },
        leading = {
            DateLeading(day = Formats.day(achievement.date), weekday = Formats.weekday(achievement.date))
        },
        trailing = {
            TrailingValue(
                value = pluralStringResource(R.plurals.achievement_goal_extra, extra, Formats.integer(extra)),
                status = if (achievement.record) {
                    stringResource(R.string.achievement_record)
                } else {
                    pluralStringResource(
                        R.plurals.achievement_goal_status,
                        achievement.goal,
                        Formats.integer(achievement.goal)
                    )
                },
                valueColor = ReadingTheme.colors.positive
            )
        }
    )
}

/**
 * Diálogo para definir a meta mensal de páginas.
 *
 * @param goal meta atual, usada como valor inicial.
 * @param onDismiss fecha o diálogo sem gravar.
 * @param onConfirm grava a nova meta.
 */
@Composable
private fun GoalDialog(goal: Int?, onDismiss: () -> Unit, onConfirm: (Int) -> Unit) {
    var text by rememberSaveable { mutableStateOf(goal?.toString().orEmpty()) }
    var error by rememberSaveable { mutableStateOf<String?>(null) }
    val requiredMessage = stringResource(R.string.error_goal_required)
    val zeroMessage = stringResource(R.string.error_goal_zero)
    CardDialog(
        title = stringResource(R.string.action_set_goal),
        onDismiss = onDismiss,
        message = stringResource(R.string.goal_dialog_message)
    ) {
        LargeNumberField(
            label = stringResource(R.string.goal_dialog_label),
            value = text,
            onValueChange = {
                text = it
                error = null
            },
            error = error,
            imeAction = ImeAction.Done
        )
        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            PrimaryButton(
                text = stringResource(R.string.action_save),
                onClick = {
                    val pages = text.toIntOrNull()
                    error = when {
                        pages == null -> requiredMessage
                        pages <= 0 -> zeroMessage
                        else -> null
                    }
                    if (pages != null && error == null) {
                        onConfirm(pages)
                    }
                }
            )
            PlainButton(text = stringResource(R.string.action_cancel), onClick = onDismiss)
        }
    }
}
