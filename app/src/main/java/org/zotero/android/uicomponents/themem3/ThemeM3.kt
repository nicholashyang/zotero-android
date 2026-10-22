package org.zotero.android.uicomponents.themem3

import android.os.Build
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import org.zotero.android.uicomponents.library.LibraryDarkColors
import org.zotero.android.uicomponents.library.LibraryLightColors
import org.zotero.android.uicomponents.library.LibraryShapes
import org.zotero.android.uicomponents.theme.CustomTheme

/** Keep every Compose entry point on the same semantic colors, type and shapes. */
@Composable
fun AppThemeM3(
    darkTheme: Boolean = org.zotero.android.preferences.appDarkTheme(),
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit,
) {
    val colors = if (dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        if (darkTheme) dynamicDarkColorScheme(LocalContext.current)
        else dynamicLightColorScheme(LocalContext.current)
    } else if (darkTheme) LibraryDarkColors else LibraryLightColors
    MaterialTheme(colorScheme = colors, typography = AppTypographyM3, shapes = LibraryShapes) {
        CustomTheme(isDarkTheme = darkTheme, content = content)
    }
}
