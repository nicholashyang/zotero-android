package org.zotero.android.uicomponents.library

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.graphics.SolidColor
import org.zotero.android.R
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import org.zotero.android.uicomponents.Drawables
import org.zotero.android.uicomponents.Strings

@Composable
internal fun LibrarySearchField(query: String, onQueryChange: (String) -> Unit) {
    var focused by remember { mutableStateOf(false) }
    val restingFocus = remember { FocusRequester() }
    val keyboard = LocalSoftwareKeyboardController.current
    val finishInput: () -> Unit = {
        // Keep focus in Compose: older Android versions otherwise refocus the first text field.
        restingFocus.requestFocus()
        keyboard?.hide()
    }
    // Dismiss input before leaving the list; keep the existing query/results.
    BackHandler(enabled = focused, onBack = finishInput)
    Row(
        modifier = Modifier.fillMaxWidth().padding(horizontal = LibraryMetrics.pageInset)
            .padding(bottom = 12.dp).focusRequester(restingFocus).focusable(),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        BasicTextField(
            value = query,
            onValueChange = onQueryChange,
            modifier = Modifier.weight(1f).heightIn(min = 44.dp)
                .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(11.dp))
                .onFocusChanged { focused = it.isFocused },
            singleLine = true,
            cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
            textStyle = MaterialTheme.typography.bodyLarge.copy(color = MaterialTheme.colorScheme.onSurface),
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
            keyboardActions = KeyboardActions(onSearch = { finishInput() }),
            decorationBox = { input ->
                Row(Modifier.padding(start = 12.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(painterResource(R.drawable.ic_native_search), null,
                        Modifier.size(19.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
                    Spacer(Modifier.width(8.dp))
                    Box(Modifier.weight(1f).padding(vertical = 10.dp)) {
                        if (query.isEmpty()) Text(stringResource(Strings.items_search_title),
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant)
                        input()
                    }
                    if (query.isNotEmpty()) LibraryIconButton(Drawables.ic_close_24dp,
                        stringResource(Strings.searchbar_accessibility_clear), { onQueryChange("") })
                    else Spacer(Modifier.width(12.dp))
                }
            },
        )
        if (focused || query.isNotEmpty()) {
            TextButton(onClick = {
                onQueryChange("")
                finishInput()
            }) {
                Text(stringResource(Strings.cancel))
            }
        }
    }
}
