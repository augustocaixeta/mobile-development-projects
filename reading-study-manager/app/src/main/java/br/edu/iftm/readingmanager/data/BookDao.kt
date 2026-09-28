package br.edu.iftm.readingmanager.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface BookDao {

    /**
     * Observa o catálogo aplicando os filtros direto no banco. Um filtro nulo não restringe a busca.
     *
     * @param status situação de leitura desejada, ou null para todas.
     * @param genre gênero desejado, ou null para todos.
     * @return fluxo que emite de novo a cada alteração na tabela, ordenado pelo título.
     */
    @Query(
        "SELECT * FROM books " +
            "WHERE (:status IS NULL OR status = :status) " +
            "AND (:genre IS NULL OR genre = :genre) " +
            "ORDER BY title COLLATE NOCASE"
    )
    fun observeFiltered(status: BookStatus?, genre: Genre?): Flow<List<Book>>

    /**
     * Observa os livros terminados dentro de um intervalo, usado no resumo do mês.
     *
     * @param from início do intervalo em milissegundos, inclusivo.
     * @param until fim do intervalo em milissegundos, exclusivo.
     * @return fluxo com os livros lidos no período.
     */
    @Query("SELECT * FROM books WHERE status = 'READ' AND finishedAt >= :from AND finishedAt < :until")
    fun observeFinishedBetween(from: Long, until: Long): Flow<List<Book>>

    /**
     * Observa um livro pelo identificador.
     *
     * @param id identificador do livro.
     * @return fluxo que emite null quando o livro deixa de existir.
     */
    @Query("SELECT * FROM books WHERE id = :id")
    fun observe(id: Long): Flow<Book?>

    /**
     * Busca um livro pelo identificador.
     *
     * @param id identificador do livro.
     * @return o livro ou null se não existir.
     */
    @Query("SELECT * FROM books WHERE id = :id")
    suspend fun find(id: Long): Book?

    /**
     * Conta livros com o mesmo título e autor, sem diferenciar maiúsculas, para evitar duplicatas.
     *
     * @param title título a comparar.
     * @param author autor a comparar.
     * @param ignoreId livro que fica de fora da contagem, usado na edição.
     * @return quantidade de livros iguais.
     */
    @Query(
        "SELECT COUNT(*) FROM books " +
            "WHERE title = :title COLLATE NOCASE AND author = :author COLLATE NOCASE AND id != :ignoreId"
    )
    suspend fun countDuplicates(title: String, author: String, ignoreId: Long): Int

    /**
     * Insere um livro novo.
     *
     * @param book livro a ser gravado.
     * @return id gerado pelo banco.
     */
    @Insert
    suspend fun insert(book: Book): Long

    /**
     * Atualiza um livro existente.
     *
     * @param book livro com os novos dados.
     */
    @Update
    suspend fun update(book: Book)

    /**
     * Remove um livro.
     *
     * @param id identificador do livro.
     */
    @Query("DELETE FROM books WHERE id = :id")
    suspend fun delete(id: Long)
}
