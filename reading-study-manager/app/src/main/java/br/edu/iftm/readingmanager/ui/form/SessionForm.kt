package br.edu.iftm.readingmanager.ui.form

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
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
import br.edu.iftm.readingmanager.data.Book
import br.edu.iftm.readingmanager.data.BookRepository
import br.edu.iftm.readingmanager.data.BookStatus
import br.edu.iftm.readingmanager.data.Session
import br.edu.iftm.readingmanager.data.SessionRepository
import br.edu.iftm.readingmanager.data.isRead
import br.edu.iftm.readingmanager.data.pagesLeft
import br.edu.iftm.readingmanager.data.withCurrentPage
import br.edu.iftm.readingmanager.ui.components.BottomActions
import br.edu.iftm.readingmanager.ui.components.CardDialog
import br.edu.iftm.readingmanager.ui.components.FieldLabel
import br.edu.iftm.readingmanager.ui.components.Labels
import br.edu.iftm.readingmanager.ui.components.LargeNumberField
import br.edu.iftm.readingmanager.ui.components.Pill
import br.edu.iftm.readingmanager.ui.components.PickerField
import br.edu.iftm.readingmanager.ui.components.PlainButton
import br.edu.iftm.readingmanager.ui.components.PrimaryButton
import br.edu.iftm.readingmanager.ui.components.bottomBorder
import br.edu.iftm.readingmanager.ui.theme.ReadingTheme
import br.edu.iftm.readingmanager.util.Formats
import br.edu.iftm.readingmanager.util.toEpochMillis
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

private val DurationOptions = listOf(15, 30, 45, 60)
private const val DEFAULT_DURATION = 30
private const val MINUTES_IN_HOUR = 60

enum class SessionFormError {
    BOOK_REQUIRED,
    PAGES_REQUIRED,
    PAGES_ABOVE_LEFT,
    FUTURE_DATE
}

data class SessionFormState(
    val books: List<Book> = emptyList(),
    val book: Book? = null,
    val pages: String = "",
    val date: LocalDate = LocalDate.now(),
    val time: LocalTime = LocalTime.now().withSecond(0).withNano(0),
    val durationMinutes: Int = DEFAULT_DURATION,
    val errors: Set<SessionFormError> = emptySet(),
    val loaded: Boolean = false,
    val saved: Boolean = false
)

private data class SessionFormDraft(
    val bookId: Long? = null,
    val pages: String = "",
    val date: LocalDate,
    val time: LocalTime,
    val durationMinutes: Int = DEFAULT_DURATION,
    val errors: Set<SessionFormError> = emptySet(),
    val saved: Boolean = false
)

class SessionFormViewModel(
    private val books: BookRepository,
    private val sessions: SessionRepository
) : ViewModel() {

    private val draft = MutableStateFlow(initialDraft())

    private val openBooks = books.observeFiltered(null, null).map { list ->
        list.filter { !it.isRead }.sortedBy { if (it.status == BookStatus.READING) 0 else 1 }
    }

    val state: StateFlow<SessionFormState> =
        combine(openBooks, draft) { list, current ->
            SessionFormState(
                books = list,
                book = list.firstOrNull { it.id == current.bookId } ?: list.firstOrNull(),
                pages = current.pages,
                date = current.date,
                time = current.time,
                durationMinutes = current.durationMinutes,
                errors = current.errors,
                loaded = true,
                saved = current.saved
            )
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), SessionFormState())

    /**
     * Escolhe o livro da sessão.
     *
     * @param book livro tocado na lista.
     */
    fun selectBook(book: Book) {
        draft.value = draft.value.copy(bookId = book.id, errors = draft.value.errors - SessionFormError.BOOK_REQUIRED)
    }

    /**
     * Atualiza as páginas lidas digitadas.
     *
     * @param text texto já filtrado pelo campo numérico.
     */
    fun changePages(text: String) {
        draft.value = draft.value.copy(
            pages = text,
            errors = draft.value.errors - SessionFormError.PAGES_REQUIRED - SessionFormError.PAGES_ABOVE_LEFT
        )
    }

    /**
     * Atualiza o dia da sessão.
     *
     * @param date dia escolhido no calendário.
     */
    fun changeDate(date: LocalDate) {
        draft.value = draft.value.copy(date = date, errors = draft.value.errors - SessionFormError.FUTURE_DATE)
    }

    /**
     * Atualiza o horário de início da sessão.
     *
     * @param time horário escolhido no relógio.
     */
    fun changeTime(time: LocalTime) {
        draft.value = draft.value.copy(time = time, errors = draft.value.errors - SessionFormError.FUTURE_DATE)
    }

    /**
     * Atualiza a duração da sessão.
     *
     * @param minutes duração escolhida nas pílulas.
     */
    fun changeDuration(minutes: Int) {
        draft.value = draft.value.copy(durationMinutes = minutes)
    }

    /**
     * Valida os campos e grava a sessão, avançando a página atual do livro.
     * Chegar à última página marca o livro como lido.
     */
    fun save() {
        val current = state.value
        val book = current.book
        val pages = current.pages.toIntOrNull()
        val start = LocalDateTime.of(current.date, current.time)
        val errors = buildSet {
            if (book == null) {
                add(SessionFormError.BOOK_REQUIRED)
            }
            if (pages == null || pages <= 0) {
                add(SessionFormError.PAGES_REQUIRED)
            } else if (book != null && pages > book.pagesLeft) {
                add(SessionFormError.PAGES_ABOVE_LEFT)
            }
            if (start.isAfter(LocalDateTime.now())) {
                add(SessionFormError.FUTURE_DATE)
            }
        }
        draft.value = draft.value.copy(bookId = book?.id, errors = errors)
        if (errors.isNotEmpty() || book == null || pages == null) {
            return
        }
        viewModelScope.launch {
            val fresh = books.find(book.id) ?: return@launch
            val startedAt = start.toEpochMillis()
            val durationSeconds = current.durationMinutes * 60L
            val updated = fresh.withCurrentPage(fresh.currentPage + pages, startedAt + durationSeconds * 1000)
            val session = Session(
                bookId = fresh.id,
                startedAt = startedAt,
                durationSeconds = durationSeconds,
                startPage = fresh.currentPage,
                endPage = updated.currentPage
            )
            sessions.record(updated, session)
            draft.value = draft.value.copy(saved = true)
        }
    }

    /**
     * Rascunho inicial, com a sessão terminando agora e começando meia hora antes.
     *
     * @return rascunho com o dia e o horário sugeridos.
     */
    private fun initialDraft(): SessionFormDraft {
        val start = LocalDateTime.now().withSecond(0).withNano(0).minusMinutes(DEFAULT_DURATION.toLong())
        return SessionFormDraft(date = start.toLocalDate(), time = start.toLocalTime())
    }

    companion object {
        val Factory = viewModelFactory {
            initializer {
                val app = this[APPLICATION_KEY] as ReadingApp
                SessionFormViewModel(app.books, app.sessions)
            }
        }
    }
}

