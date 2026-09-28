package br.edu.iftm.readingmanager.ui.navigation

import kotlinx.serialization.Serializable

@Serializable
data object Home

@Serializable
data class BookDetail(val bookId: Long)

@Serializable
data class BookForm(val bookId: Long? = null)

@Serializable
data class ReadingSession(val bookId: Long)

@Serializable
data object Performance
