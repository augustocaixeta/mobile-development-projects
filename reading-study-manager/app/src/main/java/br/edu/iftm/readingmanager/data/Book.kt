package br.edu.iftm.readingmanager.data

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

enum class BookStatus {
    WANT_TO_READ,
    READING,
    READ
}

enum class Genre {
    FICTION,
    NON_FICTION,
    FANTASY,
    BIOGRAPHY,
    SELF_HELP,
    STUDY
}

enum class BookIcon {
    MENU_BOOK,
    AUTO_STORIES,
    SCHOOL,
    SCIENCE,
    PSYCHOLOGY,
    HISTORY_EDU,
    FAVORITE
}

@Entity(
    tableName = "books",
    indices = [Index("status"), Index("genre")]
)
data class Book(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String,
    val author: String,
    val totalPages: Int,
    val genre: Genre,
    val icon: BookIcon = BookIcon.MENU_BOOK,
    val status: BookStatus = BookStatus.WANT_TO_READ,
    val currentPage: Int = 0,
    val deadline: Long? = null,
    val startedAt: Long? = null,
    val finishedAt: Long? = null,
    val createdAt: Long = System.currentTimeMillis()
)

val Book.isRead: Boolean
    get() = status == BookStatus.READ

val Book.progressPercent: Int
    get() = when {
        isRead -> 100
        totalPages <= 0 -> 0
        else -> (currentPage.coerceIn(0, totalPages) * 100) / totalPages
    }

val Book.pagesLeft: Int
    get() = if (isRead) 0 else (totalPages - currentPage).coerceAtLeast(0)
