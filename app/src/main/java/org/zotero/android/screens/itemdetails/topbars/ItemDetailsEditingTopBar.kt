package org.zotero.android.screens.itemdetails.topbars

import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import org.zotero.android.uicomponents.Strings
import org.zotero.android.uicomponents.library.LibraryNavigationBar

@Composable
internal fun ItemDetailsEditingTopBar(
    onViewOrEditClicked: () -> Unit,
    onCancelOrBackClicked: () -> Unit,
) {
    LibraryNavigationBar(
        title = "",
        navigationIcon = {
            TextButton(onClick = onCancelOrBackClicked) { Text(stringResource(Strings.cancel)) }
        },
        actions = {
            TextButton(onClick = onViewOrEditClicked) { Text(stringResource(Strings.save)) }
        },
    )
}
