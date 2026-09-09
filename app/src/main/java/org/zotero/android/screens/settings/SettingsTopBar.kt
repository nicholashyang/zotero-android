package org.zotero.android.screens.settings

import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import org.zotero.android.architecture.ui.CustomLayoutSize
import org.zotero.android.uicomponents.Strings
import org.zotero.android.uicomponents.library.LibraryNavigationBar

@Composable
internal fun SettingsTopBar(onClose: () -> Unit, scrollBehavior: TopAppBarScrollBehavior? = null) {
    LibraryNavigationBar(
        title = stringResource(Strings.settings_title),
        largeTitle = !CustomLayoutSize.calculateLayoutType().isTablet(),
        onBack = onClose,
        scrollBehavior = scrollBehavior,
    )
}