/**
 * Segmento Sessão do Novo registro: registra uma leitura já feita, com páginas, livro,
 * dia, horário e duração. Ocupa o espaço abaixo do seletor de segmentos e traz o próprio
 * botão de salvar no rodapé.
 *
 * @param onSaved chamado depois que a sessão é gravada, para fechar o formulário.
 * @param modifier modificador aplicado ao segmento.
 * @param viewModel estado do segmento, criado pela fábrica do app.
 */
@Composable
fun SessionForm(
    onSaved: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: SessionFormViewModel = viewModel(factory = SessionFormViewModel.Factory)
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    LaunchedEffect(state.saved) {
        if (state.saved) {
            onSaved()
        }
    }
    SessionFormContent(
        state = state,
        onSelectBook = viewModel::selectBook,
        onPagesChange = viewModel::changePages,
        onDateChange = viewModel::changeDate,
        onTimeChange = viewModel::changeTime,
        onDurationChange = viewModel::changeDuration,
        onSave = viewModel::save,
        modifier = modifier
    )
}

/**
 * Campos do segmento Sessão, sem depender do ViewModel.
 *
 * @param state estado atual do segmento.
 * @param onSelectBook escolhe o livro.
 * @param onPagesChange atualiza as páginas lidas.
 * @param onDateChange atualiza o dia.
 * @param onTimeChange atualiza o horário de início.
 * @param onDurationChange atualiza a duração.
 * @param onSave valida e grava a sessão.
 * @param modifier modificador aplicado ao segmento.
 */
