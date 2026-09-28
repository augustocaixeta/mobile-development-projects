package br.edu.iftm.readingmanager.ui.detail

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.LifecycleResumeEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import br.edu.iftm.readingmanager.R
import br.edu.iftm.readingmanager.data.Book
import br.edu.iftm.readingmanager.data.BookStatus
import br.edu.iftm.readingmanager.data.Note
import br.edu.iftm.readingmanager.data.NoteType
import br.edu.iftm.readingmanager.data.isRead
import br.edu.iftm.readingmanager.data.pagesLeft
import br.edu.iftm.readingmanager.data.progressPercent
import br.edu.iftm.readingmanager.ui.components.BarIconButton
import br.edu.iftm.readingmanager.ui.components.BottomActions
import br.edu.iftm.readingmanager.ui.components.CardDialog
import br.edu.iftm.readingmanager.ui.components.DetailRow
import br.edu.iftm.readingmanager.ui.components.Labels
import br.edu.iftm.readingmanager.ui.components.LargeNumberField
import br.edu.iftm.readingmanager.ui.components.Pill
import br.edu.iftm.readingmanager.ui.components.PlainButton
import br.edu.iftm.readingmanager.ui.components.PrimaryButton
import br.edu.iftm.readingmanager.ui.components.ProgressLine
import br.edu.iftm.readingmanager.ui.components.TopBar
import br.edu.iftm.readingmanager.ui.components.relativeDay
import br.edu.iftm.readingmanager.ui.theme.ReadingTheme
import br.edu.iftm.readingmanager.util.Formats
import br.edu.iftm.readingmanager.util.toLocalDateTime
import java.time.LocalDate
import java.time.LocalDateTime

/**
 * Detalhe do livro com o progresso de leitura, a situação e os dados do cadastro.
 *
 * @param onBack volta para o Início, também usado depois de excluir o livro.
 * @param onEdit abre o formulário em modo de edição.
 * @param onContinue abre a sessão de leitura do livro.
 * @param viewModel estado da tela, criado pela fábrica do app.
 */
@Composable
fun DetailScreen(
    onBack: () -> Unit,
    onEdit: () -> Unit,
    onContinue: () -> Unit,
    viewModel: DetailViewModel = viewModel(factory = DetailViewModel.Factory)
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    LifecycleResumeEffect(viewModel) {
        viewModel.refreshDate()
        onPauseOrDispose { }
    }
    LaunchedEffect(state.deleted, state.loaded, state.book) {
        if (state.deleted || (state.loaded && state.book == null)) {
            onBack()
        }
    }
    DetailContent(
        state = state,
        onBack = onBack,
        onEdit = onEdit,
        onContinue = onContinue,
        onUpdatePage = viewModel::updatePage,
        onChangeStatus = viewModel::changeStatus,
        onDeleteBook = viewModel::deleteBook,
        onSaveNote = viewModel::saveNote,
        onDeleteNote = viewModel::deleteNote
    )
}

/**
 * Conteúdo do detalhe, sem depender do ViewModel.
 *
 * @param state estado atual da tela.
 * @param onBack volta para o Início.
 * @param onEdit abre o formulário em modo de edição.
 * @param onContinue abre a sessão de leitura.
 * @param onUpdatePage grava a nova página atual.
 * @param onChangeStatus grava a nova situação do livro.
 * @param onDeleteBook exclui o livro.
 * @param onSaveNote grava a nota editada ou nova.
 * @param onDeleteNote exclui uma nota.
 */
