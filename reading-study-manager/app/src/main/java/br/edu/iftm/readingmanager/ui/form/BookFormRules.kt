package br.edu.iftm.readingmanager.ui.form

import br.edu.iftm.readingmanager.data.Book
import br.edu.iftm.readingmanager.data.BookIcon
import br.edu.iftm.readingmanager.data.Genre
import br.edu.iftm.readingmanager.data.isRead
import br.edu.iftm.readingmanager.data.withCurrentPage
import br.edu.iftm.readingmanager.util.toEpochMillis
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime

val DefaultDeadlineTime: LocalTime = LocalTime.of(23, 59)

enum class BookFormError {
    PAGES_REQUIRED,
    PAGES_BELOW_CURRENT,
    TITLE_REQUIRED,
    AUTHOR_REQUIRED,
    DUPLICATE,
    DEADLINE_PAST
}

data class BookInput(
    val pages: String = "",
    val title: String = "",
    val author: String = "",
    val genre: Genre = Genre.FICTION,
    val icon: BookIcon = BookIcon.MENU_BOOK,
    val deadlineDate: LocalDate? = null,
    val deadlineTime: LocalTime? = null
)

/**
 * Prazo completo do formulário. Sem horário escolhido, vale o fim do dia.
 *
 * @receiver campos digitados.
 * @return data e hora do prazo, ou null quando o livro não tem prazo.
 */
fun BookInput.deadline(): LocalDateTime? = deadlineDate?.atTime(deadlineTime ?: DefaultDeadlineTime)

/**
 * Preenche o formulário com os dados de um livro já cadastrado, para a edição.
 *
 * @receiver livro em edição.
 * @param deadline prazo do livro já convertido para data e hora locais.
 * @return campos preenchidos.
 */
fun Book.toInput(deadline: LocalDateTime?): BookInput = BookInput(
    pages = totalPages.toString(),
    title = title,
    author = author,
    genre = genre,
    icon = icon,
    deadlineDate = deadline?.toLocalDate(),
    deadlineTime = deadline?.toLocalTime()
)

/**
 * Valida os campos do livro. A página atual impede diminuir o total abaixo do que já foi lido,
 * e o prazo só precisa estar no futuro quando foi alterado.
 *
 * @param input campos digitados.
 * @param currentPage página atual do livro em edição, ou zero para um livro novo.
 * @param deadlineChanged true quando o prazo é novo ou diferente do gravado.
 * @param now instante da validação.
 * @return erros encontrados, vazio quando os campos estão corretos.
 */
fun validateBook(
    input: BookInput,
    currentPage: Int,
    deadlineChanged: Boolean,
    now: LocalDateTime
): Set<BookFormError> = buildSet {
    val pages = input.pages.toIntOrNull()
    if (pages == null || pages <= 0) {
        add(BookFormError.PAGES_REQUIRED)
    } else if (pages < currentPage) {
        add(BookFormError.PAGES_BELOW_CURRENT)
    }
    if (input.title.isBlank()) {
        add(BookFormError.TITLE_REQUIRED)
    }
    if (input.author.isBlank()) {
        add(BookFormError.AUTHOR_REQUIRED)
    }
    val deadline = input.deadline()
    if (deadlineChanged && deadline != null && deadline.isBefore(now)) {
        add(BookFormError.DEADLINE_PAST)
    }
}

/**
 * Monta o livro a gravar a partir dos campos já validados. Na edição, um livro lido continua
 * completo, e reduzir o total até a página atual conclui a leitura.
 *
 * @param existing livro em edição, ou null para um livro novo.
 * @param input campos validados.
 * @param now instante da gravação em milissegundos.
 * @return livro pronto para o repositório.
 */
fun bookFromInput(existing: Book?, input: BookInput, now: Long): Book {
    val total = input.pages.toInt()
    val deadline = input.deadline()?.toEpochMillis()
    if (existing == null) {
        return Book(
            title = input.title.trim(),
            author = input.author.trim(),
            totalPages = total,
            genre = input.genre,
            icon = input.icon,
            deadline = deadline,
            createdAt = now
        )
    }
    val edited = existing.copy(
        title = input.title.trim(),
        author = input.author.trim(),
        totalPages = total,
        genre = input.genre,
        icon = input.icon,
        deadline = deadline
    )
    return when {
        existing.isRead -> edited.copy(currentPage = total)
        edited.currentPage >= total -> edited.withCurrentPage(total, now)
        else -> edited
    }
}
