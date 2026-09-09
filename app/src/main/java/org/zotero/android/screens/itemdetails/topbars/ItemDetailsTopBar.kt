package org.zotero.android.screens.itemdetails.topbars

import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import org.zotero.android.uicomponents.Strings
import org.zotero.android.uicomponents.library.LibraryNavigationBar

@Composable
internal fun ItemDetailsTopBar(
    onViewOrEditClicked: () -> Unit,
    onCancelOrBackClicked: () -> Unit,
    scrollBehavior: TopAppBarScrollBehavior,
) {
    LibraryNavigationBar(
        title = stringResource(Strings.all_items_details),
        onBack = onCancelOrBackClicked,
        scrollBehavior = scrollBehavior,
        actions = {
            TextButton(onClick = onViewOrEditClicked) { Text(stringResource(Strings.edit)) }
        },
    )
}
