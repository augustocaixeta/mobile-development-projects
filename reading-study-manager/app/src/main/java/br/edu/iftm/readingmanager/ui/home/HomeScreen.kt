package br.edu.iftm.readingmanager.ui.home

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.LifecycleResumeEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import br.edu.iftm.readingmanager.R
import br.edu.iftm.readingmanager.data.Book
import br.edu.iftm.readingmanager.data.BookStatus
import br.edu.iftm.readingmanager.data.Genre
import br.edu.iftm.readingmanager.data.isRead
import br.edu.iftm.readingmanager.data.progressPercent
import br.edu.iftm.readingmanager.ui.components.AddFab
import br.edu.iftm.readingmanager.ui.components.BarIconButton
import br.edu.iftm.readingmanager.ui.components.FilterTabs
import br.edu.iftm.readingmanager.ui.components.Labels
import br.edu.iftm.readingmanager.ui.components.ListRow
import br.edu.iftm.readingmanager.ui.components.Pill
import br.edu.iftm.readingmanager.ui.components.ProgressLine
import br.edu.iftm.readingmanager.ui.components.SectionHeader
import br.edu.iftm.readingmanager.ui.components.StatusPill
import br.edu.iftm.readingmanager.ui.components.TrailingValue
import br.edu.iftm.readingmanager.ui.components.relativeDay
import br.edu.iftm.readingmanager.ui.theme.ReadingTheme
import br.edu.iftm.readingmanager.util.Formats
import br.edu.iftm.readingmanager.util.toLocalDateTime
import java.time.LocalDate
import java.time.LocalDateTime

/**
 * Tela inicial com o resumo do mês e o catálogo de livros filtrado por situação e gênero.
 *
 * @param onOpenBook abre o detalhe do livro tocado.
 * @param onAdd abre o formulário de novo registro.
 * @param onOpenPerformance abre a tela de desempenho.
 * @param viewModel estado da tela, criado pela fábrica do app.
 */
@Composable
fun HomeScreen(
    onOpenBook: (Long) -> Unit,
    onAdd: () -> Unit,
    onOpenPerformance: () -> Unit,
    viewModel: HomeViewModel = viewModel(factory = HomeViewModel.Factory)
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    LifecycleResumeEffect(viewModel) {
        viewModel.refreshDate()
        onPauseOrDispose { }
    }
    HomeContent(
        state = state,
        onSelectStatus = viewModel::selectStatus,
        onSelectGenre = viewModel::selectGenre,
        onOpenBook = onOpenBook,
        onAdd = onAdd,
        onOpenPerformance = onOpenPerformance
    )
}

/**
 * Conteúdo da tela inicial, sem depender do ViewModel.
 *
 * @param state estado atual da tela.
 * @param onSelectStatus troca a aba de situação.
 * @param onSelectGenre troca o gênero filtrado.
 * @param onOpenBook abre o detalhe do livro tocado.
 * @param onAdd abre o formulário de novo registro.
 * @param onOpenPerformance abre a tela de desempenho.
 */
@Composable
private fun HomeContent(
    state: HomeUiState,
    onSelectStatus: (StatusFilter) -> Unit,
    onSelectGenre: (Genre?) -> Unit,
    onOpenBook: (Long) -> Unit,
    onAdd: () -> Unit,
    onOpenPerformance: () -> Unit
) {
    val bottomInset = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()
    val tabs = listOf(R.string.tab_all, R.string.tab_reading, R.string.tab_read, R.string.tab_want_to_read)
        .map { stringResource(it) }
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(ReadingTheme.colors.bg)
            .statusBarsPadding()
    ) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(top = 12.dp, bottom = bottomInset + 96.dp)
        ) {
            item(key = "header") {
                Header(today = state.today, onOpenPerformance = onOpenPerformance)
            }
            item(key = "summary") {
                SummaryBlock(summary = state.summary, onClick = onOpenPerformance)
            }
            item(key = "tabs") {
                FilterTabs(
                    labels = tabs,
                    selectedIndex = state.filters.status.ordinal,
                    onSelect = { index -> onSelectStatus(StatusFilter.entries[index]) },
                    modifier = Modifier
                        .padding(start = 16.dp, top = 28.dp, end = 16.dp)
                        .fillMaxWidth()
                )
            }
            item(key = "genres") {
                GenrePills(selected = state.filters.genre, onSelect = onSelectGenre)
            }
            if (state.loaded && state.items.isEmpty()) {
                item(key = "empty") {
                    EmptyState(filtered = !state.filters.isDefault)
                }
            }
            items(
                items = state.items,
                key = { item ->
                    when (item) {
                        is HomeItem.Section -> "section-${item.status}"
                        is HomeItem.Entry -> item.book.id
                    }
                },
                contentType = { item -> item::class }
            ) { item ->
                when (item) {
                    is HomeItem.Section -> SectionHeader(
                        text = stringResource(Labels.section(item.status)),
                        modifier = Modifier.padding(horizontal = 16.dp)
                    )
                    is HomeItem.Entry -> BookRow(
                        book = item.book,
                        today = state.today,
                        onClick = { onOpenBook(item.book.id) }
                    )
                }
            }
        }
        AddFab(
            contentDescription = stringResource(R.string.cd_add),
            onClick = onAdd,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .navigationBarsPadding()
                .padding(end = 20.dp)
        )
    }
}

/**
 * Cabeçalho com a data de hoje e o atalho para o desempenho.
 *
 * @param today data exibida.
 * @param onOpenPerformance abre a tela de desempenho.
 */
