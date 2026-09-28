package br.edu.iftm.readingmanager.data

import br.edu.iftm.readingmanager.util.toEpochMillis
import java.time.YearMonth
import kotlinx.coroutines.flow.Flow

class BookRepository(private val dao: BookDao) {

    /**
     * Observa o catálogo com os filtros de situação e gênero aplicados no banco.
     *
     * @param status situação desejada, ou null para todas.
     * @param genre gênero desejado, ou null para todos.
     * @return fluxo com os livros filtrados.
     */
    fun observeFiltered(status: BookStatus?, genre: Genre?): Flow<List<Book>> =
        dao.observeFiltered(status, genre)

    /**
     * Observa os livros terminados em um mês do calendário.
     *
     * @param month mês de referência.
     * @return fluxo com os livros lidos naquele mês.
     */
    fun observeFinishedIn(month: YearMonth): Flow<List<Book>> {
        val from = month.atDay(1).atStartOfDay().toEpochMillis()
        val until = month.plusMonths(1).atDay(1).atStartOfDay().toEpochMillis()
        return dao.observeFinishedBetween(from, until)
    }

    /**
     * Observa um livro.
     *
     * @param id identificador do livro.
     * @return fluxo que emite null quando o livro é excluído.
     */
    fun observe(id: Long): Flow<Book?> = dao.observe(id)

    /**
     * Busca um livro.
     *
     * @param id identificador do livro.
     * @return o livro ou null se não existir.
     */
    suspend fun find(id: Long): Book? = dao.find(id)

    /**
     * Verifica se já existe outro livro com o mesmo título e autor.
     *
     * @param title título digitado.
     * @param author autor digitado.
     * @param ignoreId livro em edição, que não conta como duplicata.
     * @return true quando o livro já está no catálogo.
     */
    suspend fun isDuplicate(title: String, author: String, ignoreId: Long): Boolean =
        dao.countDuplicates(title.trim(), author.trim(), ignoreId) > 0

    /**
     * Insere o livro quando ele é novo ou atualiza quando já tem id.
     *
     * @param book livro a ser gravado.
     * @return id do livro gravado.
     */
    suspend fun save(book: Book): Long {
        if (book.id == 0L) {
            return dao.insert(book)
        }
        dao.update(book)
        return book.id
    }

    /**
     * Remove um livro do catálogo.
     *
     * @param id identificador do livro.
     */
    suspend fun delete(id: Long) {
        dao.delete(id)
    }
}
