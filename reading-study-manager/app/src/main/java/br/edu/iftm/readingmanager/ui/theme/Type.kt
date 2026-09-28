package br.edu.iftm.readingmanager.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import br.edu.iftm.readingmanager.R

val Grotesk = FontFamily(
    Font(R.font.schibsted_grotesk_regular, FontWeight.Normal),
    Font(R.font.schibsted_grotesk_medium, FontWeight.Medium),
    Font(R.font.schibsted_grotesk_semibold, FontWeight.SemiBold)
)

val PlexMono = FontFamily(
    Font(R.font.ibm_plex_mono_regular, FontWeight.Normal),
    Font(R.font.ibm_plex_mono_medium, FontWeight.Medium)
)

@Immutable
data class ReadingTypography(
    val display: TextStyle,
    val title: TextStyle,
    val body: TextStyle,
    val bodyMedium: TextStyle,
    val caption: TextStyle,
    val label: TextStyle,
    val overline: TextStyle,
    val number: TextStyle,
    val value: TextStyle,
    val monoSmall: TextStyle
)

val DefaultTypography = ReadingTypography(
    display = TextStyle(
        fontFamily = PlexMono,
        fontWeight = FontWeight.Normal,
        fontSize = 34.sp,
        lineHeight = 40.sp,
        letterSpacing = (-0.03).em
    ),
    title = TextStyle(
        fontFamily = Grotesk,
        fontWeight = FontWeight.SemiBold,
        fontSize = 22.sp,
        lineHeight = 28.sp,
        letterSpacing = (-0.009).em
    ),
    body = TextStyle(
        fontFamily = Grotesk,
        fontWeight = FontWeight.Normal,
        fontSize = 15.sp,
        lineHeight = 20.sp
    ),
    bodyMedium = TextStyle(
        fontFamily = Grotesk,
        fontWeight = FontWeight.Medium,
        fontSize = 15.sp,
        lineHeight = 20.sp
    ),
    caption = TextStyle(
        fontFamily = Grotesk,
        fontWeight = FontWeight.Normal,
        fontSize = 13.sp,
        lineHeight = 18.sp
    ),
    label = TextStyle(
        fontFamily = Grotesk,
        fontWeight = FontWeight.Medium,
        fontSize = 13.sp,
        lineHeight = 16.sp
    ),
    overline = TextStyle(
        fontFamily = PlexMono,
        fontWeight = FontWeight.Normal,
        fontSize = 11.sp,
        lineHeight = 14.sp,
        letterSpacing = 0.06.em
    ),
    number = TextStyle(
        fontFamily = PlexMono,
        fontWeight = FontWeight.Medium,
        fontSize = 20.sp,
        lineHeight = 24.sp,
        letterSpacing = (-0.02).em
    ),
    value = TextStyle(
        fontFamily = PlexMono,
        fontWeight = FontWeight.Normal,
        fontSize = 14.sp,
        lineHeight = 18.sp
    ),
    monoSmall = TextStyle(
        fontFamily = PlexMono,
        fontWeight = FontWeight.Normal,
        fontSize = 12.sp,
        lineHeight = 16.sp
    )
)

val LocalReadingTypography = staticCompositionLocalOf { DefaultTypography }

/**
 * Aplica a Schibsted Grotesk em todos os estilos do Material 3, para que diálogos e seletores
 * nativos usem a mesma fonte do restante do app.
 *
 * @return tipografia do Material 3 com a fonte do Figma.
 */
fun materialTypography(): Typography {
    val base = Typography()
    return Typography(
        displayLarge = base.displayLarge.copy(fontFamily = Grotesk),
        displayMedium = base.displayMedium.copy(fontFamily = Grotesk),
        displaySmall = base.displaySmall.copy(fontFamily = Grotesk),
        headlineLarge = base.headlineLarge.copy(fontFamily = Grotesk),
        headlineMedium = base.headlineMedium.copy(fontFamily = Grotesk),
        headlineSmall = base.headlineSmall.copy(fontFamily = Grotesk),
        titleLarge = base.titleLarge.copy(fontFamily = Grotesk),
        titleMedium = base.titleMedium.copy(fontFamily = Grotesk),
        titleSmall = base.titleSmall.copy(fontFamily = Grotesk),
        bodyLarge = base.bodyLarge.copy(fontFamily = Grotesk),
        bodyMedium = base.bodyMedium.copy(fontFamily = Grotesk),
        bodySmall = base.bodySmall.copy(fontFamily = Grotesk),
        labelLarge = base.labelLarge.copy(fontFamily = Grotesk),
        labelMedium = base.labelMedium.copy(fontFamily = Grotesk),
        labelSmall = base.labelSmall.copy(fontFamily = Grotesk)
    )
}
