package br.edu.iftm.readingmanager.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import br.edu.iftm.readingmanager.ui.theme.ReadingTheme

/**
 * Linha de detalhe do Figma, com o rótulo à esquerda e o valor à direita em 48 dp de altura.
 *
 * @param label nome do dado.
 * @param value valor formatado.
 * @param modifier modificador aplicado à linha.
 * @param valueColor cor do valor.
 */
@Composable
fun DetailRow(
    label: String,
    value: String,
    modifier: Modifier = Modifier,
    valueColor: Color = ReadingTheme.colors.text
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(48.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            modifier = Modifier.weight(1f),
            style = ReadingTheme.typography.body,
            color = ReadingTheme.colors.text2,
            maxLines = 1
        )
        Text(text = value, style = ReadingTheme.typography.bodyMedium, color = valueColor, maxLines = 1)
    }
}
