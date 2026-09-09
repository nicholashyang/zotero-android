package org.zotero.android.uicomponents.library

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import org.zotero.android.uicomponents.Drawables
import org.zotero.android.uicomponents.Strings

@Composable
internal fun LibraryEmptyState(filtered: Boolean) {
    LibraryStatusMessage(
        title = stringResource(if (filtered) Strings.library_search_empty_title else Strings.library_empty_title),
        message = stringResource(if (filtered) Strings.library_search_empty_message else Strings.library_empty_message),
        modifier = Modifier.padding(horizontal = 24.dp, vertical = 48.dp),
    )
}

@Composable
internal fun LibraryErrorState(errorTitle: String, modifier: Modifier = Modifier) {
    LibraryStatusMessage(
        title = errorTitle,
        message = stringResource(Strings.error_list_load_body),
        modifier = modifier.padding(24.dp),
        isError = true,
    )
}

@Composable
private fun LibraryStatusMessage(
    title: String,
    message: String,
    modifier: Modifier,
    isError: Boolean = false,
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        if (isError) Icon(painterResource(Drawables.ic_alert_icon), null, tint = MaterialTheme.colorScheme.error)
        Text(title, style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onSurface, textAlign = TextAlign.Center)
        Text(message, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant, textAlign = TextAlign.Center)
    }
}
