package br.edu.iftm.deadlinetracker.data

import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import androidx.room.Relation

enum class ReminderOffset(val days: Long) {
    ON_DUE_DATE(0),
    ONE_DAY(1),
    THREE_DAYS(3),
    ONE_WEEK(7)
}

enum class ReminderState {
    SCHEDULED,
    FIRED,
    CANCELED
}

@Entity(
    tableName = "reminders",
    foreignKeys = [
        ForeignKey(
            entity = Obligation::class,
            parentColumns = ["id"],
            childColumns = ["obligationId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("obligationId")]
)
data class Reminder(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val obligationId: Long,
    val offset: ReminderOffset,
    val triggerAt: Long,
    val state: ReminderState = ReminderState.SCHEDULED
)

data class ObligationWithReminders(
    @Embedded
    val obligation: Obligation,
    @Relation(parentColumn = "id", entityColumn = "obligationId")
    val reminders: List<Reminder>
)
