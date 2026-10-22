package org.zotero.android.uicomponents.library

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.zotero.android.uicomponents.theme.CustomTheme

internal object LibraryMetrics {
    val pageInset = 20.dp
    val groupGap = 28.dp
    val groupRadius = 14.dp
    val groupShape = RoundedCornerShape(groupRadius)
    val minimumTouchTarget = 48.dp
    val minimumRowHeight = 56.dp
}

internal val LibraryLightColors = lightColorScheme(
    primary = Color(0xFF0066CC),
    onPrimary = Color.White,
    primaryContainer = Color(0xFFE6F0FF),
    onPrimaryContainer = Color(0xFF004C99),
    secondary = Color(0xFF626268),
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFE6F0FF),
    onSecondaryContainer = Color(0xFF004C99),
    tertiary = Color(0xFF626268),
    onTertiary = Color.White,
    tertiaryContainer = Color(0xFFE8E8ED),
    onTertiaryContainer = Color(0xFF1C1C1E),
    background = Color(0xFFF2F2F7),
    onBackground = Color(0xFF1C1C1E),
    surface = Color.White,
    onSurface = Color(0xFF1C1C1E),
    surfaceVariant = Color(0xFFE8E8ED),
    onSurfaceVariant = Color(0xFF626268),
    surfaceContainerLowest = Color.White,
    surfaceContainerLow = Color(0xFFF8F8FA),
    surfaceContainer = Color(0xFFF2F2F7),
    surfaceContainerHigh = Color(0xFFE8E8ED),
    surfaceContainerHighest = Color(0xFFDEDEE5),
    outline = Color(0xFF76767D),
    outlineVariant = Color(0xFFD8D8DE),
    surfaceTint = Color.Transparent,
    inverseSurface = Color(0xFF2C2C2E),
    inverseOnSurface = Color(0xFFF2F2F7),
    inversePrimary = Color(0xFF64ACFF),
    error = Color(0xFFB42318),
    onError = Color.White,
    errorContainer = Color(0xFFFFEDEA),
    onErrorContainer = Color(0xFF801D15),
)

internal val LibraryDarkColors = darkColorScheme(
    primary = Color(0xFF64ACFF),
    onPrimary = Color(0xFF00264D),
    primaryContainer = Color(0xFF102D50),
    onPrimaryContainer = Color(0xFFB8D8FF),
    secondary = Color(0xFFB0B0B8),
    onSecondary = Color(0xFF1C1C1E),
    secondaryContainer = Color(0xFF102D50),
    onSecondaryContainer = Color(0xFFB8D8FF),
    tertiary = Color(0xFFB0B0B8),
    onTertiary = Color(0xFF1C1C1E),
    tertiaryContainer = Color(0xFF2C2C2E),
    onTertiaryContainer = Color(0xFFF2F2F7),
    background = Color(0xFF000000),
    onBackground = Color(0xFFF2F2F7),
    surface = Color(0xFF1C1C1E),
    onSurface = Color(0xFFF2F2F7),
    surfaceVariant = Color(0xFF2C2C2E),
    onSurfaceVariant = Color(0xFFB0B0B8),
    surfaceContainerLowest = Color(0xFF141416),
    surfaceContainerLow = Color(0xFF1C1C1E),
    surfaceContainer = Color(0xFF1C1C1E),
    surfaceContainerHigh = Color(0xFF2C2C2E),
    surfaceContainerHighest = Color(0xFF3A3A3C),
    outline = Color(0xFF8E8E93),
    outlineVariant = Color(0xFF38383A),
    surfaceTint = Color.Transparent,
    inverseSurface = Color(0xFFF2F2F7),
    inverseOnSurface = Color(0xFF1C1C1E),
    inversePrimary = Color(0xFF0066CC),
    error = Color(0xFFFFB4A9),
    onError = Color(0xFF680D07),
    errorContainer = Color(0xFF54211C),
    onErrorContainer = Color(0xFFFFDAD4),
)

private fun textStyle(size: Int, height: Int, weight: FontWeight = FontWeight.Normal) =
    TextStyle(
        fontFamily = FontFamily.Default,
        fontSize = size.sp,
        lineHeight = height.sp,
        fontWeight = weight,
        letterSpacing = 0.sp,
    )

internal val LibraryTypography = Typography(
    displayLarge = textStyle(48, 54, FontWeight.Bold),
    displayMedium = textStyle(40, 46, FontWeight.Bold),
    displaySmall = textStyle(34, 41, FontWeight.Bold),
    headlineLarge = textStyle(34, 41, FontWeight.Bold),
    headlineMedium = textStyle(34, 41, FontWeight.Bold),
    headlineSmall = textStyle(26, 33, FontWeight.Bold),
    titleLarge = textStyle(17, 22, FontWeight.SemiBold),
    titleLargeEmphasized = textStyle(24, 30, FontWeight.Bold),
    titleMedium = textStyle(17, 23, FontWeight.SemiBold),
    titleSmall = textStyle(13, 18, FontWeight.SemiBold),
    bodyLarge = textStyle(17, 24),
    bodyMedium = textStyle(15, 21),
    bodySmall = textStyle(13, 19),
    labelLarge = textStyle(17, 22, FontWeight.SemiBold),
    labelMedium = textStyle(13, 18, FontWeight.Medium),
    labelSmall = textStyle(13, 18),
)

internal val LibraryShapes = Shapes(
    extraSmall = RoundedCornerShape(6.dp),
    small = RoundedCornerShape(10.dp),
    medium = LibraryMetrics.groupShape,
    large = RoundedCornerShape(20.dp),
    extraLarge = RoundedCornerShape(28.dp),
)

/** Shared visual language for library, account, import and reader controls. */
@Composable
internal fun LibraryTheme(
    darkTheme: Boolean = org.zotero.android.preferences.appDarkTheme(),
    content: @Composable () -> Unit,
) {
    MaterialTheme(
        colorScheme = if (darkTheme) LibraryDarkColors else LibraryLightColors,
        typography = LibraryTypography,
        shapes = LibraryShapes,
    ) {
        CustomTheme(isDarkTheme = darkTheme, content = content)
    }
}
