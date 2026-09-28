package br.edu.iftm.readingmanager.ui.session

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.createSavedStateHandle
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import androidx.navigation.toRoute
import br.edu.iftm.readingmanager.ReadingApp
import br.edu.iftm.readingmanager.data.Book
import br.edu.iftm.readingmanager.data.BookRepository
import br.edu.iftm.readingmanager.data.SessionRepository
import br.edu.iftm.readingmanager.ui.navigation.ReadingSession
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class SessionUiState(
    val book: Book? = null,
    val startPage: Int = 0,
    val draft: SessionDraft = SessionDraft(startedAt = 0),
    val elapsedSeconds: Long = 0,
    val loaded: Boolean = false,
    val finished: Boolean = false
)

class SessionViewModel(
    private val savedStateHandle: SavedStateHandle,
    private val books: BookRepository,
    private val sessions: SessionRepository
) : ViewModel() {

    private val bookId = savedStateHandle.toRoute<ReadingSession>().bookId
    private val draft = MutableStateFlow(restoreDraft())
    private val startPage = MutableStateFlow(savedStateHandle.get<Int>(KEY_START_PAGE))
    private val finished = MutableStateFlow(false)

    private val clock = flow {
        while (true) {
            emit(System.currentTimeMillis())
            delay(1_000)
        }
    }

    val state: StateFlow<SessionUiState> =
        combine(books.observe(bookId), draft, startPage, clock, finished) { book, current, start, now, done ->
            SessionUiState(
                book = book,
                startPage = start ?: book?.currentPage ?: 0,
                draft = current,
                elapsedSeconds = current.elapsedSeconds(now),
                loaded = true,
                finished = done
            )
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), SessionUiState())

    init {
        viewModelScope.launch {
            val book = books.observe(bookId).first() ?: return@launch
            if (startPage.value == null) {
                startPage.value = book.currentPage
                savedStateHandle[KEY_START_PAGE] = book.currentPage
            }
            if (!draft.value.prepared) {
                val start = startPage.value ?: book.currentPage
                update(draft.value.copy(marks = suggestedMarks(start, book.totalPages), prepared = true))
            }
        }
    }

    /**
     * Pausa ou retoma o cronômetro da sessão.
     */
    fun togglePause() {
        val now = System.currentTimeMillis()
        val current = draft.value
        update(if (current.isRunning) current.paused(now) else current.resumed(now))
    }

    /**
     * Marca ou desmarca uma página como alcançada nesta sessão.
     *
     * @param page página da marcação tocada.
     */
    fun toggleMark(page: Int) {
        update(draft.value.toggled(page))
    }

    /**
     * Inclui uma nova marcação de página, já validada pela tela.
     *
     * @param page página da nova marcação.
     */
    fun addMark(page: Int) {
        update(draft.value.withMark(page))
    }

    /**
     * Grava a página final no livro e registra a sessão com o tempo lido,
     * depois avisa a tela para fechar.
     *
     * @param page página final informada pelo leitor.
     */
    fun finish(page: Int) {
        viewModelScope.launch {
            val book = books.find(bookId) ?: return@launch
            val now = System.currentTimeMillis()
            val current = draft.value
            val updated = book.withFinalPage(page, now)
            sessions.record(updated, finishedSession(book, updated, current.startedAt, current.elapsedSeconds(now)))
            finished.value = true
        }
    }

    /**
     * Troca o rascunho da sessão e guarda os campos no estado salvo, para que o cronômetro
     * e as marcações sobrevivam à rotação e ao encerramento do processo.
     *
     * @param next novo rascunho.
     */
    private fun update(next: SessionDraft) {
        draft.value = next
        persist(next)
    }

    /**
     * Guarda os campos do rascunho no estado salvo.
     *
     * @param next rascunho a guardar.
     */
    private fun persist(next: SessionDraft) {
        savedStateHandle[KEY_STARTED_AT] = next.startedAt
        savedStateHandle[KEY_ACCUMULATED] = next.accumulatedSeconds
        savedStateHandle[KEY_RESUMED_AT] = next.resumedAt ?: NOT_RUNNING
        savedStateHandle[KEY_MARKS] = next.marks.toIntArray()
        savedStateHandle[KEY_DONE] = next.done.toIntArray()
        savedStateHandle[KEY_PREPARED] = next.prepared
    }

    /**
     * Recupera o rascunho salvo ou começa uma sessão nova com o cronômetro rodando.
     *
     * @return rascunho da sessão.
     */
    private fun restoreDraft(): SessionDraft {
        val startedAt = savedStateHandle.get<Long>(KEY_STARTED_AT)
        if (startedAt == null) {
            val created = SessionDraft(startedAt = System.currentTimeMillis())
            persist(created)
            return created
        }
        val resumedAt = savedStateHandle.get<Long>(KEY_RESUMED_AT) ?: NOT_RUNNING
        return SessionDraft(
            startedAt = startedAt,
            accumulatedSeconds = savedStateHandle.get<Long>(KEY_ACCUMULATED) ?: 0,
            resumedAt = resumedAt.takeIf { it != NOT_RUNNING },
            marks = savedStateHandle.get<IntArray>(KEY_MARKS)?.toList().orEmpty(),
            done = savedStateHandle.get<IntArray>(KEY_DONE)?.toSet().orEmpty(),
            prepared = savedStateHandle.get<Boolean>(KEY_PREPARED) ?: false
        )
    }

    companion object {
        private const val KEY_START_PAGE = "session_start_page"
        private const val KEY_STARTED_AT = "session_started_at"
        private const val KEY_ACCUMULATED = "session_accumulated"
        private const val KEY_RESUMED_AT = "session_resumed_at"
        private const val KEY_MARKS = "session_marks"
        private const val KEY_DONE = "session_done"
        private const val KEY_PREPARED = "session_prepared"
        private const val NOT_RUNNING = -1L

        val Factory = viewModelFactory {
            initializer {
                val app = this[APPLICATION_KEY] as ReadingApp
                SessionViewModel(createSavedStateHandle(), app.books, app.sessions)
            }
        }
    }
}
