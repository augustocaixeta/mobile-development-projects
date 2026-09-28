package br.edu.iftm.readingmanager.ui.components

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import br.edu.iftm.readingmanager.R
import br.edu.iftm.readingmanager.data.BookIcon
import br.edu.iftm.readingmanager.data.BookStatus
import br.edu.iftm.readingmanager.data.Genre
import br.edu.iftm.readingmanager.util.daysUntil
import java.time.LocalDate

object Labels {

    /**
     * Nome do gênero literário exibido nas pílulas e no detalhe.
     *
     * @param genre gênero do livro.
     * @return recurso de texto como Não-ficção.
     */
    @StringRes
    fun genre(genre: Genre): Int = when (genre) {
        Genre.FICTION -> R.string.genre_fiction
        Genre.NON_FICTION -> R.string.genre_non_fiction
        Genre.FANTASY -> R.string.genre_fantasy
        Genre.BIOGRAPHY -> R.string.genre_biography
        Genre.SELF_HELP -> R.string.genre_self_help
        Genre.STUDY -> R.string.genre_study
    }

    /**
     * Nome da situação de leitura com inicial maiúscula.
     *
     * @param status situação do livro.
     * @return recurso de texto como Quero ler.
     */
    @StringRes
    fun status(status: BookStatus): Int = when (status) {
        BookStatus.WANT_TO_READ -> R.string.status_want_to_read
        BookStatus.READING -> R.string.status_reading
        BookStatus.READ -> R.string.status_read
    }

    /**
     * Título em minúsculas da seção que agrupa os livros de uma situação na lista.
     *
     * @param status situação dos livros da seção.
     * @return recurso de texto como quero ler.
     */
    @StringRes
    fun section(status: BookStatus): Int = when (status) {
        BookStatus.WANT_TO_READ -> R.string.section_want_to_read
        BookStatus.READING -> R.string.section_reading
        BookStatus.READ -> R.string.section_read
    }

    /**
     * Ícone escolhido como capa do livro.
     *
     * @param icon ícone gravado no banco.
     * @return recurso de imagem do Material Symbols.
     */
    @DrawableRes
    fun icon(icon: BookIcon): Int = when (icon) {
        BookIcon.MENU_BOOK -> R.drawable.ic_menu_book
        BookIcon.AUTO_STORIES -> R.drawable.ic_auto_stories
        BookIcon.SCHOOL -> R.drawable.ic_school
        BookIcon.SCIENCE -> R.drawable.ic_science
        BookIcon.PSYCHOLOGY -> R.drawable.ic_psychology
        BookIcon.HISTORY_EDU -> R.drawable.ic_history_edu
        BookIcon.FAVORITE -> R.drawable.ic_favorite
    }
}

/**
 * Descreve a distância entre uma data e hoje.
 *
 * @param date data de referência.
 * @param today data atual.
 * @return texto como há 2 dias, hoje, amanhã ou em 5 dias.
 */
@Composable
fun relativeDay(date: LocalDate, today: LocalDate): String {
    val days = today.daysUntil(date)
    return when {
        days == 0 -> stringResource(R.string.relative_today)
        days == 1 -> stringResource(R.string.relative_tomorrow)
        days == -1 -> stringResource(R.string.relative_yesterday)
        days > 1 -> pluralStringResource(R.plurals.relative_in_days, days, days)
        else -> pluralStringResource(R.plurals.relative_days_ago, -days, -days)
    }
}
