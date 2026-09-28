package br.edu.iftm.readingmanager.ui.session

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import br.edu.iftm.readingmanager.R
import br.edu.iftm.readingmanager.data.Book
import br.edu.iftm.readingmanager.ui.components.BarIconButton
import br.edu.iftm.readingmanager.ui.components.BottomActions
import br.edu.iftm.readingmanager.ui.components.CardDialog
import br.edu.iftm.readingmanager.ui.components.LargeNumberField
import br.edu.iftm.readingmanager.ui.components.PlainButton
import br.edu.iftm.readingmanager.ui.components.PrimaryButton
import br.edu.iftm.readingmanager.ui.components.bottomBorder
import br.edu.iftm.readingmanager.ui.theme.ReadingTheme
import br.edu.iftm.readingmanager.util.Formats

private val CardShape = RoundedCornerShape(20.dp)
private val ButtonShape = RoundedCornerShape(16.dp)

/**
 * Sessão ativa de leitura de um livro, com cronômetro, marcações de página e o que vem a seguir.
 *
 * @param onClose fecha a sessão e volta para o detalhe.
 * @param viewModel estado da tela, criado pela fábrica do app.
 */
@Composable
fun SessionScreen(
    onClose: () -> Unit,
    viewModel: SessionViewModel = viewModel(factory = SessionViewModel.Factory)
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    LaunchedEffect(state.finished, state.loaded, state.book) {
        if (state.finished || (state.loaded && state.book == null)) {
            onClose()
        }
    }
    SessionContent(
        state = state,
        onClose = onClose,
        onTogglePause = viewModel::togglePause,
        onToggleMark = viewModel::toggleMark,
        onAddMark = viewModel::addMark,
        onFinish = viewModel::finish
    )
}

/**
 * Conteúdo da sessão, sem depender do ViewModel.
 *
 * @param state estado atual da tela.
 * @param onClose sai da sessão sem gravar.
 * @param onTogglePause pausa ou retoma o cronômetro.
 * @param onToggleMark marca ou desmarca uma página.
 * @param onAddMark inclui uma nova marcação.
 * @param onFinish grava a página final e encerra a sessão.
 */
@Composable
private fun SessionContent(
    state: SessionUiState,
    onClose: () -> Unit,
    onTogglePause: () -> Unit,
    onToggleMark: (Int) -> Unit,
    onAddMark: (Int) -> Unit,
    onFinish: (Int) -> Unit
) {
    val colors = ReadingTheme.colors
    val book = state.book
    var showDiscard by rememberSaveable { mutableStateOf(false) }
    var showFinish by rememberSaveable { mutableStateOf(false) }
    var showAddMark by rememberSaveable { mutableStateOf(false) }
    val hasProgress = state.elapsedSeconds > 0 || state.draft.done.isNotEmpty()
    val requestClose: () -> Unit = {
        if (hasProgress) {
            showDiscard = true
        } else {
            onClose()
        }
    }
    BackHandler(onBack = requestClose)
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.bg)
            .statusBarsPadding()
    ) {
        SessionTopBar(
            elapsedSeconds = state.elapsedSeconds,
            running = state.draft.isRunning,
            onClose = requestClose
        )
        if (book != null) {
            val reached = state.draft.reachedPage(state.startPage)
            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp)
            ) {
                BookHeading(book = book, reached = reached)
                MarksCard(
                    marks = state.draft.marks,
                    reached = reached,
                    onToggle = onToggleMark,
                    onAdd = { showAddMark = true },
                    modifier = Modifier.padding(top = 24.dp)
                )
                UpNext(
                    book = book,
                    reached = reached,
                    nextMark = state.draft.nextMark(state.startPage),
                    modifier = Modifier.padding(top = 24.dp, bottom = 8.dp)
                )
            }
            BottomActions(modifier = Modifier.padding(top = 8.dp)) {
                PrimaryButton(text = stringResource(R.string.action_finish_session), onClick = { showFinish = true })
                PlainButton(
                    text = stringResource(
                        if (state.draft.isRunning) R.string.action_pause_session else R.string.action_resume_session
                    ),
                    onClick = onTogglePause
                )
            }
            if (showFinish) {
                FinishDialog(
                    book = book,
                    startPage = state.startPage,
                    reached = reached,
                    elapsedSeconds = state.elapsedSeconds,
                    onDismiss = { showFinish = false },
                    onConfirm = { page ->
                        showFinish = false
                        onFinish(page)
                    }
                )
            }
            if (showAddMark) {
                AddMarkDialog(
                    book = book,
                    reached = reached,
                    marks = state.draft.marks,
                    onDismiss = { showAddMark = false },
                    onConfirm = { page ->
                        showAddMark = false
                        onAddMark(page)
                    }
                )
            }
        }
    }
    if (showDiscard) {
        DiscardDialog(
            onDismiss = { showDiscard = false },
            onConfirm = {
                showDiscard = false
                onClose()
            }
        )
    }
}

