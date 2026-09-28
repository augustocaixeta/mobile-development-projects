package br.edu.iftm.readingmanager.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import br.edu.iftm.readingmanager.R
import br.edu.iftm.readingmanager.ui.theme.ReadingTheme

/**
 * Conteúdo provisório das telas que ainda não foram implementadas.
 *
 * @param title nome da tela.
 * @param modifier modificador aplicado ao bloco.
 */
@Composable
fun UnderConstruction(title: String, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(text = title, style = ReadingTheme.typography.title, color = ReadingTheme.colors.text)
        Text(
            text = stringResource(R.string.under_construction),
            style = ReadingTheme.typography.caption,
            color = ReadingTheme.colors.text2
        )
    }
}
