package br.edu.iftm.readingmanager.data

/**
 * Aplica uma nova página atual ao livro e ajusta a situação de acordo com ela.
 * Chegar à última página marca o livro como lido, e qualquer página intermediária
 * deixa o livro em leitura, preenchendo o início quando ele ainda não existe.
 *
 * @receiver livro antes da atualização.
 * @param page nova página atual, limitada entre zero e o total do livro.
 * @param now instante da atualização em milissegundos.
 * @return livro com a página e a situação atualizadas.
 */
fun Book.withCurrentPage(page: Int, now: Long): Book {
    val clamped = page.coerceIn(0, totalPages.coerceAtLeast(0))
    return when {
        totalPages > 0 && clamped == totalPages -> copy(
            currentPage = totalPages,
            status = BookStatus.READ,
            startedAt = startedAt ?: now,
            finishedAt = if (isRead) finishedAt ?: now else now
        )
        clamped > 0 -> copy(
            currentPage = clamped,
            status = BookStatus.READING,
            startedAt = startedAt ?: now,
            finishedAt = null
        )
        else -> copy(
            currentPage = 0,
            status = if (isRead) BookStatus.READING else status,
            finishedAt = null
        )
    }
}

/**
 * Muda a situação do livro. Ao começar a leitura, o início é preenchido, e ao marcar como lido
 * o término é registrado e a página atual passa a ser a última.
 *
 * @receiver livro antes da mudança.
 * @param target nova situação escolhida.
 * @param now instante da mudança em milissegundos.
 * @return livro com a nova situação.
 */
fun Book.withStatus(target: BookStatus, now: Long): Book = when (target) {
    BookStatus.WANT_TO_READ -> copy(status = BookStatus.WANT_TO_READ, finishedAt = null)
    BookStatus.READING -> copy(
        status = BookStatus.READING,
        startedAt = startedAt ?: now,
        finishedAt = null
    )
    BookStatus.READ -> copy(
        status = BookStatus.READ,
        currentPage = totalPages,
        startedAt = startedAt ?: now,
        finishedAt = if (isRead) finishedAt ?: now else now
    )
}

/**
 * Monta a sessão que registra o avanço entre duas versões do mesmo livro.
 * Voltar a página não gera sessão, porque é apenas uma correção.
 *
 * @param before livro antes da atualização.
 * @param after livro depois da atualização.
 * @param startedAt instante em que a leitura começou, em milissegundos.
 * @param durationSeconds duração da leitura em segundos.
 * @return sessão com as páginas lidas, ou null quando não houve avanço.
 */
fun progressSession(before: Book, after: Book, startedAt: Long, durationSeconds: Long = 0): Session? {
    if (after.currentPage <= before.currentPage) {
        return null
    }
    return Session(
        bookId = before.id,
        startedAt = startedAt,
        durationSeconds = durationSeconds,
        startPage = before.currentPage,
        endPage = after.currentPage
    )
}
