package br.edu.iftm.deadlinetracker.data

import androidx.room.Entity
import androidx.room.PrimaryKey

enum class ObligationType {
    PAYABLE,
    RECEIVABLE,
    DEADLINE
}

enum class ObligationStatus {
    PENDING,
    COMPLETED
}

@Entity(tableName = "obligations")
data class Obligation(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val type: ObligationType,
    val title: String,
    val payee: String,
    val amountCents: Long?,
    val dueAt: Long,
    val repeatMonthly: Boolean = false,
    val status: ObligationStatus = ObligationStatus.PENDING,
    val completedAt: Long? = null,
    val createdAt: Long = System.currentTimeMillis()
)

val Obligation.isCompleted: Boolean
    get() = status == ObligationStatus.COMPLETED
