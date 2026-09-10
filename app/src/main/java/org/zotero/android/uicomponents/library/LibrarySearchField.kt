package org.zotero.android.uicomponents.library

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
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
import androidx.compose.ui.graphics.Color
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
        TextField(
            value = query,
            onValueChange = onQueryChange,
            modifier = Modifier.weight(1f).onFocusChanged { focused = it.isFocused },
            singleLine = true,
            shape = LibraryMetrics.groupShape,
            textStyle = MaterialTheme.typography.bodyLarge,
            placeholder = { Text(stringResource(Strings.items_search_title)) },
            leadingIcon = {
                Icon(painterResource(Drawables.search_24px), contentDescription = null)
            },
            trailingIcon = {
                if (query.isNotEmpty()) {
                    LibraryIconButton(
                        Drawables.ic_close_24dp,
                        stringResource(Strings.searchbar_accessibility_clear),
                        onClick = { onQueryChange("") },
                    )
                }
            },
            colors = TextFieldDefaults.colors(
                focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                focusedIndicatorColor = Color.Transparent,
                unfocusedIndicatorColor = Color.Transparent,
            ),
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
            keyboardActions = KeyboardActions(onSearch = { finishInput() }),
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
