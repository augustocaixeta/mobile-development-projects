package br.edu.iftm.readingmanager.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import br.edu.iftm.readingmanager.ReadingApp
import br.edu.iftm.readingmanager.data.Book
import br.edu.iftm.readingmanager.data.BookRepository
import br.edu.iftm.readingmanager.data.BookStatus
import br.edu.iftm.readingmanager.data.Genre
import br.edu.iftm.readingmanager.data.GoalRepository
import java.time.LocalDate
import java.time.YearMonth
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update

enum class StatusFilter(val status: BookStatus?) {
    ALL(null),
    READING(BookStatus.READING),
    READ(BookStatus.READ),
    WANT_TO_READ(BookStatus.WANT_TO_READ)
}

data class Filters(
    val status: StatusFilter = StatusFilter.ALL,
    val genre: Genre? = null
) {
    val isDefault: Boolean
        get() = status == StatusFilter.ALL && genre == null
}

sealed interface HomeItem {
    data class Section(val status: BookStatus) : HomeItem
    data class Entry(val book: Book) : HomeItem
}

data class MonthSummary(
    val month: YearMonth = YearMonth.now(),
    val pagesRead: Int = 0,
    val booksRead: Int = 0,
    val goal: Int? = null
) {
    val goalPercent: Int?
        get() = goal?.let { pagesRead * 100 / it }
}

data class HomeUiState(
    val today: LocalDate = LocalDate.now(),
    val filters: Filters = Filters(),
    val summary: MonthSummary = MonthSummary(),
    val items: List<HomeItem> = emptyList(),
    val loaded: Boolean = false
)

@OptIn(ExperimentalCoroutinesApi::class)
class HomeViewModel(books: BookRepository, goals: GoalRepository) : ViewModel() {

    private val filters = MutableStateFlow(Filters())
    private val today = MutableStateFlow(LocalDate.now())

    private val catalog = filters.flatMapLatest { current ->
        books.observeFiltered(current.status.status, current.genre).map { list -> current to list }
    }

    private val finishedThisMonth = today
        .map { YearMonth.from(it) }
        .distinctUntilChanged()
        .flatMapLatest { month -> books.observeFinishedIn(month).map { list -> month to list } }

    val state: StateFlow<HomeUiState> =
        combine(catalog, finishedThisMonth, goals.observeMonthlyPages(), today) { filtered, finished, goal, date ->
            val (current, list) = filtered
            val (month, read) = finished
            HomeUiState(
                today = date,
                filters = current,
                summary = MonthSummary(
                    month = month,
                    pagesRead = read.sumOf { it.totalPages },
                    booksRead = read.size,
                    goal = goal
                ),
                items = group(list),
                loaded = true
            )
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), HomeUiState())

    /**
     * Troca a aba de situação exibida na lista.
     *
     * @param status filtro escolhido pelo usuário.
     */
    fun selectStatus(status: StatusFilter) {
        filters.update { it.copy(status = status) }
    }

    /**
     * Troca o gênero filtrado. Tocar no gênero já selecionado mostra todos de novo.
     *
     * @param genre gênero escolhido, ou null para todos.
     */
    fun selectGenre(genre: Genre?) {
        filters.update { it.copy(genre = if (it.genre == genre) null else genre) }
    }

    /**
     * Atualiza a data de referência, usada no cabeçalho, no resumo do mês e nos prazos relativos.
     */
    fun refreshDate() {
        today.value = LocalDate.now()
    }

    /**
     * Separa os livros em seções por situação: lendo, quero ler e lidos.
     * Os de leitura pendente vêm pelo prazo mais próximo, e os lidos pelo término mais recente.
     *
     * @param books livros já filtrados pelo banco.
     * @return itens da lista com os cabeçalhos de seção.
     */
    private fun group(books: List<Book>): List<HomeItem> {
        val byDeadline = compareBy<Book, Long?>(nullsLast()) { it.deadline }
        val sections = linkedMapOf(
            BookStatus.READING to books
                .filter { it.status == BookStatus.READING }
                .sortedWith(byDeadline.thenBy { it.title.lowercase() }),
            BookStatus.WANT_TO_READ to books
                .filter { it.status == BookStatus.WANT_TO_READ }
                .sortedWith(byDeadline.thenByDescending { it.createdAt }),
            BookStatus.READ to books
                .filter { it.status == BookStatus.READ }
                .sortedByDescending { it.finishedAt }
        )
        return sections
            .filterValues { it.isNotEmpty() }
            .flatMap { (status, entries) ->
                listOf(HomeItem.Section(status)) + entries.map { HomeItem.Entry(it) }
            }
    }

    companion object {
        val Factory = viewModelFactory {
            initializer {
                val app = this[APPLICATION_KEY] as ReadingApp
                HomeViewModel(app.books, app.goals)
            }
        }
    }
}
