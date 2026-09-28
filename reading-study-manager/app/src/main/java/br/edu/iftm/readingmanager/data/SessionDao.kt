package br.edu.iftm.readingmanager.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
abstract class SessionDao {

    /**
     * Observa todas as sessões de leitura, das mais antigas para as mais recentes.
     *
     * @return fluxo que emite de novo a cada sessão registrada.
     */
    @Query("SELECT * FROM sessions ORDER BY startedAt, id")
    abstract fun observeAll(): Flow<List<Session>>

    /**
     * Observa as sessões de um livro.
     *
     * @param bookId identificador do livro.
     * @return fluxo com as sessões do livro, das mais antigas para as mais recentes.
     */
    @Query("SELECT * FROM sessions WHERE bookId = :bookId ORDER BY startedAt, id")
    abstract fun observeByBook(bookId: Long): Flow<List<Session>>

    /**
     * Insere uma sessão de leitura.
     *
     * @param session sessão a ser gravada.
     * @return id gerado pelo banco.
     */
    @Insert
    abstract suspend fun insert(session: Session): Long

    /**
     * Atualiza o livro que teve o progresso alterado.
     *
     * @param book livro com a nova página e situação.
     */
    @Update
    abstract suspend fun updateBook(book: Book)

    /**
     * Grava o novo progresso do livro e a sessão que o gerou na mesma transação,
     * para que o livro e o histórico de leitura nunca fiquem diferentes.
     *
     * @param book livro já com a nova página e situação.
     * @param session sessão a registrar, ou null quando só o livro muda.
     */
    @Transaction
    open suspend fun record(book: Book, session: Session?) {
        updateBook(book)
        if (session != null) {
            insert(session)
        }
    }
}
