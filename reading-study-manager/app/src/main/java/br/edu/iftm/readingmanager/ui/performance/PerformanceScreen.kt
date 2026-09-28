package br.edu.iftm.readingmanager.ui.performance

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
 * Desempenho de leitura por semana, mês e ano. Por enquanto só liga a navegação da tela.
 *
 * @param onBack volta para o Início.
 */
@Composable
fun PerformanceScreen(onBack: () -> Unit) {
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
        UnderConstruction(title = stringResource(R.string.performance_title))
    }
}
