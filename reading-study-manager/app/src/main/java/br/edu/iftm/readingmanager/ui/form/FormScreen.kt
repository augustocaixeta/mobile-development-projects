package br.edu.iftm.readingmanager.ui.form

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import br.edu.iftm.readingmanager.R
import br.edu.iftm.readingmanager.ui.components.SegmentedControl
import br.edu.iftm.readingmanager.ui.components.TopBar
import br.edu.iftm.readingmanager.ui.theme.ReadingTheme

private const val SEGMENT_BOOK = 0
private const val SEGMENT_SESSION = 1
private const val SEGMENT_GOAL = 2

/**
 * Novo registro com os segmentos Livro, Sessão e Meta. Ao editar um livro,
 * mostra só os campos do livro.
 *
 * @param onClose fecha o formulário, também chamado depois de salvar.
 * @param bookViewModel estado do segmento Livro, que também indica se é uma edição.
 */
@Composable
fun FormScreen(
    onClose: () -> Unit,
    bookViewModel: BookFormViewModel = viewModel(factory = BookFormViewModel.Factory)
) {
    val bookState by bookViewModel.state.collectAsStateWithLifecycle()
    var segment by rememberSaveable { mutableIntStateOf(SEGMENT_BOOK) }
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(ReadingTheme.colors.bg)
            .statusBarsPadding()
    ) {
        TopBar(
            navigationIcon = painterResource(R.drawable.ic_close),
            navigationDescription = stringResource(R.string.cd_close),
            onNavigate = onClose,
            title = stringResource(if (bookState.editing) R.string.form_edit else R.string.form_new)
        )
        if (!bookState.editing) {
            SegmentedControl(
                labels = listOf(
                    stringResource(R.string.form_segment_book),
                    stringResource(R.string.form_segment_session),
                    stringResource(R.string.form_segment_goal)
                ),
                selectedIndex = segment,
                onSelect = { segment = it },
                modifier = Modifier.padding(start = 16.dp, top = 8.dp, end = 16.dp)
            )
        }
        val content = Modifier.weight(1f)
        when (if (bookState.editing) SEGMENT_BOOK else segment) {
            SEGMENT_SESSION -> SessionForm(onSaved = onClose, modifier = content)
            SEGMENT_GOAL -> GoalForm(onSaved = onClose, modifier = content)
            else -> BookFormSection(viewModel = bookViewModel, onSaved = onClose, modifier = content)
        }
    }
}
