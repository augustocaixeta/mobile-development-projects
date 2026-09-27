package br.edu.iftm.deadlinetracker.data

import androidx.room.withTransaction
import br.edu.iftm.deadlinetracker.scheduling.ReminderScheduler
import br.edu.iftm.deadlinetracker.util.toEpochMillis
import br.edu.iftm.deadlinetracker.util.toLocalDateTime
import kotlinx.coroutines.flow.Flow

class ObligationRepository(
    private val database: AppDatabase,
    private val scheduler: ReminderScheduler
) {

    private val dao = database.obligationDao()

    /**
     * Observa todas as obrigações.
     *
     * @return fluxo com a lista ordenada pelo vencimento.
     */
    fun observeAll(): Flow<List<Obligation>> = dao.observeAll()

    /**
     * Observa uma obrigação com os lembretes dela.
     *
     * @param id identificador da obrigação.
     * @return fluxo que emite null quando o registro é excluído.
     */
    fun observe(id: Long): Flow<ObligationWithReminders?> = dao.observeWithReminders(id)

    /**
     * Busca uma obrigação.
     *
     * @param id identificador da obrigação.
     * @return a obrigação ou null se não existir.
     */
    suspend fun find(id: Long): Obligation? = dao.find(id)

    /**
     * Busca uma obrigação junto com os lembretes cadastrados.
     *
     * @param id identificador da obrigação.
     * @return a obrigação com lembretes ou null se não existir.
     */
    suspend fun findWithReminders(id: Long): ObligationWithReminders? = dao.findWithReminders(id)

    /**
     * Busca um lembrete, usado pelo worker na hora do disparo.
     *
     * @param id identificador do lembrete.
     * @return o lembrete ou null se não existir.
     */
    suspend fun findReminder(id: Long): Reminder? = dao.findReminder(id)

    /**
     * Lista as obrigações pendentes que já passaram do vencimento.
     *
     * @return obrigações atrasadas ordenadas pelo vencimento.
     */
    suspend fun listOverdue(): List<Obligation> = dao.listOverdue(System.currentTimeMillis())

    /**
     * Grava a obrigação e refaz os lembretes a partir das antecedências escolhidas.
     *
     * @param obligation obrigação nova, com id 0, ou editada.
     * @param offsets antecedências marcadas no formulário.
     * @return id da obrigação gravada.
     */
    suspend fun save(obligation: Obligation, offsets: Set<ReminderOffset>): Long {
        val saved = database.withTransaction {
            if (obligation.id == 0L) {
                obligation.copy(id = dao.insert(obligation))
            } else {
                dao.update(obligation)
                obligation
            }
        }
        rebuildReminders(saved, offsets)
        return saved.id
    }

    /**
     * Marca a obrigação como paga, recebida ou entregue e cancela os alertas que ainda não dispararam.
     * Quando ela se repete todo mês, a próxima ocorrência é criada com as mesmas antecedências.
     *
     * @param id identificador da obrigação.
     */
    suspend fun settle(id: Long) {
        val data = dao.findWithReminders(id) ?: return
        val obligation = data.obligation
        if (obligation.isCompleted) {
            return
        }
        scheduler.cancel(id)
        database.withTransaction {
            dao.update(
                obligation.copy(
                    status = ObligationStatus.COMPLETED,
                    completedAt = System.currentTimeMillis()
                )
            )
            dao.cancelReminders(id)
        }
        if (obligation.repeatMonthly) {
            createNextOccurrence(obligation, data.reminders.map { it.offset }.toSet())
        }
    }

    /**
     * Volta a obrigação para pendente e agenda de novo os lembretes que ainda estão no futuro.
     *
     * @param id identificador da obrigação.
     */
    suspend fun reopen(id: Long) {
        val data = dao.findWithReminders(id) ?: return
        val reopened = data.obligation.copy(status = ObligationStatus.PENDING, completedAt = null)
        dao.update(reopened)
        rebuildReminders(reopened, data.reminders.map { it.offset }.toSet())
    }

    /**
     * Remove a obrigação, os lembretes e qualquer trabalho agendado no WorkManager.
     *
     * @param id identificador da obrigação.
     */
    suspend fun delete(id: Long) {
        scheduler.cancel(id)
        database.withTransaction {
            dao.deleteReminders(id)
            dao.delete(id)
        }
    }

    /**
     * Registra que o lembrete já apareceu para o usuário.
     *
     * @param reminderId identificador do lembrete.
     */
    suspend fun markFired(reminderId: Long) {
        dao.updateReminderState(reminderId, ReminderState.FIRED)
    }

    /**
     * Cria a ocorrência do mês seguinte, sem duplicar caso ela já exista.
     *
     * @param obligation obrigação que acabou de ser liquidada.
     * @param offsets antecedências que a nova ocorrência deve herdar.
     */
    private suspend fun createNextOccurrence(obligation: Obligation, offsets: Set<ReminderOffset>) {
        val nextDue = obligation.dueAt.toLocalDateTime().plusMonths(1).toEpochMillis()
        if (dao.countMatching(obligation.type, obligation.title, nextDue) > 0) {
            return
        }
        val next = obligation.copy(
            id = 0,
            dueAt = nextDue,
            status = ObligationStatus.PENDING,
            completedAt = null,
            createdAt = System.currentTimeMillis()
        )
        save(next, offsets)
    }

    /**
     * Apaga os lembretes antigos e cria um para cada antecedência. Só vão para o WorkManager
     * os que ainda estão no futuro e pertencem a uma obrigação pendente.
     *
     * @param obligation obrigação já gravada, com id definido.
     * @param offsets antecedências escolhidas.
     */
    private suspend fun rebuildReminders(obligation: Obligation, offsets: Set<ReminderOffset>) {
        scheduler.cancel(obligation.id)
        val now = System.currentTimeMillis()
        val due = obligation.dueAt.toLocalDateTime()
        val reminders = offsets.map { offset ->
            val triggerAt = due.minusDays(offset.days).toEpochMillis()
            val active = !obligation.isCompleted && triggerAt > now
            Reminder(
                obligationId = obligation.id,
                offset = offset,
                triggerAt = triggerAt,
                state = if (active) ReminderState.SCHEDULED else ReminderState.CANCELED
            )
        }
        val ids = database.withTransaction {
            dao.deleteReminders(obligation.id)
            dao.insertReminders(reminders)
        }
        reminders.zip(ids)
            .filter { (reminder, _) -> reminder.state == ReminderState.SCHEDULED }
            .forEach { (reminder, id) -> scheduler.schedule(reminder.copy(id = id)) }
    }
}
