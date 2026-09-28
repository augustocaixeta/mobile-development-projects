package br.edu.iftm.readingmanager.ui.form

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import br.edu.iftm.readingmanager.R
import br.edu.iftm.readingmanager.ui.components.TopBar
import br.edu.iftm.readingmanager.ui.components.UnderConstruction
import br.edu.iftm.readingmanager.ui.theme.ReadingTheme

/**
 * Formulário de novo registro. Por enquanto só liga a navegação da tela.
 *
 * @param onClose fecha o formulário.
 */
@Composable
fun FormScreen(onClose: () -> Unit) {
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
            title = stringResource(R.string.form_new)
        )
        UnderConstruction(title = stringResource(R.string.form_new))
    }
}
