package br.edu.iftm.readingmanager.data

import kotlinx.coroutines.flow.Flow

class NoteRepository(private val dao: NoteDao) {

    /**
     * Observa as notas de um livro.
     *
     * @param bookId identificador do livro.
     * @return fluxo com as notas, das mais recentes para as mais antigas.
     */
    fun observeByBook(bookId: Long): Flow<List<Note>> = dao.observeByBook(bookId)

    /**
     * Insere a nota quando ela é nova ou atualiza quando já tem id.
     *
     * @param note nota a ser gravada.
     * @return id da nota gravada.
     */
    suspend fun save(note: Note): Long {
        if (note.id == 0L) {
            return dao.insert(note)
        }
        dao.update(note)
        return note.id
    }

    /**
     * Remove uma nota do diário.
     *
     * @param id identificador da nota.
     */
    suspend fun delete(id: Long) {
        dao.delete(id)
    }
}
