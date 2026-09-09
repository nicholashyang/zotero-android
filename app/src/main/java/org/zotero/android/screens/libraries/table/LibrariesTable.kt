package org.zotero.android.screens.libraries.table

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import org.zotero.android.screens.libraries.DeleteGroupPopup
import org.zotero.android.screens.libraries.LibrariesViewModel
import org.zotero.android.screens.libraries.LibrariesViewState
import org.zotero.android.uicomponents.Strings
import org.zotero.android.uicomponents.library.LibraryGroupItem
import org.zotero.android.uicomponents.library.LibraryMetrics

@Composable
internal fun LibrariesTable(
    viewState: LibrariesViewState,
    viewModel: LibrariesViewModel,
) {
    LazyColumn(
        state = rememberLazyListState(),
        contentPadding = PaddingValues(LibraryMetrics.pageInset),
    ) {
        itemsIndexed(viewState.customLibraries, key = { _, item -> "custom:${item.id}" }) { index, item ->
            LibraryGroupItem(first = index == 0, last = index == viewState.customLibraries.lastIndex) {
                LibrariesItem(
                    item = item,
                    onItemTapped = { viewModel.onCustomLibraryTapped(index) },
                    onItemLongTapped = {
                        //no-op
                    }
                )
            }
        }

        if (viewState.groupLibraries.isNotEmpty()) {
            item {
                Spacer(Modifier.height(LibraryMetrics.groupGap))
                LibrariesSectionTitle(Strings.libraries_group_libraries)
                Spacer(Modifier.height(8.dp))
            }
            itemsIndexed(viewState.groupLibraries, key = { _, item -> "group:${item.id}" }) { index, item ->
                LibraryGroupItem(first = index == 0, last = index == viewState.groupLibraries.lastIndex) {
                    Box {
                        if (viewState.groupIdForDeletePopup == item.id) {
                            DeleteGroupPopup(
                                onDeleteGroup = {
                                    viewModel.showDeleteGroupQuestion(
                                        item.id,
                                        item.name
                                    )
                                },
                                dismissDeleteGroupPopup = { viewModel.dismissDeleteGroupPopup() },
                            )
                        }
                        LibrariesItem(
                            item = item,
                            onItemTapped = { viewModel.onGroupLibraryTapped(index) },
                            onItemLongTapped = { viewModel.showDeleteGroupPopup(item) }
                        )
                    }
                }
            }
        }
    }
}
