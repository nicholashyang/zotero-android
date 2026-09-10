package org.zotero.android.screens.allitems.table

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.*
import org.zotero.android.preferences.*
import org.zotero.android.uicomponents.library.SwipeReveal
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Modifier
import org.zotero.android.screens.allitems.data.ItemCellModel
import org.zotero.android.screens.allitems.table.rows.ItemRow
import org.zotero.android.uicomponents.library.LibraryEmptyState
import org.zotero.android.uicomponents.library.LibraryGroupItem
import org.zotero.android.uicomponents.library.LibraryMetrics

@Composable
internal fun AllItemsTable(
    lazyListState: LazyListState,
    itemCellModels: SnapshotStateList<ItemCellModel>,
    isItemSelected: (key: String) -> Boolean,
    getItemAccessory: (itemKey: String) -> ItemCellModel.Accessory?,
    isEditing: Boolean,
    isRefreshing: Boolean,
    onItemTapped: (item: ItemCellModel) -> Unit,
    onItemLongTapped: (key: String) -> Unit,
    onAccessoryTapped: (key: String) -> Unit,
    onStartSync: () -> Unit,
    isFiltered: Boolean = false,
    canSwipe: (String, SwipeAction) -> Boolean = { _, _ -> false },
    onSwipe: (ItemCellModel, SwipeAction) -> Unit = { _, _ -> },
) {
    val preferences by rememberAppPreferences().state.collectAsState()
    PullToRefreshBox(
        isRefreshing = isRefreshing,
        onRefresh = onStartSync,
    ) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            state = lazyListState,
            contentPadding = PaddingValues(LibraryMetrics.pageInset),
        ) {
            if (itemCellModels.isEmpty() && !isRefreshing) {
                item(key = "empty-state") { LibraryEmptyState(filtered = isFiltered) }
            }
            itemsIndexed(
                items = itemCellModels, key = { _, item -> item.key }
            ) { index, item ->
                LibraryGroupItem(first = index == 0, last = index == itemCellModels.lastIndex) {
                    SwipeReveal(item.key,
                        left = preferences.leftSwipe.takeIf { !isEditing && canSwipe(item.key, it) } ?: SwipeAction.NONE,
                        right = preferences.rightSwipe.takeIf { !isEditing && canSwipe(item.key, it) } ?: SwipeAction.NONE,
                        onAction = { onSwipe(item, it) }) { revealed, close ->
                    ItemRow(
                        cellModel = item,
                        itemAccessory = getItemAccessory(item.key),
                        isEditing = isEditing,
                        onItemTapped = { if (revealed) close() else onItemTapped(it) },
                        onItemLongTapped = onItemLongTapped,
                        onAccessoryTapped = onAccessoryTapped,
                        isItemSelected = isItemSelected,
                    )
                    }
                }
            }

        }

    }
}
