package br.edu.iftm.readingmanager.ui.detail

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
import br.edu.iftm.readingmanager.data.BookStatus
import br.edu.iftm.readingmanager.data.SessionRepository
import br.edu.iftm.readingmanager.data.progressSession
import br.edu.iftm.readingmanager.data.withCurrentPage
import br.edu.iftm.readingmanager.data.withStatus
import br.edu.iftm.readingmanager.ui.navigation.BookDetail
import java.time.LocalDate
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class DetailUiState(
    val book: Book? = null,
    val today: LocalDate = LocalDate.now(),
    val loaded: Boolean = false,
    val deleted: Boolean = false
)

class DetailViewModel(
    savedStateHandle: SavedStateHandle,
    private val books: BookRepository,
    private val sessions: SessionRepository
) : ViewModel() {

    private val bookId = savedStateHandle.toRoute<BookDetail>().bookId
    private val deleted = MutableStateFlow(false)
    private val today = MutableStateFlow(LocalDate.now())

    val state: StateFlow<DetailUiState> =
        combine(books.observe(bookId), today, deleted) { book, date, gone ->
            DetailUiState(book = book, today = date, loaded = true, deleted = gone)
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), DetailUiState())

    /**
     * Atualiza a página atual digitada pelo usuário. O avanço vira uma sessão de leitura,
     * para aparecer no desempenho, e a situação do livro acompanha a nova página.
     *
     * @param page página já validada pela tela, entre zero e o total do livro.
     */
    fun updatePage(page: Int) {
        viewModelScope.launch {
            val book = books.find(bookId) ?: return@launch
            val now = System.currentTimeMillis()
            val updated = book.withCurrentPage(page, now)
            sessions.record(updated, progressSession(book, updated, now))
        }
    }

    /**
     * Muda a situação do livro. Marcar como lido completa as páginas restantes,
     * que também contam como leitura no desempenho.
     *
     * @param status nova situação escolhida nas pílulas.
     */
    fun changeStatus(status: BookStatus) {
        viewModelScope.launch {
            val book = books.find(bookId) ?: return@launch
            if (book.status == status) {
                return@launch
            }
            val now = System.currentTimeMillis()
            val updated = book.withStatus(status, now)
            sessions.record(updated, progressSession(book, updated, now))
        }
    }

    /**
     * Exclui o livro com as notas e sessões dele e avisa a tela para voltar ao Início.
     */
    fun deleteBook() {
        viewModelScope.launch {
            books.delete(bookId)
            deleted.value = true
        }
    }

    /**
     * Atualiza a data de referência usada no prazo relativo.
     */
    fun refreshDate() {
        today.value = LocalDate.now()
    }

    companion object {
        val Factory = viewModelFactory {
            initializer {
                val app = this[APPLICATION_KEY] as ReadingApp
                DetailViewModel(createSavedStateHandle(), app.books, app.sessions)
            }
        }
    }
}
