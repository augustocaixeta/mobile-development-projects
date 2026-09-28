package br.edu.iftm.readingmanager.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import br.edu.iftm.readingmanager.ui.theme.ReadingTheme

/**
 * Campo numérico grande em fonte mono, com o cursor na cor de destaque.
 * Aceita apenas dígitos e limita a quantidade digitada.
 *
 * @param label rótulo acima do número.
 * @param value texto atual do campo.
 * @param onValueChange recebe o texto já filtrado.
 * @param modifier modificador aplicado ao campo.
 * @param error mensagem exibida no lugar do rótulo quando o valor é inválido.
 * @param placeholder texto mostrado com o campo vazio.
 * @param maxDigits quantidade máxima de dígitos.
 * @param imeAction ação da tecla de confirmação do teclado.
 */
@Composable
fun LargeNumberField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    error: String? = null,
    placeholder: String = "0",
    maxDigits: Int = 5,
    imeAction: ImeAction = ImeAction.Next
) {
    val colors = ReadingTheme.colors
    val typography = ReadingTheme.typography
    Column(modifier = modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(4.dp)) {
        FieldLabel(label = label, error = error)
        BasicTextField(
            value = value,
            onValueChange = { input -> onValueChange(input.filter(Char::isDigit).take(maxDigits)) },
            modifier = Modifier.fillMaxWidth(),
            textStyle = typography.display.copy(color = colors.text),
            cursorBrush = SolidColor(colors.accent),
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number, imeAction = imeAction),
            decorationBox = { inner ->
                Box {
                    if (value.isEmpty()) {
                        Text(text = placeholder, style = typography.display, color = colors.text3)
                    }
                    inner()
                }
            }
        )
    }
}

/**
 * Campo de texto do Figma, com rótulo pequeno e uma linha na base que fica laranja em caso de erro.
 *
 * @param label rótulo acima do texto.
 * @param value texto atual do campo.
 * @param onValueChange recebe o texto digitado.
 * @param modifier modificador aplicado ao campo.
 * @param placeholder texto mostrado com o campo vazio.
 * @param error mensagem exibida no lugar do rótulo quando o valor é inválido.
 * @param capitalization regra de maiúsculas do teclado.
 * @param imeAction ação da tecla de confirmação do teclado.
 * @param maxLength quantidade máxima de caracteres.
 */
@Composable
fun LineTextField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    placeholder: String = "",
    error: String? = null,
    capitalization: KeyboardCapitalization = KeyboardCapitalization.Sentences,
    imeAction: ImeAction = ImeAction.Next,
    maxLength: Int = 120
) {
    val colors = ReadingTheme.colors
    val typography = ReadingTheme.typography
    FieldBox(hasError = error != null, modifier = modifier) {
        FieldLabel(label = label, error = error)
        BasicTextField(
            value = value,
            onValueChange = { input -> onValueChange(input.take(maxLength)) },
            modifier = Modifier.fillMaxWidth(),
            textStyle = typography.body.copy(color = colors.text),
            cursorBrush = SolidColor(colors.accent),
            singleLine = true,
            keyboardOptions = KeyboardOptions(capitalization = capitalization, imeAction = imeAction),
            decorationBox = { inner ->
                Box {
                    if (value.isEmpty()) {
                        Text(text = placeholder, style = typography.body, color = colors.text3, maxLines = 1)
                    }
                    inner()
                }
            }
        )
    }
}

/**
 * Campo que abre um seletor ao ser tocado, como os de data e horário do Figma.
 *
 * @param label rótulo acima do valor.
 * @param value valor formatado, ou null quando nada foi escolhido.
 * @param placeholder texto mostrado quando não há valor.
 * @param icon ícone exibido à direita.
 * @param onClick ação que abre o seletor.
 * @param modifier modificador aplicado ao campo.
 * @param error mensagem exibida no lugar do rótulo quando o valor é inválido.
 * @param enabled false para bloquear o toque.
 */
@Composable
fun PickerField(
    label: String,
    value: String?,
    placeholder: String,
    icon: Painter,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    error: String? = null,
    enabled: Boolean = true
) {
    val colors = ReadingTheme.colors
    FieldBox(
        hasError = error != null,
        modifier = modifier.clickable(enabled = enabled, role = Role.Button, onClick = onClick)
    ) {
        FieldLabel(label = label, error = error)
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = value ?: placeholder,
                modifier = Modifier.weight(1f),
                style = ReadingTheme.typography.body,
                color = if (value != null && enabled) colors.text else colors.text3,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Icon(painter = icon, contentDescription = null, modifier = Modifier.size(18.dp), tint = colors.text3)
        }
    }
}

/**
 * Rótulo dos campos. Em caso de erro, mostra a mensagem na cor de destaque.
 *
 * @param label rótulo normal do campo.
 * @param error mensagem de erro, ou null quando o campo é válido.
 */
@Composable
fun FieldLabel(label: String, error: String?) {
    val colors = ReadingTheme.colors
    Text(
        text = error ?: label,
        style = ReadingTheme.typography.caption,
        color = if (error != null) colors.accent else colors.text3
    )
}

/**
 * Moldura comum dos campos de linha, com os espaçamentos e a borda inferior do Figma.
 *
 * @param hasError true para pintar a linha na cor de destaque.
 * @param modifier modificador aplicado à moldura.
 * @param content rótulo e valor do campo.
 */
@Composable
private fun FieldBox(hasError: Boolean, modifier: Modifier, content: @Composable ColumnScope.() -> Unit) {
    val colors = ReadingTheme.colors
    Column(
        modifier = modifier
            .fillMaxWidth()
            .bottomBorder(if (hasError) colors.accent else colors.line)
            .padding(top = 4.dp, bottom = 12.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp),
        content = content
    )
}
