package br.edu.iftm.readingmanager.ui.theme

import androidx.compose.foundation.text.selection.LocalTextSelectionColors
import androidx.compose.foundation.text.selection.TextSelectionColors
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.ReadOnlyComposable

object ReadingTheme {

    val colors: ReadingColors
        @Composable
        @ReadOnlyComposable
        get() = LocalReadingColors.current

    val typography: ReadingTypography
        @Composable
        @ReadOnlyComposable
        get() = LocalReadingTypography.current
}

/**
 * Tema do Ler+, sempre escuro, com as cores e a tipografia do protótipo no Figma.
 * Também configura o Material 3 para que os componentes nativos sigam a mesma identidade.
 *
 * @param content conteúdo que recebe o tema.
 */
@Composable
fun ReadingTheme(content: @Composable () -> Unit) {
    val colors = DarkPalette
    val selection = TextSelectionColors(
        handleColor = colors.accent,
        backgroundColor = colors.accent.copy(alpha = 0.3f)
    )
    MaterialTheme(
        colorScheme = colors.toColorScheme(),
        typography = materialTypography()
    ) {
        CompositionLocalProvider(
            LocalReadingColors provides colors,
            LocalReadingTypography provides DefaultTypography,
            LocalTextSelectionColors provides selection,
            content = content
        )
    }
}
