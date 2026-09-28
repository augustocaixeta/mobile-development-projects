package br.edu.iftm.readingmanager.ui.components

import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Desenha uma linha na base do elemento, como as bordas inferiores dos frames do Figma.
 *
 * @receiver modificador atual.
 * @param color cor da linha.
 * @param thickness espessura da linha.
 * @return modificador com a linha aplicada.
 */
fun Modifier.bottomBorder(color: Color, thickness: Dp = 1.dp): Modifier = drawBehind {
    val stroke = thickness.toPx()
    drawRect(
        color = color,
        topLeft = Offset(0f, size.height - stroke),
        size = Size(size.width, stroke)
    )
}
