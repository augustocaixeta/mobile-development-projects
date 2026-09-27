package br.edu.iftm.deadlinetracker.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface ObligationDao {

    /**
     * Observa todas as obrigações cadastradas.
     *
     * @return fluxo com a lista ordenada pelo vencimento.
     */
    @Query("SELECT * FROM obligations ORDER BY dueAt")
    fun observeAll(): Flow<List<Obligation>>

    /**
     * Observa uma obrigação junto com os lembretes da relação 1:N.
     *
     * @param id identificador da obrigação.
     * @return fluxo que emite null quando o registro deixa de existir.
     */
    @Transaction
    @Query("SELECT * FROM obligations WHERE id = :id")
    fun observeWithReminders(id: Long): Flow<ObligationWithReminders?>

    /**
     * Busca uma obrigação junto com os lembretes.
     *
     * @param id identificador da obrigação.
     * @return a obrigação com lembretes ou null se não existir.
     */
    @Transaction
    @Query("SELECT * FROM obligations WHERE id = :id")
    suspend fun findWithReminders(id: Long): ObligationWithReminders?

    /**
     * Busca uma obrigação pelo identificador.
     *
     * @param id identificador da obrigação.
     * @return a obrigação ou null se não existir.
     */
    @Query("SELECT * FROM obligations WHERE id = :id")
    suspend fun find(id: Long): Obligation?

    /**
     * Lista as obrigações pendentes com vencimento anterior ao limite.
     *
     * @param limit instante de corte em milissegundos.
     * @return obrigações vencidas ordenadas pelo vencimento.
     */
    @Query("SELECT * FROM obligations WHERE status = 'PENDING' AND dueAt < :limit ORDER BY dueAt")
    suspend fun listOverdue(limit: Long): List<Obligation>

    /**
     * Conta obrigações iguais para evitar duplicar uma ocorrência mensal.
     *
     * @param type tipo da obrigação.
     * @param title descrição da obrigação.
     * @param dueAt vencimento em milissegundos.
     * @return quantidade de registros encontrados.
     */
    @Query("SELECT COUNT(*) FROM obligations WHERE type = :type AND title = :title AND dueAt = :dueAt")
    suspend fun countMatching(type: ObligationType, title: String, dueAt: Long): Int

    /**
     * Insere uma nova obrigação.
     *
     * @param obligation obrigação a ser gravada.
     * @return id gerado pelo banco.
     */
    @Insert
    suspend fun insert(obligation: Obligation): Long

    /**
     * Atualiza uma obrigação existente.
     *
     * @param obligation obrigação com os novos dados.
     */
    @Update
    suspend fun update(obligation: Obligation)

    /**
     * Remove uma obrigação.
     *
     * @param id identificador da obrigação.
     */
    @Query("DELETE FROM obligations WHERE id = :id")
    suspend fun delete(id: Long)

    /**
     * Busca um lembrete pelo identificador.
     *
     * @param id identificador do lembrete.
     * @return o lembrete ou null se não existir.
     */
    @Query("SELECT * FROM reminders WHERE id = :id")
    suspend fun findReminder(id: Long): Reminder?

    /**
     * Insere os lembretes de uma obrigação.
     *
     * @param reminders lembretes a serem gravados.
     * @return ids gerados, na mesma ordem da lista.
     */
    @Insert
    suspend fun insertReminders(reminders: List<Reminder>): List<Long>

    /**
     * Remove todos os lembretes de uma obrigação.
     *
     * @param obligationId identificador da obrigação.
     */
    @Query("DELETE FROM reminders WHERE obligationId = :obligationId")
    suspend fun deleteReminders(obligationId: Long)

    /**
     * Altera o estado de um lembrete.
     *
     * @param id identificador do lembrete.
     * @param state novo estado.
     */
    @Query("UPDATE reminders SET state = :state WHERE id = :id")
    suspend fun updateReminderState(id: Long, state: ReminderState)

    /**
     * Cancela os lembretes que ainda não dispararam.
     *
     * @param obligationId identificador da obrigação.
     */
    @Query("UPDATE reminders SET state = 'CANCELED' WHERE obligationId = :obligationId AND state = 'SCHEDULED'")
    suspend fun cancelReminders(obligationId: Long)
}