/**
 * Barra do topo com o botão de fechar e o cronômetro da sessão.
 *
 * @param elapsedSeconds tempo lido até agora.
 * @param running true enquanto o cronômetro está rodando.
 * @param onClose pede para sair da sessão.
 */
@Composable
private fun SessionTopBar(elapsedSeconds: Long, running: Boolean, onClose: () -> Unit) {
    val colors = ReadingTheme.colors
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 16.dp, top = 12.dp, end = 16.dp)
            .height(40.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        BarIconButton(
            painter = painterResource(R.drawable.ic_close),
            contentDescription = stringResource(R.string.cd_close),
            onClick = onClose,
            size = 40.dp,
            iconSize = 22.dp
        )
        Text(
            text = sessionClock(elapsedSeconds),
            style = ReadingTheme.typography.number.copy(fontSize = 16.sp, lineHeight = 20.sp),
            color = if (running) colors.positive else colors.text3
        )
    }
}

/**
 * Título do livro e a página alcançada na sessão.
 *
 * @param book livro da sessão.
 * @param reached página alcançada até agora.
 */
@Composable
private fun BookHeading(book: Book, reached: Int) {
    val colors = ReadingTheme.colors
    val typography = ReadingTheme.typography
    Column(
        modifier = Modifier.padding(top = 8.dp),
        verticalArrangement = Arrangement.spacedBy(2.dp)
    ) {
        Text(
            text = book.title,
            style = typography.title.copy(fontSize = 18.sp, lineHeight = 24.sp),
            color = colors.text
        )
        Text(
            text = stringResource(
                R.string.session_page_of,
                Formats.integer(reached),
                Formats.integer(book.totalPages)
            ),
            style = typography.caption,
            color = colors.text2
        )
    }
}

/**
 * Cartão com as marcações de página da sessão. Tocar numa marcação indica que ela foi alcançada.
 *
 * @param marks páginas marcadas, em ordem crescente.
 * @param reached página alcançada até agora.
 * @param onToggle marca ou desmarca a página tocada.
 * @param onAdd abre o diálogo de nova marcação.
 * @param modifier modificador aplicado ao cartão.
 */
@Composable
private fun MarksCard(
    marks: List<Int>,
    reached: Int,
    onToggle: (Int) -> Unit,
    onAdd: () -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = ReadingTheme.colors
    val typography = ReadingTheme.typography
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(CardShape)
            .background(colors.surface)
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = stringResource(R.string.session_marks_title),
            style = typography.bodyMedium.copy(fontSize = 17.sp, lineHeight = 22.sp),
            color = colors.text
        )
        if (marks.isNotEmpty()) {
            Column(modifier = Modifier.fillMaxWidth()) {
                marks.forEach { page ->
                    MarkRow(page = page, checked = page <= reached, onClick = { onToggle(page) })
                }
            }
        }
        Box(
            modifier = Modifier
                .height(52.dp)
                .clip(ButtonShape)
                .clickable(role = Role.Button, onClick = onAdd)
                .padding(horizontal = 24.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(text = stringResource(R.string.action_add_mark), style = typography.bodyMedium, color = colors.text2)
        }
    }
}

/**
 * Linha de uma marcação, com o círculo verde quando a página já foi alcançada.
 *
 * @param page página da marcação.
 * @param checked true quando a página já foi alcançada.
 * @param onClick marca ou desmarca a página.
 */
@Composable
private fun MarkRow(page: Int, checked: Boolean, onClick: () -> Unit) {
    val colors = ReadingTheme.colors
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .bottomBorder(colors.line)
            .clickable(role = Role.Checkbox, onClick = onClick)
            .padding(vertical = 10.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = stringResource(R.string.session_mark_page, Formats.integer(page)),
            style = ReadingTheme.typography.body,
            color = colors.text
        )
        Icon(
            painter = painterResource(if (checked) R.drawable.ic_check_circle else R.drawable.ic_radio_button_unchecked),
            contentDescription = stringResource(if (checked) R.string.cd_mark_done else R.string.cd_mark_pending),
            modifier = Modifier.size(22.dp),
            tint = if (checked) colors.positive else colors.text3
        )
    }
}

/**
 * Seção "a seguir", com as páginas até a próxima marcação e até o fim do livro.
 *
 * @param book livro da sessão.
 * @param reached página alcançada até agora.
 * @param nextMark próxima marcação pendente, quando houver.
 * @param modifier modificador aplicado à seção.
 */
