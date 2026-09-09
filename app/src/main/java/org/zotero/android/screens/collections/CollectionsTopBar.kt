package org.zotero.android.screens.collections

import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import org.zotero.android.architecture.ui.CustomLayoutSize
import org.zotero.android.uicomponents.Drawables
import org.zotero.android.uicomponents.Strings
import org.zotero.android.uicomponents.library.LibraryIconButton
import org.zotero.android.uicomponents.library.LibraryNavigationBar

@Composable
internal fun CollectionsTopBar(
    libraryName: String,
    scrollBehavior: TopAppBarScrollBehavior,
    navigateToLibraries: () -> Unit,
    onAdd: () -> Unit,
) {
    LibraryNavigationBar(
        title = libraryName,
        largeTitle = !CustomLayoutSize.calculateLayoutType().isTablet(),
        scrollBehavior = scrollBehavior,
        onBack = navigateToLibraries,
        actions = {
            LibraryIconButton(Drawables.add_24px, stringResource(Strings.collections_add_collection), onAdd)
        },
    )
}