@Composable
private fun SessionFormContent(
    state: SessionFormState,
    onSelectBook: (Book) -> Unit,
    onPagesChange: (String) -> Unit,
    onDateChange: (LocalDate) -> Unit,
    onTimeChange: (LocalTime) -> Unit,
    onDurationChange: (Int) -> Unit,
    onSave: () -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = ReadingTheme.colors
    var showBooks by rememberSaveable { mutableStateOf(false) }
    var showDate by rememberSaveable { mutableStateOf(false) }
    var showTime by rememberSaveable { mutableStateOf(false) }
    val book = state.book
    val pagesError = when {
        SessionFormError.PAGES_REQUIRED in state.errors -> stringResource(R.string.error_session_pages)
        SessionFormError.PAGES_ABOVE_LEFT in state.errors && book != null -> pluralStringResource(
            R.plurals.error_session_pages_left,
            book.pagesLeft,
            Formats.integer(book.pagesLeft)
        )
        else -> null
    }
    val dateError = if (SessionFormError.FUTURE_DATE in state.errors) {
        stringResource(R.string.error_session_future)
    } else {
        null
    }
    Column(modifier = modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp)
        ) {
            LargeNumberField(
                label = stringResource(R.string.session_form_pages),
                value = state.pages,
                onValueChange = onPagesChange,
                modifier = Modifier.padding(top = 28.dp),
                error = pagesError,
                maxDigits = 4,
                imeAction = ImeAction.Done
            )
            if (book != null && pagesError == null) {
                Text(
                    text = pluralStringResource(
                        R.plurals.session_form_pages_left,
                        book.pagesLeft,
                        Formats.integer(book.pagesLeft)
                    ),
                    modifier = Modifier.padding(top = 4.dp),
                    style = ReadingTheme.typography.caption,
                    color = colors.text3
                )
            }
            PickerField(
                label = stringResource(R.string.session_form_book),
                value = book?.title,
                placeholder = stringResource(R.string.session_form_book_placeholder),
                icon = painterResource(R.drawable.ic_menu_book),
                onClick = { showBooks = true },
                modifier = Modifier.padding(top = 20.dp),
                error = if (SessionFormError.BOOK_REQUIRED in state.errors) {
                    stringResource(R.string.error_session_book)
                } else {
                    null
                }
            )
            Row(
                modifier = Modifier.padding(top = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                PickerField(
                    label = stringResource(R.string.session_form_date),
                    value = Formats.fullDate(state.date),
                    placeholder = "",
                    icon = painterResource(R.drawable.ic_calendar_month),
                    onClick = { showDate = true },
                    modifier = Modifier.weight(1f),
                    error = dateError
                )
                PickerField(
                    label = stringResource(R.string.session_form_start),
                    value = Formats.time(state.time),
                    placeholder = "",
                    icon = painterResource(R.drawable.ic_schedule),
                    onClick = { showTime = true },
                    modifier = Modifier.width(112.dp)
                )
            }
            Column(modifier = Modifier.padding(top = 20.dp, bottom = 8.dp)) {
                FieldLabel(label = stringResource(R.string.session_form_duration), error = null)
                FlowRow(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 10.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    DurationOptions.forEach { minutes ->
                        Pill(
                            text = durationLabel(minutes),
                            selected = minutes == state.durationMinutes,
                            onClick = { onDurationChange(minutes) }
                        )
                    }
                }
            }
        }
        BottomActions {
            PrimaryButton(text = stringResource(R.string.action_save), onClick = onSave, enabled = state.loaded)
        }
    }
    if (showBooks) {
        BookPickerDialog(
            books = state.books,
            selected = book,
            onDismiss = { showBooks = false },
            onSelect = { chosen ->
                showBooks = false
                onSelectBook(chosen)
            }
        )
    }
    if (showDate) {
        FormDateDialog(
            date = state.date,
            limit = DateLimit.UNTIL_TODAY,
            onDismiss = { showDate = false },
            onConfirm = { date ->
                showDate = false
                onDateChange(date)
            }
        )
    }
    if (showTime) {
        FormTimeDialog(
            title = stringResource(R.string.session_form_start),
            time = state.time,
            onDismiss = { showTime = false },
            onConfirm = { time ->
                showTime = false
                onTimeChange(time)
            }
        )
    }
}

/**
 * Texto de uma pílula de duração, em minutos ou em hora.
 *
 * @param minutes duração da opção.
 * @return rótulo como 30 min ou 1 hora.
 */
@Composable
private fun durationLabel(minutes: Int): String =
    if (minutes % MINUTES_IN_HOUR == 0) {
        pluralStringResource(R.plurals.session_duration_hours, minutes / MINUTES_IN_HOUR, minutes / MINUTES_IN_HOUR)
    } else {
        stringResource(R.string.session_duration_minutes, minutes)
    }

/**
 * Lista dos livros ainda não lidos, para escolher o da sessão.
 *
 * @param books livros em leitura e na fila.
 * @param selected livro escolhido no momento.
 * @param onDismiss fecha a lista sem trocar.
 * @param onSelect escolhe o livro tocado.
 */
@Composable
private fun BookPickerDialog(
    books: List<Book>,
    selected: Book?,
    onDismiss: () -> Unit,
    onSelect: (Book) -> Unit
) {
    val colors = ReadingTheme.colors
    val typography = ReadingTheme.typography
    CardDialog(
        title = stringResource(R.string.session_form_book_dialog),
        onDismiss = onDismiss,
        message = if (books.isEmpty()) stringResource(R.string.session_form_no_books) else null
    ) {
        Column(
            modifier = Modifier
                .heightIn(max = 360.dp)
                .verticalScroll(rememberScrollState())
        ) {
            books.forEach { book ->
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .bottomBorder(colors.line)
                        .clickable(role = Role.RadioButton, onClick = { onSelect(book) })
                        .padding(vertical = 12.dp),
                    verticalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    Text(
                        text = book.title,
                        style = typography.bodyMedium,
                        color = if (book.id == selected?.id) colors.positive else colors.text,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = stringResource(
                            R.string.pair_format,
                            stringResource(Labels.status(book.status)),
                            stringResource(
                                R.string.progress_format,
                                Formats.integer(book.currentPage),
                                Formats.integer(book.totalPages)
                            )
                        ),
                        style = typography.caption,
                        color = colors.text2
                    )
                }
            }
        }
        PlainButton(text = stringResource(R.string.action_cancel), onClick = onDismiss)
    }
}