@Composable
private fun Header(today: LocalDate, onOpenPerformance: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(40.dp)
            .padding(horizontal = 16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = Formats.header(today),
            style = ReadingTheme.typography.overline,
            color = ReadingTheme.colors.text3
        )
        BarIconButton(
            painter = painterResource(R.drawable.ic_bar_chart),
            contentDescription = stringResource(R.string.performance_title),
            onClick = onOpenPerformance,
            size = 40.dp,
            iconSize = 22.dp,
            tint = ReadingTheme.colors.text2
        )
    }
}

/**
 * Resumo do mês: páginas dos livros terminados, quantidade de livros e o avanço da meta mensal.
 *
 * @param summary números do mês atual.
 * @param onClick abre a tela de desempenho.
 */
@Composable
private fun SummaryBlock(summary: MonthSummary, onClick: () -> Unit) {
    val colors = ReadingTheme.colors
    val typography = ReadingTheme.typography
    val percent = summary.goalPercent
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(role = Role.Button, onClick = onClick)
            .padding(start = 16.dp, top = 8.dp, end = 16.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Text(
            text = stringResource(R.string.summary_read_in, Formats.monthName(summary.month.atDay(1))),
            style = typography.caption,
            color = colors.text2
        )
        Text(
            text = pluralStringResource(
                R.plurals.pages_count,
                summary.pagesRead,
                Formats.integer(summary.pagesRead)
            ),
            style = typography.display,
            color = colors.text,
            maxLines = 1
        )
        Row(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = pluralStringResource(R.plurals.summary_books_count, summary.booksRead, summary.booksRead),
                style = typography.value,
                color = colors.positive
            )
            Text(
                text = pluralStringResource(R.plurals.summary_books_suffix, summary.booksRead),
                style = typography.caption,
                color = colors.text3
            )
        }
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 14.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            ProgressLine(progress = (percent ?: 0) / 100f, modifier = Modifier.weight(1f))
            Text(
                text = if (percent != null) {
                    stringResource(R.string.summary_goal, percent)
                } else {
                    stringResource(R.string.summary_no_goal)
                },
                style = typography.overline,
                color = colors.text2
            )
        }
    }
}

/**
 * Pílulas roláveis para filtrar o catálogo por gênero literário.
 *
 * @param selected gênero filtrado, ou null para todos.
 * @param onSelect recebe o gênero tocado, ou null para todos.
 */
@Composable
private fun GenrePills(selected: Genre?, onSelect: (Genre?) -> Unit) {
    LazyRow(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 16.dp),
        contentPadding = PaddingValues(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        item(key = "all") {
            Pill(
                text = stringResource(R.string.genre_all),
                selected = selected == null,
                onClick = { onSelect(null) }
            )
        }
        items(items = Genre.entries, key = { it.name }) { genre ->
            Pill(
                text = stringResource(Labels.genre(genre)),
                selected = selected == genre,
                onClick = { onSelect(genre) }
            )
        }
    }
}

/**
 * Linha de um livro no catálogo. A cor de destaque aparece quando o prazo já passou.
 *
 * @param book livro exibido.
 * @param today data de referência para o prazo relativo.
 * @param onClick abre o detalhe do livro.
 */
@Composable
private fun BookRow(book: Book, today: LocalDate, onClick: () -> Unit) {
    val colors = ReadingTheme.colors
    val deadline = book.deadline?.toLocalDateTime()
    val overdue = !book.isRead && deadline != null && deadline.isBefore(LocalDateTime.now())
    ListRow(
        title = book.title,
        subtitle = stringResource(R.string.pair_format, book.author, stringResource(Labels.genre(book.genre))),
        onClick = onClick,
        modifier = Modifier.padding(horizontal = 16.dp),
        leading = {
            Icon(
                painter = painterResource(Labels.icon(book.icon)),
                contentDescription = null,
                modifier = Modifier.size(24.dp),
                tint = if (overdue) colors.accent else colors.text
            )
            Text(
                text = stringResource(R.string.percent_format, book.progressPercent),
                modifier = Modifier.width(32.dp),
                style = ReadingTheme.typography.overline,
                color = colors.text3,
                maxLines = 1
            )
        },
        trailing = {
            if (book.status == BookStatus.READ) {
                StatusPill(
                    text = stringResource(R.string.status_read),
                    icon = painterResource(R.drawable.ic_check)
                )
            } else {
                TrailingValue(
                    value = if (book.status == BookStatus.READING) {
                        stringResource(
                            R.string.progress_format,
                            Formats.integer(book.currentPage),
                            Formats.integer(book.totalPages)
                        )
                    } else {
                        pluralStringResource(R.plurals.pages_count, book.totalPages, Formats.integer(book.totalPages))
                    },
                    status = deadline?.let { relativeDay(it.toLocalDate(), today) }
                        ?: stringResource(R.string.no_deadline),
                    valueColor = if (book.status == BookStatus.READING) colors.text else colors.text2,
                    statusColor = if (overdue) colors.accent else colors.text3
                )
            }
        }
    )
}

/**
 * Mensagem exibida quando a lista está vazia.
 *
 * @param filtered true quando há filtro ativo, e false quando o catálogo ainda não tem livros.
 */
@Composable
private fun EmptyState(filtered: Boolean) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 16.dp, top = 32.dp, end = 16.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Text(
            text = stringResource(if (filtered) R.string.empty_filtered_title else R.string.empty_title),
            style = ReadingTheme.typography.bodyMedium,
            color = ReadingTheme.colors.text
        )
        Text(
            text = stringResource(if (filtered) R.string.empty_filtered_hint else R.string.empty_hint),
            style = ReadingTheme.typography.caption,
            color = ReadingTheme.colors.text2
        )
    }
}
