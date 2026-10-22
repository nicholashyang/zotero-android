package org.zotero.android.screens.settings.quickcopy

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import org.zotero.android.uicomponents.Strings
import org.zotero.android.uicomponents.library.LibraryNavigationBar

@Composable
internal fun SettingsQuickCopyTopBar(onBack: () -> Unit) {
    LibraryNavigationBar(stringResource(Strings.settings_export_title), onBack = onBack)
}
