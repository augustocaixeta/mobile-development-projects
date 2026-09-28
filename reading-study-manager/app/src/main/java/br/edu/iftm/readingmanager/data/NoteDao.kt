package br.edu.iftm.readingmanager.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface NoteDao {

    /**
     * Observa o diário de um livro, com as notas mais recentes primeiro.
     *
     * @param bookId identificador do livro.
     * @return fluxo que emite de novo a cada alteração nas notas.
     */
    @Query("SELECT * FROM notes WHERE bookId = :bookId ORDER BY createdAt DESC, id DESC")
    fun observeByBook(bookId: Long): Flow<List<Note>>

    /**
     * Insere uma nota nova.
     *
     * @param note nota a ser gravada.
     * @return id gerado pelo banco.
     */
    @Insert
    suspend fun insert(note: Note): Long

    /**
     * Atualiza uma nota existente.
     *
     * @param note nota com os novos dados.
     */
    @Update
    suspend fun update(note: Note)

    /**
     * Remove uma nota.
     *
     * @param id identificador da nota.
     */
    @Query("DELETE FROM notes WHERE id = :id")
    suspend fun delete(id: Long)
}
