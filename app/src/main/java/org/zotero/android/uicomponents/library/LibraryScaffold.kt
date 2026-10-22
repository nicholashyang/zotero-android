package org.zotero.android.uicomponents.library

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.Alignment
import androidx.compose.ui.unit.dp
import org.zotero.android.architecture.ui.FoldSafeContent
import androidx.compose.ui.input.nestedscroll.nestedScroll

/** Owns all content insets. Its children must not add toolbar/nav-bar spacers. */
@Composable
internal fun LibraryScaffold(
    topBar: @Composable () -> Unit,
    modifier: Modifier = Modifier,
    bottomBar: @Composable () -> Unit = {},
    scrollBehavior: TopAppBarScrollBehavior? = null,
    content: @Composable () -> Unit,
) {
    val scrollModifier = scrollBehavior?.let {
        Modifier.nestedScroll(it.nestedScrollConnection)
    } ?: Modifier
    FoldSafeContent(Modifier.fillMaxSize()) {
        Scaffold(
            modifier = modifier.fillMaxSize().imePadding().then(scrollModifier),
            containerColor = MaterialTheme.colorScheme.background,
            contentWindowInsets = WindowInsets.safeDrawing,
            topBar = topBar,
            bottomBar = bottomBar,
        ) { padding ->
            Box(Modifier.fillMaxSize().padding(padding).consumeWindowInsets(padding), contentAlignment = Alignment.TopCenter) {
                Box(Modifier.widthIn(max = 820.dp).fillMaxSize()) { content() }
            }
        }
    }
}
