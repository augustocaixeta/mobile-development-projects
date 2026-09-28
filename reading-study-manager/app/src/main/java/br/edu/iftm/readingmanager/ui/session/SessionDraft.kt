package br.edu.iftm.readingmanager.ui.session

import br.edu.iftm.readingmanager.data.Book
import br.edu.iftm.readingmanager.data.Session
import br.edu.iftm.readingmanager.data.withCurrentPage

private const val MARK_STEP = 10
private const val MARK_ROUNDING = 5
private const val SUGGESTED_MARKS = 3
private const val MIN_SESSION_SECONDS = 60L

data class SessionDraft(
    val startedAt: Long,
    val accumulatedSeconds: Long = 0,
    val resumedAt: Long? = startedAt,
    val marks: List<Int> = emptyList(),
    val done: Set<Int> = emptySet(),
    val prepared: Boolean = false
)

val SessionDraft.isRunning: Boolean
    get() = resumedAt != null

/**
 * Calcula o tempo de leitura da sessão, somando o que já passou antes da última pausa.
 *
 * @receiver rascunho da sessão.
 * @param now instante atual em milissegundos.
 * @return segundos lidos até agora.
 */
fun SessionDraft.elapsedSeconds(now: Long): Long {
    val running = resumedAt?.let { ((now - it) / 1000).coerceAtLeast(0) } ?: 0
    return accumulatedSeconds + running
}

/**
 * Pausa o cronômetro guardando o tempo lido até agora.
 *
 * @receiver rascunho da sessão.
 * @param now instante da pausa em milissegundos.
 * @return rascunho pausado, ou o mesmo quando já estava pausado.
 */
fun SessionDraft.paused(now: Long): SessionDraft =
    if (isRunning) copy(accumulatedSeconds = elapsedSeconds(now), resumedAt = null) else this

/**
 * Retoma o cronômetro a partir do instante informado.
 *
 * @receiver rascunho da sessão.
 * @param now instante da retomada em milissegundos.
 * @return rascunho em andamento, ou o mesmo quando já estava rodando.
 */
fun SessionDraft.resumed(now: Long): SessionDraft =
    if (isRunning) this else copy(resumedAt = now)

/**
 * Página mais adiantada da sessão: a maior marcação concluída ou a página em que ela começou.
 *
 * @receiver rascunho da sessão.
 * @param startPage página do livro quando a sessão começou.
 * @return página alcançada até agora.
 */
fun SessionDraft.reachedPage(startPage: Int): Int = maxOf(startPage, done.maxOrNull() ?: 0)

/**
 * Próxima marcação ainda não concluída depois da página alcançada.
 *
 * @receiver rascunho da sessão.
 * @param startPage página do livro quando a sessão começou.
 * @return página da próxima marcação, ou null quando não há nenhuma pendente.
 */
fun SessionDraft.nextMark(startPage: Int): Int? {
    val reached = reachedPage(startPage)
    return marks.filter { it > reached && it !in done }.minOrNull()
}

/**
 * Marca ou desmarca uma página como alcançada. Marcar uma página conclui também as anteriores,
 * e desmarcar volta a leitura para antes dela, desfazendo as posteriores.
 *
 * @receiver rascunho da sessão.
 * @param page página da marcação tocada.
 * @return rascunho com as marcações atualizadas.
 */
fun SessionDraft.toggled(page: Int): SessionDraft =
    if (page in done) {
        copy(done = done.filter { it < page }.toSet())
    } else {
        copy(done = done + marks.filter { it <= page } + page)
    }

/**
 * Inclui uma nova marcação, mantendo a lista em ordem crescente e sem repetições.
 *
 * @receiver rascunho da sessão.
 * @param page página da nova marcação.
 * @return rascunho com a marcação incluída.
 */
fun SessionDraft.withMark(page: Int): SessionDraft = copy(marks = (marks + page).distinct().sorted())

/**
 * Sugere as primeiras marcações da sessão, de dez em dez páginas a partir da atual,
 * arredondadas para múltiplos de cinco e sem passar do total do livro.
 *
 * @param startPage página do livro quando a sessão começou.
 * @param totalPages total de páginas do livro.
 * @return até três páginas sugeridas em ordem crescente.
 */
fun suggestedMarks(startPage: Int, totalPages: Int): List<Int> =
    (1..SUGGESTED_MARKS)
        .map { step ->
            val target = startPage + step * MARK_STEP
            val rounded = (target + MARK_ROUNDING - 1) / MARK_ROUNDING * MARK_ROUNDING
            rounded.coerceAtMost(totalPages)
        }
        .filter { it > startPage }
        .distinct()

/**
 * Formata a duração no relógio da sessão, como 12:45 ou 1:05:30.
 *
 * @param seconds duração em segundos.
 * @return texto com minutos e segundos, e as horas quando houver.
 */
fun sessionClock(seconds: Long): String {
    val safe = seconds.coerceAtLeast(0)
    val hours = safe / 3600
    val minutes = safe % 3600 / 60
    val rest = safe % 60
    return if (hours > 0) {
        "%d:%02d:%02d".format(hours, minutes, rest)
    } else {
        "%02d:%02d".format(minutes, rest)
    }
}

/**
 * Monta a sessão que será gravada ao finalizar a leitura. Sessões sem avanço de página
 * só são registradas quando duram pelo menos um minuto.
 *
 * @param before livro como está no banco no momento de finalizar.
 * @param after livro já com a página final aplicada.
 * @param startedAt instante em que a sessão começou.
 * @param durationSeconds tempo lido em segundos.
 * @return sessão a gravar, ou null quando não houve leitura.
 */
fun finishedSession(before: Book, after: Book, startedAt: Long, durationSeconds: Long): Session? {
    if (after.currentPage <= before.currentPage && durationSeconds < MIN_SESSION_SECONDS) {
        return null
    }
    return Session(
        bookId = before.id,
        startedAt = startedAt,
        durationSeconds = durationSeconds,
        startPage = before.currentPage,
        endPage = maxOf(before.currentPage, after.currentPage)
    )
}

/**
 * Aplica a página final da sessão ao livro.
 *
 * @receiver livro como está no banco.
 * @param page página final informada pelo leitor.
 * @param now instante em que a sessão terminou.
 * @return livro atualizado, que só muda quando a página é diferente da atual.
 */
fun Book.withFinalPage(page: Int, now: Long): Book =
    if (page == currentPage) this else withCurrentPage(page, now)