@Composable
private fun DetailContent(
    state: DetailUiState,
    onBack: () -> Unit,
    onEdit: () -> Unit,
    onContinue: () -> Unit,
    onUpdatePage: (Int) -> Unit,
    onChangeStatus: (BookStatus) -> Unit,
    onDeleteBook: () -> Unit,
    onSaveNote: (Note?, NoteType, String) -> Unit,
    onDeleteNote: (Note) -> Unit
) {
    val book = state.book
    var editingNoteId by rememberSaveable { mutableStateOf<Long?>(null) }
    var creatingNote by rememberSaveable { mutableStateOf(false) }
    var showPageDialog by rememberSaveable { mutableStateOf(false) }
    var showDeleteDialog by rememberSaveable { mutableStateOf(false) }
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(ReadingTheme.colors.bg)
            .statusBarsPadding()
    ) {
        TopBar(
            navigationIcon = painterResource(R.drawable.ic_arrow_back),
            navigationDescription = stringResource(R.string.cd_back),
            onNavigate = onBack,
            actions = {
                if (book != null) {
                    MoreMenu(
                        onUpdatePage = { showPageDialog = true },
                        onDelete = { showDeleteDialog = true }
                    )
                }
            }
        )
        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(start = 16.dp, top = 8.dp, end = 16.dp, bottom = 24.dp)
        ) {
            if (book != null) {
                BookHeader(book = book)
                ProgressBlock(book = book, today = state.today, onClick = { showPageDialog = true })
                StatusPills(selected = book.status, onSelect = onChangeStatus)
                BookInfo(book = book)
                NotesSection(
                    notes = state.notes,
                    onAdd = { creatingNote = true },
                    onOpen = { note -> editingNoteId = note.id }
                )
            }
        }
        BottomActions {
            if (book != null && !book.isRead) {
                PrimaryButton(
                    text = stringResource(
                        if (book.status == BookStatus.WANT_TO_READ) {
                            R.string.action_start_reading
                        } else {
                            R.string.action_continue_reading
                        }
                    ),
                    onClick = onContinue
                )
            }
            PlainButton(text = stringResource(R.string.action_edit_book), onClick = onEdit)
        }
    }
    if (book != null && showPageDialog) {
        PageDialog(
            book = book,
            onDismiss = { showPageDialog = false },
            onConfirm = { page ->
                showPageDialog = false
                onUpdatePage(page)
            }
        )
    }
    val editingNote = state.notes.firstOrNull { it.id == editingNoteId }
    if (creatingNote || editingNote != null) {
        val close = {
            creatingNote = false
            editingNoteId = null
        }
        NoteDialog(
            note = editingNote,
            onDismiss = close,
            onSave = { type, text ->
                onSaveNote(editingNote, type, text)
                close()
            },
            onDelete = {
                if (editingNote != null) {
                    onDeleteNote(editingNote)
                }
                close()
            }
        )
    }
    if (showDeleteDialog) {
        DeleteBookDialog(
            onDismiss = { showDeleteDialog = false },
            onConfirm = {
                showDeleteDialog = false
                onDeleteBook()
            }
        )
    }
}

/**
 * Ícone de mais opções da barra superior, com o menu de atualizar a página e excluir o livro.
 *
 * @param onUpdatePage abre o diálogo de página atual.
 * @param onDelete abre a confirmação de exclusão.
 */
@Composable
private fun MoreMenu(onUpdatePage: () -> Unit, onDelete: () -> Unit) {
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
                text = { MenuText(stringResource(R.string.action_update_page)) },
                onClick = {
                    expanded = false
                    onUpdatePage()
                }
            )
            DropdownMenuItem(
                text = { MenuText(stringResource(R.string.action_delete_book)) },
                onClick = {
                    expanded = false
                    onDelete()
                }
            )
        }
    }
}

/**
 * Texto de um item do menu de opções.
 *
 * @param text rótulo do item.
 */
@Composable
private fun MenuText(text: String) {
    Text(text = text, style = ReadingTheme.typography.body, color = ReadingTheme.colors.text)
}

/**
 * Situação e gênero em mono minúsculo, seguidos do título do livro.
 *
 * @param book livro exibido.
 */
@Composable
private fun BookHeader(book: Book) {
    val colors = ReadingTheme.colors
    Text(
        text = stringResource(
            R.string.pair_format,
            stringResource(Labels.status(book.status)).lowercase(),
            stringResource(Labels.genre(book.genre)).lowercase()
        ),
        style = ReadingTheme.typography.overline,
        color = colors.text3
    )
    Text(
        text = book.title,
        modifier = Modifier.padding(top = 6.dp),
        style = ReadingTheme.typography.title,
        color = colors.text
    )
}

/**
 * Bloco de progresso com a página atual em destaque, o que falta ler e a barra percentual.
 * Tocar no bloco abre a atualização da página.
 *
 * @param book livro exibido.
 * @param today data de referência para o prazo relativo.
 * @param onClick abre o diálogo de página atual.
 */
@Composable
private fun ProgressBlock(book: Book, today: LocalDate, onClick: () -> Unit) {
    val colors = ReadingTheme.colors
    val typography = ReadingTheme.typography
    val deadline = book.deadline?.toLocalDateTime()
    val overdue = !book.isRead && deadline != null && deadline.isBefore(LocalDateTime.now())
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 20.dp)
            .clickable(role = Role.Button, onClickLabel = stringResource(R.string.action_update_page), onClick = onClick),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Text(
            text = stringResource(R.string.detail_progress_label),
            style = typography.caption,
            color = colors.text2
        )
        Text(
            text = pluralStringResource(R.plurals.pages_count, book.currentPage, Formats.integer(book.currentPage)),
            style = typography.display,
            color = colors.text,
            maxLines = 1
        )
        Text(
            text = progressCaption(book = book, today = today, deadline = deadline?.toLocalDate()),
            style = typography.caption,
            color = when {
                overdue -> colors.accent
                book.status == BookStatus.WANT_TO_READ -> colors.text2
                else -> colors.positive
            }
        )
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 10.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            ProgressLine(progress = book.progressPercent / 100f, modifier = Modifier.weight(1f))
            Text(
                text = stringResource(R.string.percent_format, book.progressPercent),
                style = typography.overline,
                color = colors.text2
            )
        }
    }
}

