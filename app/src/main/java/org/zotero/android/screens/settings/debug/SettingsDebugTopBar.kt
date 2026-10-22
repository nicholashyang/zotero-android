package org.zotero.android.screens.settings.debug

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import org.zotero.android.uicomponents.Strings
import org.zotero.android.uicomponents.library.LibraryNavigationBar

@Composable
internal fun SettingsDebugTopBar(onBack: () -> Unit) {
    LibraryNavigationBar(stringResource(Strings.settings_debug), onBack = onBack)
}
