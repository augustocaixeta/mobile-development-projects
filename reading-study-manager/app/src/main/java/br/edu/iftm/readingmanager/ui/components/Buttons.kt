package br.edu.iftm.readingmanager.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import br.edu.iftm.readingmanager.R
import br.edu.iftm.readingmanager.ui.theme.ReadingTheme

private val ButtonShape = RoundedCornerShape(16.dp)
private val FabShape = RoundedCornerShape(18.dp)

/**
 * Botão principal preenchido do Figma, com 52 dp de altura e cantos de 16 dp.
 *
 * @param text rótulo do botão.
 * @param onClick ação executada no toque.
 * @param modifier modificador aplicado ao botão.
 * @param enabled false para bloquear o toque e esmaecer o botão.
 */
@Composable
fun PrimaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true
) {
    val colors = ReadingTheme.colors
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(52.dp)
            .alpha(if (enabled) 1f else 0.4f)
            .clip(ButtonShape)
            .background(colors.text)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = ripple(color = colors.onFill),
                enabled = enabled,
                role = Role.Button,
                onClick = onClick
            )
            .padding(horizontal = 24.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(text = text, style = ReadingTheme.typography.bodyMedium, color = colors.onFill)
    }
}

/**
 * Botão do tipo texto, sem fundo, usado para ações secundárias.
 *
 * @param text rótulo do botão.
 * @param onClick ação executada no toque.
 * @param modifier modificador aplicado ao botão.
 */
@Composable
fun PlainButton(text: String, onClick: () -> Unit, modifier: Modifier = Modifier) {
    val colors = ReadingTheme.colors
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(52.dp)
            .clip(ButtonShape)
            .clickable(role = Role.Button, onClick = onClick)
            .padding(horizontal = 24.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(text = text, style = ReadingTheme.typography.bodyMedium, color = colors.text2)
    }
}

/**
 * Botão flutuante de adicionar, com 56 dp e cantos de 18 dp.
 *
 * @param contentDescription descrição lida pelo leitor de tela.
 * @param onClick ação executada no toque.
 * @param modifier modificador aplicado ao botão.
 */
@Composable
fun AddFab(contentDescription: String, onClick: () -> Unit, modifier: Modifier = Modifier) {
    val colors = ReadingTheme.colors
    Box(
        modifier = modifier
            .size(56.dp)
            .clip(FabShape)
            .background(colors.text)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = ripple(color = colors.onFill),
                role = Role.Button,
                onClick = onClick
            ),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            painter = painterResource(R.drawable.ic_add),
            contentDescription = contentDescription,
            modifier = Modifier.size(28.dp),
            tint = colors.onFill
        )
    }
}

/**
 * Ícone tocável das barras superiores, como voltar, fechar e mais opções.
 *
 * @param painter ícone exibido.
 * @param contentDescription descrição lida pelo leitor de tela.
 * @param onClick ação executada no toque.
 * @param modifier modificador aplicado ao botão.
 * @param size tamanho da área de toque.
 * @param iconSize tamanho do ícone.
 * @param tint cor do ícone.
 */
@Composable
fun BarIconButton(
    painter: Painter,
    contentDescription: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    size: Dp = 48.dp,
    iconSize: Dp = 24.dp,
    tint: Color = ReadingTheme.colors.text
) {
    Box(
        modifier = modifier
            .size(size)
            .clip(CircleShape)
            .clickable(role = Role.Button, onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            painter = painter,
            contentDescription = contentDescription,
            modifier = Modifier.size(iconSize),
            tint = tint
        )
    }
}

/**
 * Área fixa no rodapé das telas, com os botões de ação acima da barra de gestos.
 *
 * @param modifier modificador aplicado à área.
 * @param content botões exibidos, empilhados com 4 dp de espaço.
 */
@Composable
fun BottomActions(modifier: Modifier = Modifier, content: @Composable ColumnScope.() -> Unit) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(ReadingTheme.colors.bg)
            .navigationBarsPadding()
            .padding(start = 16.dp, top = 12.dp, end = 16.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp),
        content = content
    )
}
