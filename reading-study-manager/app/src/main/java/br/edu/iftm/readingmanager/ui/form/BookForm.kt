package br.edu.iftm.readingmanager.ui.form

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.unit.dp
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.createSavedStateHandle
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import androidx.navigation.toRoute
import br.edu.iftm.readingmanager.R
import br.edu.iftm.readingmanager.ReadingApp
import br.edu.iftm.readingmanager.data.Book
import br.edu.iftm.readingmanager.data.BookIcon
import br.edu.iftm.readingmanager.data.BookRepository
import br.edu.iftm.readingmanager.data.Genre
import br.edu.iftm.readingmanager.ui.components.BottomActions
import br.edu.iftm.readingmanager.ui.components.FieldLabel
import br.edu.iftm.readingmanager.ui.components.Labels
import br.edu.iftm.readingmanager.ui.components.LargeNumberField
import br.edu.iftm.readingmanager.ui.components.LineTextField
import br.edu.iftm.readingmanager.ui.components.PickerField
import br.edu.iftm.readingmanager.ui.components.Pill
import br.edu.iftm.readingmanager.ui.components.PlainButton
import br.edu.iftm.readingmanager.ui.components.PrimaryButton
import br.edu.iftm.readingmanager.ui.navigation.BookForm
import br.edu.iftm.readingmanager.ui.theme.ReadingTheme
import br.edu.iftm.readingmanager.util.Formats
import br.edu.iftm.readingmanager.util.toLocalDateTime
import java.time.LocalDate
import java.time.LocalDateTime
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class BookFormState(
    val input: BookInput = BookInput(),
    val errors: Set<BookFormError> = emptySet(),
    val editing: Boolean = false,
    val currentPage: Int = 0,
    val loaded: Boolean = false,
    val saving: Boolean = false,
    val saved: Boolean = false
)

class BookFormViewModel(
    savedStateHandle: SavedStateHandle,
    private val books: BookRepository
) : ViewModel() {

    private val bookId = savedStateHandle.toRoute<BookForm>().bookId
    private var original: Book? = null
    private val mutableState = MutableStateFlow(BookFormState(editing = bookId != null, loaded = bookId == null))

    val state: StateFlow<BookFormState> = mutableState.asStateFlow()

    init {
        if (bookId != null) {
            viewModelScope.launch {
                val book = books.find(bookId)
                original = book
                mutableState.update { current ->
                    current.copy(
                        input = book?.toInput(book.deadline?.toLocalDateTime()) ?: current.input,
                        currentPage = book?.currentPage ?: 0,
                        loaded = true
                    )
                }
            }
        }
    }

    /**
     * Atualiza os campos do livro e limpa os erros dos campos alterados.
     *
     * @param input campos com a alteração feita pelo usuário.
     */
    fun change(input: BookInput) {
        mutableState.update { current ->
            val before = current.input
            val cleared = current.errors.filterNot { error ->
                when (error) {
                    BookFormError.PAGES_REQUIRED, BookFormError.PAGES_BELOW_CURRENT -> input.pages != before.pages
                    BookFormError.TITLE_REQUIRED -> input.title != before.title
                    BookFormError.AUTHOR_REQUIRED -> input.author != before.author
                    BookFormError.DUPLICATE -> input.title != before.title || input.author != before.author
                    BookFormError.DEADLINE_PAST ->
                        input.deadlineDate != before.deadlineDate || input.deadlineTime != before.deadlineTime
                }
            }.toSet()
            current.copy(input = input, errors = cleared)
        }
    }

    /**
     * Valida os campos, confere se o livro já existe no catálogo e grava.
     */
    fun save() {
        val current = mutableState.value
        if (!current.loaded || current.saving) {
            return
        }
        val existing = original
        val input = current.input
        val originalDeadline = existing?.deadline?.toLocalDateTime()
        val errors = validateBook(
            input = input,
            currentPage = existing?.currentPage ?: 0,
            deadlineChanged = input.deadline() != originalDeadline,
            now = LocalDateTime.now()
        )
        if (errors.isNotEmpty()) {
            mutableState.update { it.copy(errors = errors) }
            return
        }
        mutableState.update { it.copy(saving = true) }
        viewModelScope.launch {
            if (books.isDuplicate(input.title, input.author, existing?.id ?: 0L)) {
                mutableState.update { it.copy(errors = setOf(BookFormError.DUPLICATE), saving = false) }
                return@launch
            }
            books.save(bookFromInput(existing, input, System.currentTimeMillis()))
            mutableState.update { it.copy(saving = false, saved = true) }
        }
    }

    companion object {
        val Factory = viewModelFactory {
            initializer {
                val app = this[APPLICATION_KEY] as ReadingApp
                BookFormViewModel(createSavedStateHandle(), app.books)
            }
        }
    }
}

