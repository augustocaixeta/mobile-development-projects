package br.edu.iftm.readingmanager.ui.theme

import androidx.compose.material3.ColorScheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

@Immutable
data class ReadingColors(
    val bg: Color,
    val surface: Color,
    val surface2: Color,
    val line: Color,
    val text: Color,
    val text2: Color,
    val text3: Color,
    val positive: Color,
    val accent: Color,
    val onFill: Color
)

val DarkPalette = ReadingColors(
    bg = Color(0xFF0E0E0D),
    surface = Color(0xFF171716),
    surface2 = Color(0xFF22221F),
    line = Color(0xFF2B2B28),
    text = Color(0xFFEEEDE7),
    text2 = Color(0xFFA09E97),
    text3 = Color(0xFF64635E),
    positive = Color(0xFF8FD4A3),
    accent = Color(0xFFFF6B3D),
    onFill = Color(0xFF0E0E0D)
)

val LocalReadingColors = staticCompositionLocalOf { DarkPalette }

/**
 * Traduz a paleta do Figma para o esquema do Material 3, usado pelos componentes nativos
 * como os seletores de data e hora.
 *
 * @receiver paleta do app.
 * @return esquema escuro com os mesmos tokens.
 */
fun ReadingColors.toColorScheme(): ColorScheme = darkColorScheme(
    primary = text,
    onPrimary = onFill,
    primaryContainer = surface2,
    onPrimaryContainer = text,
    inversePrimary = accent,
    secondary = text2,
    onSecondary = onFill,
    secondaryContainer = surface2,
    onSecondaryContainer = text,
    tertiary = positive,
    onTertiary = onFill,
    background = bg,
    onBackground = text,
    surface = bg,
    onSurface = text,
    surfaceVariant = surface2,
    onSurfaceVariant = text2,
    surfaceTint = bg,
    inverseSurface = text,
    inverseOnSurface = onFill,
    error = accent,
    onError = onFill,
    outline = line,
    outlineVariant = line,
    surfaceContainerLowest = bg,
    surfaceContainerLow = surface,
    surfaceContainer = surface,
    surfaceContainerHigh = surface2,
    surfaceContainerHighest = surface2
)
