package org.zotero.android.screens.collections

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import org.zotero.android.architecture.ui.CustomLayoutSize
import org.zotero.android.screens.collections.rows.fixedCollectionRow
import org.zotero.android.screens.collections.rows.recursiveCollectionItem
import org.zotero.android.sync.CollectionIdentifier
import org.zotero.android.uicomponents.library.LibraryMetrics

@Composable
internal fun CollectionsTable(
    viewState: CollectionsViewState,
    viewModel: CollectionsViewModel,
    layoutType: CustomLayoutSize.LayoutType
) {
    LazyColumn(
        state = rememberLazyListState(),
        contentPadding = PaddingValues(LibraryMetrics.pageInset),
    ) {
        fixedCollectionRow(
            customType = CollectionIdentifier.CustomType.all,
            viewState = viewState,
            layoutType = layoutType,
            viewModel = viewModel
        )
        fixedCollectionRow(
            customType = CollectionIdentifier.CustomType.recentlyRead,
            viewState = viewState,
            layoutType = layoutType,
            viewModel = viewModel
        )
        if (viewState.collectionItemsToDisplay.isNotEmpty()) {
            item { Spacer(Modifier.height(LibraryMetrics.groupGap)) }
        }
        recursiveCollectionItem(
            layoutType = layoutType,
            collectionItems = viewState.collectionItemsToDisplay,
            selectedCollectionId = viewState.selectedCollectionId,
            isCollapsed = { viewState.isCollapsed(it) },
            onItemTapped = { viewModel.onItemTapped(it.collection) },
            onItemLongTapped = { viewModel.onItemLongTapped(it.collection) },
            onItemChevronTapped = { viewModel.onItemChevronTapped(it.collection) },
            showCollectionItemCounts = viewState.showCollectionItemCounts
        )
        item { Spacer(Modifier.height(LibraryMetrics.groupGap)) }
        fixedCollectionRow(
            customType = CollectionIdentifier.CustomType.unfiled,
            viewState = viewState,
            layoutType = layoutType,
            viewModel = viewModel
        )
        fixedCollectionRow(
            customType = CollectionIdentifier.CustomType.trash,
            viewState = viewState,
            layoutType = layoutType,
            viewModel = viewModel
        )


    }
}
