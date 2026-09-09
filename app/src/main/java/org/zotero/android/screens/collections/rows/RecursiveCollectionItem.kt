package org.zotero.android.screens.collections.rows

import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlinx.collections.immutable.ImmutableList
import org.zotero.android.architecture.ui.CustomLayoutSize
import org.zotero.android.screens.collections.data.CollectionItemWithChildren
import org.zotero.android.sync.CollectionIdentifier
import org.zotero.android.uicomponents.library.LibraryGroupItem

private val levelPaddingConst = 16.dp

internal fun LazyListScope.recursiveCollectionItem(
    layoutType: CustomLayoutSize.LayoutType,
    levelPadding: Dp = 4.dp,
    collectionItems: ImmutableList<CollectionItemWithChildren>,
    selectedCollectionId: CollectionIdentifier,
    showCollectionItemCounts: Boolean,
    isCollapsed: (item: CollectionItemWithChildren) -> Boolean,
    onItemTapped: (item: CollectionItemWithChildren) -> Unit,
    onItemLongTapped: (item: CollectionItemWithChildren) -> Unit,
    onItemChevronTapped: (item: CollectionItemWithChildren) -> Unit,
) {
    val visible = mutableListOf<Pair<CollectionItemWithChildren, Dp>>()
    fun appendVisible(items: ImmutableList<CollectionItemWithChildren>, indent: Dp) {
        items.forEach { item ->
            visible.add(item to indent)
            if (!isCollapsed(item)) appendVisible(item.children, indent + levelPaddingConst)
        }
    }
    appendVisible(collectionItems, levelPadding)
    itemsIndexed(visible, key = { _, row -> row.first.collection.identifier.id }) { index, (item, indent) ->
        LibraryGroupItem(first = index == 0, last = index == visible.lastIndex) {
            CollectionRowItem(
                layoutType = layoutType,
                levelPadding = indent,
                selectedCollectionId = selectedCollectionId,
                collection = item.collection,
                hasChildren = item.children.isNotEmpty(),
                showCollectionItemCounts = showCollectionItemCounts,
                isCollapsed = isCollapsed(item),
                onItemTapped = { onItemTapped(item) },
                onItemLongTapped = { onItemLongTapped(item) },
                onItemChevronTapped = { onItemChevronTapped(item) },
            )
        }
    }
}
