package br.edu.iftm.readingmanager.data

import kotlinx.coroutines.flow.Flow

class SessionRepository(private val dao: SessionDao) {

    /**
     * Observa todo o histórico de sessões, usado nas estatísticas de desempenho.
     *
     * @return fluxo com as sessões em ordem cronológica.
     */
    fun observeAll(): Flow<List<Session>> = dao.observeAll()

    /**
     * Observa as sessões de um livro.
     *
     * @param bookId identificador do livro.
     * @return fluxo com as sessões do livro em ordem cronológica.
     */
    fun observeByBook(bookId: Long): Flow<List<Session>> = dao.observeByBook(bookId)

    /**
     * Grava o progresso do livro junto com a sessão de leitura, quando houver uma.
     *
     * @param book livro atualizado.
     * @param session sessão que gerou o progresso, ou null.
     */
    suspend fun record(book: Book, session: Session?) {
        dao.record(book, session)
    }
}
