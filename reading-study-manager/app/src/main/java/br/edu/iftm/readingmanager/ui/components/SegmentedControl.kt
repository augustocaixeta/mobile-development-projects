package br.edu.iftm.readingmanager.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import br.edu.iftm.readingmanager.ui.theme.ReadingTheme

private val TrackShape = RoundedCornerShape(14.dp)
private val SegmentShape = RoundedCornerShape(11.dp)

/**
 * Controle segmentado do Figma, com os segmentos dividindo a largura igualmente.
 *
 * @param labels textos dos segmentos.
 * @param selectedIndex posição do segmento ativo.
 * @param onSelect recebe a posição do segmento tocado.
 * @param modifier modificador aplicado ao controle.
 */
@Composable
fun SegmentedControl(
    labels: List<String>,
    selectedIndex: Int,
    onSelect: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = ReadingTheme.colors
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(TrackShape)
            .background(colors.surface)
            .padding(3.dp)
            .selectableGroup(),
        horizontalArrangement = Arrangement.spacedBy(3.dp)
    ) {
        labels.forEachIndexed { index, label ->
            val selected = index == selectedIndex
            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(SegmentShape)
                    .background(if (selected) colors.surface2 else Color.Transparent)
                    .selectable(selected = selected, role = Role.Tab, onClick = { onSelect(index) })
                    .padding(vertical = 9.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = label,
                    style = ReadingTheme.typography.label,
                    color = if (selected) colors.text else colors.text3,
                    maxLines = 1
                )
            }
        }
    }
}
