package br.edu.iftm.readingmanager.data

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "sessions",
    foreignKeys = [
        ForeignKey(
            entity = Book::class,
            parentColumns = ["id"],
            childColumns = ["bookId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("bookId"), Index("startedAt")]
)
data class Session(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val bookId: Long,
    val startedAt: Long,
    val durationSeconds: Long = 0,
    val startPage: Int,
    val endPage: Int
)

val Session.pagesRead: Int
    get() = (endPage - startPage).coerceAtLeast(0)
