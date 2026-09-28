package br.edu.iftm.readingmanager.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import br.edu.iftm.readingmanager.ui.theme.ReadingTheme

/**
 * Barra superior de 56 dp com o ícone de navegação, um título opcional e as ações à direita.
 *
 * @param navigationIcon ícone de voltar ou fechar.
 * @param navigationDescription descrição do ícone para o leitor de tela.
 * @param onNavigate ação do ícone de navegação.
 * @param modifier modificador aplicado à barra.
 * @param title texto exibido ao lado do ícone, quando houver.
 * @param actions ícones exibidos no lado direito.
 */
@Composable
fun TopBar(
    navigationIcon: Painter,
    navigationDescription: String,
    onNavigate: () -> Unit,
    modifier: Modifier = Modifier,
    title: String? = null,
    actions: @Composable RowScope.() -> Unit = {}
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(56.dp)
            .padding(horizontal = 4.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        BarIconButton(
            painter = navigationIcon,
            contentDescription = navigationDescription,
            onClick = onNavigate
        )
        if (title != null) {
            Text(
                text = title,
                modifier = Modifier.weight(1f),
                style = ReadingTheme.typography.bodyMedium,
                color = ReadingTheme.colors.text,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        } else {
            Spacer(Modifier.weight(1f))
        }
        actions()
    }
}
