package org.zotero.android.screens.allitems.table.rows

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import org.zotero.android.androidx.content.getDrawableByItemType
import org.zotero.android.screens.allitems.data.ItemCellModel
import org.zotero.android.uicomponents.foundation.debounceCombinedClickable
import org.zotero.android.uicomponents.library.LibraryMetrics

@Composable
internal fun ItemRow(
    cellModel: ItemCellModel,
    itemAccessory: ItemCellModel.Accessory?,
    isItemSelected: (key: String) -> Boolean,
    isEditing: Boolean,
    onItemTapped: (item: ItemCellModel) -> Unit,
    onItemLongTapped: (key: String) -> Unit,
    onAccessoryTapped: (key: String) -> Unit,
) {
    val isRowSelected = isItemSelected(cellModel.key)
    val rowModifier = Modifier.fillMaxWidth().heightIn(min = 80.dp)
        .background(if (isRowSelected) MaterialTheme.colorScheme.secondaryContainer else MaterialTheme.colorScheme.surface)
        .semantics { if (isEditing) selected = isRowSelected }
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = rowModifier
            .debounceCombinedClickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = if (isEditing) null else ripple(),
                onClick = { onItemTapped(cellModel) },
                onLongClick = { onItemLongTapped(cellModel.key) }
            )
            .padding(vertical = 12.dp)
    ) {
        Spacer(modifier = Modifier.width(LibraryMetrics.pageInset))
        Image(
            modifier = Modifier.size(24.dp),
            painter = painterResource(id = LocalContext.current.getDrawableByItemType(cellModel.typeIconName)),
            contentDescription = cellModel.typeName,
        )
        Spacer(modifier = Modifier.width(12.dp))
        ItemRowCentralPart(
            model = cellModel,
            isEditing = isEditing,
            itemAccessory = itemAccessory,
            onAccessoryTapped = onAccessoryTapped,
            isItemSelected = isItemSelected,
            onItemTapped = onItemTapped
        )
        Spacer(modifier = Modifier.width(8.dp))

    }
}
