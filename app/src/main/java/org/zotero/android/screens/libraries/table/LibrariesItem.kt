package org.zotero.android.screens.libraries.table

import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import org.zotero.android.screens.libraries.data.LibraryRowData
import org.zotero.android.screens.libraries.data.LibraryState
import org.zotero.android.uicomponents.Drawables
import org.zotero.android.uicomponents.library.LibraryRow

@Composable
internal fun LibrariesItem(item: LibraryRowData, onItemTapped: () -> Unit, onItemLongTapped: () -> Unit) {
    LibraryRow(
        title = item.name,
        onClick = onItemTapped,
        onLongClick = onItemLongTapped,
        leading = {
            Icon(
                painter = painterResource(image(item.state)),
                contentDescription = null,
                modifier = Modifier.size(24.dp),
                tint = MaterialTheme.colorScheme.primary,
            )
        },
    )
}

private fun image(state: LibraryState): Int = when (state) {
    LibraryState.normal -> Drawables.icon_cell_library
    LibraryState.locked -> Drawables.icon_cell_library_readonly
    LibraryState.archived -> Drawables.library_archived
}
