package br.edu.iftm.deadlinetracker

import androidx.room.Room
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import br.edu.iftm.deadlinetracker.data.AppDatabase
import br.edu.iftm.deadlinetracker.data.Obligation
import br.edu.iftm.deadlinetracker.data.ObligationDao
import br.edu.iftm.deadlinetracker.data.ObligationType
import br.edu.iftm.deadlinetracker.data.Reminder
import br.edu.iftm.deadlinetracker.data.ReminderOffset
import br.edu.iftm.deadlinetracker.data.ReminderState
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class ObligationDaoTest {

    private lateinit var database: AppDatabase
    private lateinit var dao: ObligationDao

    @Before
    fun openDatabase() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        database = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java).build()
        dao = database.obligationDao()
    }

    @After
    fun closeDatabase() {
        database.close()
    }

    @Test
    fun obligationLoadsItsReminders() = runBlocking {
        val id = dao.insert(bill())
        dao.insertReminders(
            listOf(
                Reminder(obligationId = id, offset = ReminderOffset.ONE_DAY, triggerAt = 1_000),
                Reminder(obligationId = id, offset = ReminderOffset.ON_DUE_DATE, triggerAt = 2_000)
            )
        )

        val data = dao.findWithReminders(id)

        assertEquals("Conta de energia", data?.obligation?.title)
        assertEquals(2, data?.reminders?.size)
    }

    @Test
    fun cancelOnlyAffectsScheduledReminders() = runBlocking {
        val id = dao.insert(bill())
        val ids = dao.insertReminders(
            listOf(
                Reminder(obligationId = id, offset = ReminderOffset.ONE_DAY, triggerAt = 1_000),
                Reminder(
                    obligationId = id,
                    offset = ReminderOffset.ON_DUE_DATE,
                    triggerAt = 2_000,
                    state = ReminderState.FIRED
                )
            )
        )

        dao.cancelReminders(id)

        assertEquals(ReminderState.CANCELED, dao.findReminder(ids[0])?.state)
        assertEquals(ReminderState.FIRED, dao.findReminder(ids[1])?.state)
    }

    @Test
    fun deletingObligationCascadesToReminders() = runBlocking {
        val id = dao.insert(bill())
        val ids = dao.insertReminders(
            listOf(Reminder(obligationId = id, offset = ReminderOffset.ONE_DAY, triggerAt = 1_000))
        )

        dao.delete(id)

        assertNull(dao.find(id))
        assertNull(dao.findReminder(ids[0]))
    }

    @Test
    fun listsOnlyPendingOverdue() = runBlocking {
        dao.insert(bill(dueAt = 500))
        dao.insert(bill(dueAt = 5_000))

        val overdue = dao.listOverdue(limit = 1_000)

        assertEquals(1, overdue.size)
        assertEquals(500L, overdue.first().dueAt)
    }

    private fun bill(dueAt: Long = 10_000) = Obligation(
        type = ObligationType.PAYABLE,
        title = "Conta de energia",
        payee = "CEMIG",
        amountCents = 31_200,
        dueAt = dueAt
    )
}
