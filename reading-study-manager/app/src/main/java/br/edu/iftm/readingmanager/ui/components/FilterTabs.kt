package br.edu.iftm.readingmanager.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import br.edu.iftm.readingmanager.ui.theme.ReadingTheme

/**
 * Grupo de abas de filtro do Figma, com uma linha fina na base e a aba ativa sublinhada.
 *
 * @param labels textos das abas, na ordem de exibição.
 * @param selectedIndex posição da aba ativa.
 * @param onSelect recebe a posição da aba tocada.
 * @param modifier modificador aplicado ao grupo.
 */
@Composable
fun FilterTabs(
    labels: List<String>,
    selectedIndex: Int,
    onSelect: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .bottomBorder(ReadingTheme.colors.line)
            .selectableGroup(),
        horizontalArrangement = Arrangement.spacedBy(22.dp)
    ) {
        labels.forEachIndexed { index, label ->
            FilterTab(label = label, selected = index == selectedIndex, onClick = { onSelect(index) })
        }
    }
}

/**
 * Uma aba do grupo. Quando ativa, usa peso médio e ganha o sublinhado de 2 dp.
 *
 * @param label texto da aba.
 * @param selected true quando a aba está ativa.
 * @param onClick ação executada no toque.
 */
@Composable
private fun FilterTab(label: String, selected: Boolean, onClick: () -> Unit) {
    val colors = ReadingTheme.colors
    val typography = ReadingTheme.typography
    Box(
        modifier = Modifier
            .selectable(selected = selected, role = Role.Tab, onClick = onClick)
            .then(if (selected) Modifier.bottomBorder(colors.text, 2.dp) else Modifier)
            .padding(bottom = 10.dp)
    ) {
        Text(
            text = label,
            style = if (selected) typography.bodyMedium else typography.body,
            color = if (selected) colors.text else colors.text3,
            maxLines = 1
        )
    }
}
