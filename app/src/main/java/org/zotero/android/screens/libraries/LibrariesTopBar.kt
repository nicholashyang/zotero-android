package org.zotero.android.screens.libraries

import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import org.zotero.android.architecture.ui.CustomLayoutSize
import org.zotero.android.uicomponents.Drawables
import org.zotero.android.uicomponents.Strings
import org.zotero.android.uicomponents.library.LibraryIconButton
import org.zotero.android.uicomponents.library.LibraryNavigationBar

@Composable
internal fun LibrariesTopBar(scrollBehavior: TopAppBarScrollBehavior, onSettingsTapped: () -> Unit) {
    LibraryNavigationBar(
        title = stringResource(Strings.toolbar_libraries),
        largeTitle = !CustomLayoutSize.calculateLayoutType().isTablet(),
        scrollBehavior = scrollBehavior,
        actions = {
            LibraryIconButton(Drawables.settings_24px, stringResource(Strings.settings_title), onSettingsTapped)
        },
    )
}
