package org.zotero.android.uicomponents.library

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.zotero.android.uicomponents.CustomUriHandler

internal object LibraryMetrics {
    val pageInset = 16.dp
    val groupGap = 24.dp
    val groupShape = RoundedCornerShape(12.dp)
    val minimumTouchTarget = 48.dp
    val minimumRowHeight = 56.dp
}

private val lightColors = lightColorScheme(
    primary = Color(0xFFC62836),
    onPrimary = Color.White,
    primaryContainer = Color(0xFFFCE8EB),
    onPrimaryContainer = Color(0xFF8C1825),
    secondary = Color(0xFF626268),
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFFCE8EB),
    onSecondaryContainer = Color(0xFF8C1825),
    tertiary = Color(0xFF626268),
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
    outlineVariant = Color(0xFFE2E2E7),
    error = Color(0xFFB42318),
    onError = Color.White,
    errorContainer = Color(0xFFFFEDEA),
    onErrorContainer = Color(0xFF801D15),
)

private val darkColors = darkColorScheme(
    primary = Color(0xFFFF7B85),
    onPrimary = Color(0xFF410B12),
    primaryContainer = Color(0xFF4A2229),
    onPrimaryContainer = Color(0xFFFFB7BE),
    secondary = Color(0xFFB0B0B8),
    onSecondary = Color(0xFF1C1C1E),
    secondaryContainer = Color(0xFF4A2229),
    onSecondaryContainer = Color(0xFFFFB7BE),
    tertiary = Color(0xFFB0B0B8),
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

private val typography = Typography(
    headlineLarge = textStyle(32, 38, FontWeight.Bold),
    headlineMedium = textStyle(32, 38, FontWeight.Bold),
    headlineSmall = textStyle(24, 30, FontWeight.Bold),
    titleLarge = textStyle(20, 26, FontWeight.SemiBold),
    titleMedium = textStyle(16, 22, FontWeight.SemiBold),
    titleSmall = textStyle(13, 18, FontWeight.SemiBold),
    bodyLarge = textStyle(16, 24),
    bodyMedium = textStyle(13, 19),
    bodySmall = textStyle(13, 19),
    labelLarge = textStyle(16, 22, FontWeight.Medium),
    labelMedium = textStyle(13, 18, FontWeight.Medium),
    labelSmall = textStyle(13, 18),
)

/** Theme for the library shell; readers keep their own explicit AppThemeM3. */
@Composable
internal fun LibraryTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    MaterialTheme(
        colorScheme = if (darkTheme) darkColors else lightColors,
        typography = typography,
        shapes = Shapes(
            small = RoundedCornerShape(8.dp),
            medium = LibraryMetrics.groupShape,
            large = RoundedCornerShape(16.dp),
        ),
    ) {
        CompositionLocalProvider(
            LocalUriHandler provides CustomUriHandler(LocalContext.current),
            content = content,
        )
    }
}
