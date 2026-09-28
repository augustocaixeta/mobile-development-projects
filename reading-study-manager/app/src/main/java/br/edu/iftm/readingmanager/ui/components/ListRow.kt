package br.edu.iftm.readingmanager.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import br.edu.iftm.readingmanager.ui.theme.ReadingTheme

/**
 * Linha de leitura ou estudo do Figma: coluna inicial, título com subtítulo e um valor no fim.
 *
 * @param title texto principal, com até duas linhas.
 * @param subtitle texto secundário, com uma linha.
 * @param onClick ação executada no toque.
 * @param modifier modificador aplicado à linha.
 * @param leading conteúdo da coluna inicial.
 * @param trailing conteúdo alinhado à direita.
 */
@Composable
fun ListRow(
    title: String,
    subtitle: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    leading: @Composable ColumnScope.() -> Unit,
    trailing: @Composable () -> Unit
) {
    val colors = ReadingTheme.colors
    val typography = ReadingTheme.typography
    Row(
        modifier = modifier
            .fillMaxWidth()
            .bottomBorder(colors.line)
            .clickable(onClick = onClick)
            .padding(vertical = 14.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(2.dp), content = leading)
        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(
                text = title,
                style = typography.bodyMedium,
                color = colors.text,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = subtitle,
                style = typography.caption,
                color = colors.text2,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
        trailing()
    }
}

/**
 * Coluna inicial com o dia em destaque e o dia da semana abaixo, como nas linhas do Figma.
 *
 * @receiver coluna inicial da linha.
 * @param day dia com dois dígitos.
 * @param weekday dia da semana abreviado.
 * @param color cor do dia, que indica urgência.
 */
@Composable
fun ColumnScope.DateLeading(day: String, weekday: String, color: Color = ReadingTheme.colors.text) {
    Text(text = day, style = ReadingTheme.typography.number, color = color)
    Text(text = weekday, style = ReadingTheme.typography.overline, color = ReadingTheme.colors.text3)
}

/**
 * Valor em mono com a situação abaixo, alinhados à direita da linha.
 *
 * @param value valor principal.
 * @param status texto da situação.
 * @param modifier modificador aplicado ao bloco.
 * @param valueColor cor do valor, que reflete a natureza do dado.
 * @param statusColor cor da situação, que reflete a urgência.
 */
@Composable
fun TrailingValue(
    value: String,
    status: String,
    modifier: Modifier = Modifier,
    valueColor: Color = ReadingTheme.colors.text,
    statusColor: Color = ReadingTheme.colors.text3
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.End,
        verticalArrangement = Arrangement.spacedBy(2.dp)
    ) {
        Text(text = value, style = ReadingTheme.typography.value, color = valueColor, maxLines = 1)
        Text(text = status, style = ReadingTheme.typography.caption, color = statusColor, maxLines = 1)
    }
}

/**
 * Cabeçalho de seção em mono minúsculo, como agora e próximos 7 dias.
 *
 * @param text título da seção.
 * @param modifier modificador aplicado ao cabeçalho.
 */
@Composable
fun SectionHeader(text: String, modifier: Modifier = Modifier) {
    Text(
        text = text,
        modifier = modifier
            .fillMaxWidth()
            .padding(top = 20.dp, bottom = 2.dp),
        style = ReadingTheme.typography.overline,
        color = ReadingTheme.colors.text3
    )
}
