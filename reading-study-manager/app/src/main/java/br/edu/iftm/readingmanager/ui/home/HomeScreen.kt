package br.edu.iftm.readingmanager.ui.home

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import br.edu.iftm.readingmanager.R
import br.edu.iftm.readingmanager.ui.components.AddFab
import br.edu.iftm.readingmanager.ui.components.UnderConstruction
import br.edu.iftm.readingmanager.ui.theme.ReadingTheme

/**
 * Tela inicial com o catálogo de livros. Por enquanto só liga a navegação da tela.
 *
 * @param onOpenBook abre o detalhe do livro tocado.
 * @param onAdd abre o formulário de novo registro.
 * @param onOpenPerformance abre a tela de desempenho.
 */
@Composable
fun HomeScreen(onOpenBook: (Long) -> Unit, onAdd: () -> Unit, onOpenPerformance: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(ReadingTheme.colors.bg)
            .statusBarsPadding()
    ) {
        UnderConstruction(title = stringResource(R.string.app_name), modifier = Modifier.padding(top = 12.dp))
        AddFab(
            contentDescription = stringResource(R.string.cd_add),
            onClick = onAdd,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .navigationBarsPadding()
                .padding(end = 20.dp)
        )
    }
}
