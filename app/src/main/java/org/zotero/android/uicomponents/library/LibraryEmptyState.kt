package org.zotero.android.uicomponents.library

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.background
import androidx.compose.foundation.shape.RoundedCornerShape
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
import org.zotero.android.R

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
        Box(Modifier.padding(bottom = 12.dp).size(64.dp)
            .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(18.dp)), contentAlignment = Alignment.Center) {
            Icon(painterResource(if (isError) Drawables.ic_alert_icon else R.drawable.ic_native_library), null,
                Modifier.size(30.dp), tint = if (isError) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurfaceVariant)
        }
        Text(title, style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onSurface, textAlign = TextAlign.Center)
        Text(message, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant, textAlign = TextAlign.Center)
    }
}
