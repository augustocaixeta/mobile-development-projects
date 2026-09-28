package br.edu.iftm.readingmanager.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import br.edu.iftm.readingmanager.ui.theme.ReadingTheme

/**
 * Barra de progresso fina de 2 dp, com o trilho na cor de linha e o preenchimento em texto.
 *
 * @param progress fração concluída entre 0 e 1.
 * @param modifier modificador aplicado à barra.
 */
@Composable
fun ProgressLine(progress: Float, modifier: Modifier = Modifier) {
    val colors = ReadingTheme.colors
    Box(
        modifier = modifier
            .height(2.dp)
            .background(colors.line)
    ) {
        Box(
            modifier = Modifier
                .fillMaxHeight()
                .fillMaxWidth(progress.coerceIn(0f, 1f))
                .background(colors.text)
        )
    }
}