/**
 * Segmento Livro do Novo registro, também usado para editar um livro do catálogo.
 * Ocupa o espaço abaixo do seletor de segmentos e traz o próprio botão de salvar.
 *
 * @param viewModel estado do formulário, compartilhado com a tela para saber se é edição.
 * @param onSaved chamado depois que o livro é gravado.
 * @param modifier modificador aplicado ao segmento.
 */
@Composable
fun BookFormSection(viewModel: BookFormViewModel, onSaved: () -> Unit, modifier: Modifier = Modifier) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    LaunchedEffect(state.saved) {
        if (state.saved) {
            onSaved()
        }
    }
    BookFormContent(state = state, onChange = viewModel::change, onSave = viewModel::save, modifier = modifier)
}

/**
 * Campos do segmento Livro, sem depender do ViewModel.
 *
 * @param state estado atual do formulário.
 * @param onChange recebe os campos alterados.
 * @param onSave valida e grava o livro.
 * @param modifier modificador aplicado ao segmento.
 */
@Composable
private fun BookFormContent(
    state: BookFormState,
    onChange: (BookInput) -> Unit,
    onSave: () -> Unit,
    modifier: Modifier = Modifier
) {
    val input = state.input
    val errors = state.errors
    var showDate by rememberSaveable { mutableStateOf(false) }
    var showTime by rememberSaveable { mutableStateOf(false) }
    val pagesError = when {
        BookFormError.PAGES_REQUIRED in errors -> stringResource(R.string.error_book_pages)
        BookFormError.PAGES_BELOW_CURRENT in errors ->
            stringResource(R.string.error_book_pages_below_current, Formats.integer(state.currentPage))
        else -> null
    }
    val titleError = when {
        BookFormError.TITLE_REQUIRED in errors -> stringResource(R.string.error_book_title)
        BookFormError.DUPLICATE in errors -> stringResource(R.string.error_book_duplicate)
        else -> null
    }
    Column(modifier = modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp)
        ) {
            LargeNumberField(
                label = stringResource(R.string.book_form_pages),
                value = input.pages,
                onValueChange = { onChange(input.copy(pages = it)) },
                modifier = Modifier.padding(top = 28.dp),
                error = pagesError
            )
            LineTextField(
                label = stringResource(R.string.book_form_title),
                value = input.title,
                onValueChange = { onChange(input.copy(title = it)) },
                modifier = Modifier.padding(top = 20.dp),
                placeholder = stringResource(R.string.book_form_title_placeholder),
                error = titleError,
                capitalization = KeyboardCapitalization.Sentences
            )
            LineTextField(
                label = stringResource(R.string.book_form_author),
                value = input.author,
                onValueChange = { onChange(input.copy(author = it)) },
                modifier = Modifier.padding(top = 12.dp),
                placeholder = stringResource(R.string.book_form_author_placeholder),
                error = if (BookFormError.AUTHOR_REQUIRED in errors) stringResource(R.string.error_book_author) else null,
                capitalization = KeyboardCapitalization.Words,
                imeAction = ImeAction.Done
            )
            Row(
                modifier = Modifier.padding(top = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                PickerField(
                    label = stringResource(R.string.book_form_deadline),
                    value = input.deadlineDate?.let(Formats::fullDate),
                    placeholder = stringResource(R.string.no_deadline),
                    icon = painterResource(R.drawable.ic_calendar_month),
                    onClick = { showDate = true },
                    modifier = Modifier.weight(1f),
                    error = if (BookFormError.DEADLINE_PAST in errors) stringResource(R.string.error_book_deadline) else null
                )
                PickerField(
                    label = stringResource(R.string.book_form_time),
                    value = input.deadlineTime?.let(Formats::time),
                    placeholder = Formats.time(DefaultDeadlineTime),
                    icon = painterResource(R.drawable.ic_schedule),
                    onClick = { showTime = true },
                    modifier = Modifier.width(112.dp),
                    enabled = input.deadlineDate != null
                )
            }
            if (input.deadlineDate != null) {
                PlainButton(
                    text = stringResource(R.string.book_form_remove_deadline),
                    onClick = { onChange(input.copy(deadlineDate = null, deadlineTime = null)) },
                    modifier = Modifier.padding(top = 4.dp)
                )
            }
            Column(modifier = Modifier.padding(top = 20.dp)) {
                FieldLabel(label = stringResource(R.string.book_form_genre), error = null)
                FlowRow(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 10.dp)
                        .selectableGroup(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Genre.entries.forEach { genre ->
                        Pill(
                            text = stringResource(Labels.genre(genre)),
                            selected = genre == input.genre,
                            onClick = { onChange(input.copy(genre = genre)) }
                        )
                    }
                }
            }
            Column(modifier = Modifier.padding(top = 20.dp, bottom = 8.dp)) {
                FieldLabel(label = stringResource(R.string.book_form_icon), error = null)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 10.dp)
                        .selectableGroup(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    BookIcon.entries.forEach { icon ->
                        IconChoice(
                            icon = icon,
                            selected = icon == input.icon,
                            onClick = { onChange(input.copy(icon = icon)) }
                        )
                    }
                }
            }
        }
        BottomActions {
            PrimaryButton(
                text = stringResource(R.string.action_save),
                onClick = onSave,
                enabled = state.loaded && !state.saving
            )
        }
    }
    if (showDate) {
        FormDateDialog(
            date = input.deadlineDate ?: LocalDate.now(),
            limit = DateLimit.FROM_TODAY,
            onDismiss = { showDate = false },
            onConfirm = { date ->
                showDate = false
                onChange(input.copy(deadlineDate = date))
            }
        )
    }
    if (showTime) {
        FormTimeDialog(
            title = stringResource(R.string.book_form_time),
            time = input.deadlineTime ?: DefaultDeadlineTime,
            onDismiss = { showTime = false },
            onConfirm = { time ->
                showTime = false
                onChange(input.copy(deadlineTime = time))
            }
        )
    }
}

/**
 * Opção de ícone do livro, em círculo preenchido quando escolhida e só com contorno quando não.
 *
 * @param icon ícone da opção.
 * @param selected true quando é o ícone escolhido.
 * @param onClick escolhe o ícone.
 */
@Composable
private fun IconChoice(icon: BookIcon, selected: Boolean, onClick: () -> Unit) {
    val colors = ReadingTheme.colors
    Box(
        modifier = Modifier
            .size(40.dp)
            .clip(CircleShape)
            .background(if (selected) colors.text else colors.bg)
            .then(if (selected) Modifier else Modifier.border(1.dp, colors.line, CircleShape))
            .selectable(selected = selected, role = Role.RadioButton, onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            painter = painterResource(Labels.icon(icon)),
            contentDescription = null,
            modifier = Modifier.size(20.dp),
            tint = if (selected) colors.onFill else colors.text2
        )
    }
}
