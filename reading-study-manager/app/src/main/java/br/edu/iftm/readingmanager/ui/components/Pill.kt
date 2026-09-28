package br.edu.iftm.readingmanager.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import br.edu.iftm.readingmanager.ui.theme.ReadingTheme

/**
 * Pílula selecionável do Figma. Selecionada fica preenchida, e sem seleção fica só com o contorno.
 *
 * @param text rótulo da pílula.
 * @param selected true quando está marcada.
 * @param onClick ação executada no toque.
 * @param modifier modificador aplicado à pílula.
 */
@Composable
fun Pill(text: String, selected: Boolean, onClick: () -> Unit, modifier: Modifier = Modifier) {
    val colors = ReadingTheme.colors
    Box(
        modifier = modifier
            .clip(CircleShape)
            .background(if (selected) colors.text else colors.bg)
            .then(if (selected) Modifier else Modifier.border(1.dp, colors.line, CircleShape))
            .selectable(
                selected = selected,
                interactionSource = remember { MutableInteractionSource() },
                indication = ripple(color = if (selected) colors.onFill else colors.text),
                role = Role.RadioButton,
                onClick = onClick
            )
            .padding(horizontal = 14.dp, vertical = 8.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            style = ReadingTheme.typography.label,
            color = if (selected) colors.onFill else colors.text2,
            maxLines = 1
        )
    }
}

/**
 * Pílula de situação exibida no fim de uma linha, como a de concluído em verde.
 *
 * @param text rótulo da situação.
 * @param icon ícone à esquerda do texto.
 * @param modifier modificador aplicado à pílula.
 * @param color cor do ícone e do texto.
 */
@Composable
fun StatusPill(
    text: String,
    icon: Painter,
    modifier: Modifier = Modifier,
    color: Color = ReadingTheme.colors.positive
) {
    Row(
        modifier = modifier
            .clip(CircleShape)
            .background(ReadingTheme.colors.surface)
            .padding(start = 10.dp, top = 6.dp, end = 12.dp, bottom = 6.dp),
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(painter = icon, contentDescription = null, modifier = Modifier.size(16.dp), tint = color)
        Text(text = text, style = ReadingTheme.typography.label, color = color, maxLines = 1)
    }
}
