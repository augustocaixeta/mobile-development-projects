package br.edu.iftm.readingmanager.ui.detail

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import br.edu.iftm.readingmanager.R
import br.edu.iftm.readingmanager.ui.components.BottomActions
import br.edu.iftm.readingmanager.ui.components.PlainButton
import br.edu.iftm.readingmanager.ui.components.PrimaryButton
import br.edu.iftm.readingmanager.ui.components.TopBar
import br.edu.iftm.readingmanager.ui.components.UnderConstruction
import br.edu.iftm.readingmanager.ui.theme.ReadingTheme

/**
 * Detalhe do livro com o progresso de leitura. Por enquanto só liga a navegação da tela.
 *
 * @param onBack volta para o Início.
 * @param onEdit abre o formulário em modo de edição.
 * @param onContinue abre a sessão de leitura do livro.
 */
@Composable
fun DetailScreen(onBack: () -> Unit, onEdit: () -> Unit, onContinue: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(ReadingTheme.colors.bg)
            .statusBarsPadding()
    ) {
        TopBar(
            navigationIcon = painterResource(R.drawable.ic_arrow_back),
            navigationDescription = stringResource(R.string.cd_back),
            onNavigate = onBack
        )
        UnderConstruction(title = stringResource(R.string.detail_title), modifier = Modifier.weight(1f))
        BottomActions {
            PrimaryButton(text = stringResource(R.string.action_continue_reading), onClick = onContinue)
            PlainButton(text = stringResource(R.string.action_edit_book), onClick = onEdit)
        }
    }
}
