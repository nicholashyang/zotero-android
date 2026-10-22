package org.zotero.android.screens.settings.account

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import org.zotero.android.uicomponents.Strings
import org.zotero.android.uicomponents.library.LibraryNavigationBar

@Composable
internal fun SettingsAccountTopBar(onBack: () -> Unit) {
    LibraryNavigationBar(stringResource(Strings.settings_sync_account), onBack = onBack)
}
