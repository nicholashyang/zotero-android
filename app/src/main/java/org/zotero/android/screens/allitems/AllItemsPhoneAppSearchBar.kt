package org.zotero.android.screens.allitems

import androidx.compose.foundation.layout.Column
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.runtime.Composable
import org.zotero.android.uicomponents.library.LibraryNavigationBar
import org.zotero.android.uicomponents.library.LibrarySearchField

@Composable
internal fun AllItemsPhoneAppSearchBar(
    viewState: AllItemsViewState,
    viewModel: AllItemsViewModel,
    scrollBehavior: TopAppBarScrollBehavior,
) {
    AllItemsPhoneHeader(
        title = viewState.collectionName,
        query = viewState.searchTerm.orEmpty(),
        onQueryChange = viewModel::onSearch,
        onBack = viewModel::navigateToCollections,
        scrollBehavior = scrollBehavior,
    )
}

@Composable
internal fun AllItemsPhoneHeader(
    title: String,
    query: String,
    onQueryChange: (String) -> Unit,
    onBack: () -> Unit,
    scrollBehavior: TopAppBarScrollBehavior,
) {
    Column {
        LibraryNavigationBar(
            title = title,
            largeTitle = true,
            scrollBehavior = scrollBehavior,
            onBack = onBack,
        )
        LibrarySearchField(query = query, onQueryChange = onQueryChange)
    }
}
