package br.edu.iftm.readingmanager.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import br.edu.iftm.readingmanager.ui.theme.ReadingTheme

private val CardShape = RoundedCornerShape(20.dp)

/**
 * Diálogo em cartão, com o fundo de superfície e os cantos de 20 dp dos cartões do Figma.
 *
 * @param title título exibido no topo do cartão.
 * @param onDismiss chamado ao tocar fora do cartão ou no botão voltar do sistema.
 * @param modifier modificador aplicado ao cartão.
 * @param message texto de apoio abaixo do título, quando houver.
 * @param content campos e botões do diálogo, empilhados com 16 dp de espaço.
 */
@Composable
fun CardDialog(
    title: String,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
    message: String? = null,
    content: @Composable ColumnScope.() -> Unit
) {
    val colors = ReadingTheme.colors
    val typography = ReadingTheme.typography
    Dialog(onDismissRequest = onDismiss, properties = DialogProperties(usePlatformDefaultWidth = false)) {
        Column(
            modifier = modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .clip(CardShape)
                .background(colors.surface)
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = title,
                style = typography.bodyMedium.copy(fontSize = 17.sp, lineHeight = 22.sp),
                color = colors.text
            )
            if (message != null) {
                Text(text = message, style = typography.caption, color = colors.text2)
            }
            content()
        }
    }
}
