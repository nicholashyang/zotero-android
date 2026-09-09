package org.zotero.android.screens.allitems

import androidx.compose.foundation.layout.Box
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import org.zotero.android.screens.downloadedfiles.DownloadedFilesPopup
import org.zotero.android.uicomponents.Drawables
import org.zotero.android.uicomponents.Strings
import org.zotero.android.uicomponents.library.LibraryBottomBar
import org.zotero.android.uicomponents.library.LibraryIconButton

@Composable
internal fun AllItemsRegularBottomPanel(viewModel: AllItemsViewModel, viewState: AllItemsViewState) {
    AllItemsToolbar(
        hasFilters = viewState.filters.isNotEmpty(),
        onFilter = viewModel::showFilters,
        onAdd = viewModel::onAdd,
        onSort = viewModel::showSortPicker,
        filterPopup = {
            if (viewState.showDownloadedFilesPopup) {
                DownloadedFilesPopup(viewState = viewState, viewModel = viewModel)
            }
        },
    )
}

@Composable
internal fun AllItemsToolbar(
    hasFilters: Boolean,
    onFilter: () -> Unit,
    onAdd: () -> Unit,
    onSort: () -> Unit,
    filterPopup: @Composable () -> Unit = {},
) {
    LibraryBottomBar {
        Box {
            filterPopup()
            LibraryIconButton(
                icon = if (hasFilters) Drawables.filter_list_24px else Drawables.filter_list_off_24px,
                label = stringResource(Strings.all_items_bottom_panel_filters),
                onClick = onFilter,
            )
        }
        FilledIconButton(onClick = onAdd) {
            Icon(
                painter = painterResource(Drawables.add_24px),
                contentDescription = stringResource(Strings.all_items_bottom_panel_add_new_item),
                tint = MaterialTheme.colorScheme.onPrimary,
            )
        }
        LibraryIconButton(
            Drawables.swap_vert_24px,
            stringResource(Strings.all_items_bottom_panel_sort_by),
            onSort,
        )
    }
}
