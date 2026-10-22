package org.zotero.android.screens.settings.stylepicker

import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import org.zotero.android.uicomponents.Strings
import org.zotero.android.uicomponents.library.LibraryNavigationBar

@Composable
internal fun SettingsCiteTopBar(onBack: () -> Unit, scrollBehavior: TopAppBarScrollBehavior) {
    LibraryNavigationBar(stringResource(Strings.settings_cite_title), onBack = onBack, scrollBehavior = scrollBehavior)
}