@Composable
private fun UpNext(book: Book, reached: Int, nextMark: Int?, modifier: Modifier = Modifier) {
    val colors = ReadingTheme.colors
    Column(modifier = modifier.fillMaxWidth()) {
        Text(text = stringResource(R.string.session_up_next), style = ReadingTheme.typography.overline, color = colors.text3)
        if (nextMark != null) {
            UpNextRow(
                label = stringResource(R.string.session_next_mark, Formats.integer(nextMark)),
                pages = nextMark - reached
            )
        }
        UpNextRow(
            label = stringResource(R.string.session_book_end),
            pages = (book.totalPages - reached).coerceAtLeast(0)
        )
    }
}

/**
 * Linha da seção "a seguir", com o destino e a quantidade de páginas até ele.
 *
 * @param label destino da leitura.
 * @param pages páginas que faltam até o destino.
 */
@Composable
private fun UpNextRow(label: String, pages: Int) {
    val colors = ReadingTheme.colors
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 10.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            modifier = Modifier.weight(1f),
            style = ReadingTheme.typography.caption.copy(fontSize = 14.sp),
            color = colors.text3,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
        Text(
            text = pluralStringResource(R.plurals.session_pages_short, pages, Formats.integer(pages)),
            style = ReadingTheme.typography.monoSmall,
            color = colors.text3
        )
    }
}

/**
 * Diálogo para encerrar a sessão informando a página em que o leitor parou.
 *
 * @param book livro da sessão.
 * @param startPage página em que a sessão começou.
 * @param reached página alcançada, usada como valor inicial.
 * @param elapsedSeconds tempo lido, exibido na mensagem.
 * @param onDismiss fecha o diálogo e continua a sessão.
 * @param onConfirm grava a página final.
 */
@Composable
private fun FinishDialog(
    book: Book,
    startPage: Int,
    reached: Int,
    elapsedSeconds: Long,
    onDismiss: () -> Unit,
    onConfirm: (Int) -> Unit
) {
    var text by rememberSaveable { mutableStateOf(reached.toString()) }
    var error by rememberSaveable { mutableStateOf<String?>(null) }
    val requiredMessage = stringResource(R.string.error_page_required)
    val aboveMessage = stringResource(R.string.error_page_above_total, Formats.integer(book.totalPages))
    val belowMessage = stringResource(R.string.error_page_below_start, Formats.integer(startPage))
    CardDialog(
        title = stringResource(R.string.action_finish_session),
        onDismiss = onDismiss,
        message = stringResource(R.string.finish_dialog_message, sessionClock(elapsedSeconds))
    ) {
        LargeNumberField(
            label = stringResource(R.string.finish_dialog_label),
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
                        page < startPage -> belowMessage
                        else -> null
                    }
                    if (page != null && error == null) {
                        onConfirm(page)
                    }
                }
            )
            PlainButton(text = stringResource(R.string.action_keep_reading), onClick = onDismiss)
        }
    }
}

/**
 * Diálogo de nova marcação de página.
 *
 * @param book livro da sessão.
 * @param reached página alcançada, que a marcação precisa ultrapassar.
 * @param marks marcações já existentes, para evitar repetição.
 * @param onDismiss fecha o diálogo sem marcar.
 * @param onConfirm inclui a marcação.
 */
@Composable
private fun AddMarkDialog(
    book: Book,
    reached: Int,
    marks: List<Int>,
    onDismiss: () -> Unit,
    onConfirm: (Int) -> Unit
) {
    var text by rememberSaveable { mutableStateOf("") }
    var error by rememberSaveable { mutableStateOf<String?>(null) }
    val requiredMessage = stringResource(R.string.error_page_required)
    val aboveMessage = stringResource(R.string.error_page_above_total, Formats.integer(book.totalPages))
    val belowMessage = stringResource(R.string.error_mark_below_reached, Formats.integer(reached))
    val duplicateMessage = stringResource(R.string.error_mark_duplicate)
    CardDialog(
        title = stringResource(R.string.mark_dialog_title),
        onDismiss = onDismiss,
        message = pluralStringResource(R.plurals.page_dialog_total, book.totalPages, Formats.integer(book.totalPages))
    ) {
        LargeNumberField(
            label = stringResource(R.string.mark_dialog_label),
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
                        page <= reached -> belowMessage
                        page in marks -> duplicateMessage
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
 * Confirmação antes de sair de uma sessão que ainda não foi gravada.
 *
 * @param onDismiss fecha o diálogo e continua a sessão.
 * @param onConfirm sai sem gravar.
 */
@Composable
private fun DiscardDialog(onDismiss: () -> Unit, onConfirm: () -> Unit) {
    CardDialog(
        title = stringResource(R.string.discard_dialog_title),
        onDismiss = onDismiss,
        message = stringResource(R.string.discard_dialog_message)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            PrimaryButton(text = stringResource(R.string.action_discard), onClick = onConfirm)
            PlainButton(text = stringResource(R.string.action_keep_reading), onClick = onDismiss)
        }
    }
}
