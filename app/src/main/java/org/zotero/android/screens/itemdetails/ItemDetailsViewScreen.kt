package org.zotero.android.screens.itemdetails

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import org.zotero.android.screens.itemdetails.rows.ItemDetailsAbstractFieldRow
import org.zotero.android.screens.itemdetails.rows.ItemDetailsDataRows
import org.zotero.android.screens.itemdetails.rows.itemDetailsNotesTagsAndAttachmentsBlock
import org.zotero.android.uicomponents.library.LibraryGroup
import org.zotero.android.uicomponents.library.LibraryMetrics

@Composable
internal fun ItemDetailsViewScreen(
    viewState: ItemDetailsViewState,
    viewModel: ItemDetailsViewModel,
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxWidth(),
        contentPadding = PaddingValues(LibraryMetrics.pageInset),
    ) {
        item {
            Title(viewState)
        }
        item {
            LibraryGroup(modifier = Modifier.padding(top = 16.dp)) {
                ItemDetailsDataRows(viewState = viewState, viewModel = viewModel)
            }

            if (!viewState.data.isAttachment && !viewState.data.abstract.isNullOrBlank()) {
                LibraryGroup(modifier = Modifier.padding(top = 24.dp)) {
                    ItemDetailsAbstractFieldRow(detailValue = viewState.data.abstract.orEmpty())
                }
            }
        }
        itemDetailsNotesTagsAndAttachmentsBlock(
            viewState = viewState,
            viewModel = viewModel,
            onNoteClicked = { viewModel.openNoteEditor(it) },
            onAddNote = { viewModel.onAddNote() },
            onNoteLongClicked = viewModel::onNoteLongClick,
        )

    }
}

@Composable
private fun Title(
    viewState: ItemDetailsViewState,
) {
    SelectionContainer {
        Text(
            modifier = Modifier
                .padding(horizontal = 4.dp, vertical = 8.dp),
            text = viewState.data.title,
            color = MaterialTheme.colorScheme.onSurface,
            style = MaterialTheme.typography.headlineSmall,
        )
    }

}