/**
 * Monta a frase abaixo da página atual: o que falta ler e o prazo, ou a data de término.
 *
 * @param book livro exibido.
 * @param today data de referência.
 * @param deadline data do prazo, ou null quando não há prazo.
 * @return frase como faltam 130 págs · prazo em 3 dias.
 */
@Composable
private fun progressCaption(book: Book, today: LocalDate, deadline: LocalDate?): String {
    if (book.isRead) {
        val finished = book.finishedAt?.toLocalDateTime()?.toLocalDate() ?: today
        return stringResource(R.string.detail_finished_on, Formats.fullDate(finished))
    }
    val left = pluralStringResource(R.plurals.detail_pages_left, book.pagesLeft, Formats.integer(book.pagesLeft))
    if (deadline == null) {
        return left
    }
    return stringResource(
        R.string.pair_format,
        left,
        stringResource(R.string.detail_deadline_relative, relativeDay(deadline, today))
    )
}

/**
 * Pílulas para trocar a situação do livro entre quero ler, lendo e lido.
 *
 * @param selected situação atual do livro.
 * @param onSelect recebe a situação tocada.
 */
@Composable
private fun StatusPills(selected: BookStatus, onSelect: (BookStatus) -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 20.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        BookStatus.entries.forEach { status ->
            Pill(
                text = stringResource(Labels.status(status)),
                selected = status == selected,
                onClick = { onSelect(status) }
            )
        }
    }
}

/**
 * Linhas com autor, categoria, início, prazo e término do livro.
 *
 * @param book livro exibido.
 */
@Composable
private fun BookInfo(book: Book) {
    val colors = ReadingTheme.colors
    Column(modifier = Modifier.padding(top = 24.dp)) {
        DetailRow(label = stringResource(R.string.detail_author), value = book.author)
        DetailRow(label = stringResource(R.string.detail_category), value = stringResource(Labels.genre(book.genre)))
        DetailRow(
            label = stringResource(R.string.detail_started),
            value = book.startedAt?.let { Formats.fullDate(it.toLocalDateTime().toLocalDate()) }
                ?: stringResource(R.string.detail_not_started),
            valueColor = if (book.startedAt != null) colors.text else colors.text3
        )
        DetailRow(
            label = stringResource(R.string.detail_deadline),
            value = book.deadline?.let { Formats.fullDate(it.toLocalDateTime().toLocalDate()) }
                ?: stringResource(R.string.no_deadline),
            valueColor = if (book.deadline != null) colors.text else colors.text3
        )
        val finished = book.finishedAt
        if (book.isRead && finished != null) {
            DetailRow(
                label = stringResource(R.string.detail_finished),
                value = Formats.fullDate(finished.toLocalDateTime().toLocalDate()),
                valueColor = colors.positive
            )
        }
    }
}

/**
 * Diálogo para atualizar a página atual, com validação do número digitado.
 *
 * @param book livro que terá a página atualizada.
 * @param onDismiss fecha o diálogo sem salvar.
 * @param onConfirm recebe a página já validada.
 */
@Composable
private fun PageDialog(book: Book, onDismiss: () -> Unit, onConfirm: (Int) -> Unit) {
    var text by rememberSaveable { mutableStateOf(book.currentPage.toString()) }
    var error by rememberSaveable { mutableStateOf<String?>(null) }
    val requiredMessage = stringResource(R.string.error_page_required)
    val aboveMessage = stringResource(R.string.error_page_above_total, Formats.integer(book.totalPages))
    CardDialog(
        title = stringResource(R.string.action_update_page),
        onDismiss = onDismiss,
        message = pluralStringResource(R.plurals.page_dialog_total, book.totalPages, Formats.integer(book.totalPages))
    ) {
        LargeNumberField(
            label = stringResource(R.string.page_dialog_label),
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
                    val page = text.toIntOrNull()
                    error = when {
                        page == null -> requiredMessage
                        page > book.totalPages -> aboveMessage
                        else -> null
                    }
                    if (page != null && error == null) {
                        onConfirm(page)
                    }
                }
            )
            PlainButton(text = stringResource(R.string.action_cancel), onClick = onDismiss)
        }
    }
}

/**
 * Confirmação antes de excluir o livro, avisando que as notas e sessões também serão apagadas.
 *
 * @param onDismiss fecha o diálogo sem excluir.
 * @param onConfirm exclui o livro.
 */
@Composable
private fun DeleteBookDialog(onDismiss: () -> Unit, onConfirm: () -> Unit) {
    CardDialog(
        title = stringResource(R.string.delete_book_title),
        onDismiss = onDismiss,
        message = stringResource(R.string.delete_book_message)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            PrimaryButton(text = stringResource(R.string.action_delete), onClick = onConfirm)
            PlainButton(text = stringResource(R.string.action_cancel), onClick = onDismiss)
        }
    }
}
