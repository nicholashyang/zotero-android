package org.zotero.android.uicomponents.library

import androidx.compose.foundation.clickable
import androidx.annotation.DrawableRes
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LargeTopAppBar
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import org.zotero.android.uicomponents.Drawables
import org.zotero.android.uicomponents.Strings

@Composable
internal fun LibraryNavigationBar(
    title: String,
    onTitleClick: (() -> Unit)? = null,
    largeTitle: Boolean = false,
    scrollBehavior: TopAppBarScrollBehavior? = null,
    onBack: (() -> Unit)? = null,
    navigationIcon: @Composable () -> Unit = {
        if (onBack != null) {
            LibraryIconButton(Drawables.arrow_back_24dp, stringResource(Strings.back), onBack)
        }
    },
    actions: @Composable RowScope.() -> Unit = {},
) {
    val colors = TopAppBarDefaults.topAppBarColors(
        containerColor = MaterialTheme.colorScheme.background,
        scrolledContainerColor = MaterialTheme.colorScheme.background,
        navigationIconContentColor = MaterialTheme.colorScheme.primary,
        actionIconContentColor = MaterialTheme.colorScheme.primary,
    )
    val titleContent: @Composable () -> Unit = {
        Text(title, modifier = if (onTitleClick != null) Modifier.clickable(onClick = onTitleClick) else Modifier, maxLines = 1, overflow = TextOverflow.Ellipsis)
    }
    if (largeTitle) {
        LargeTopAppBar(
            title = titleContent,
            navigationIcon = navigationIcon,
            actions = actions,
            colors = colors,
            scrollBehavior = scrollBehavior,
        )
    } else {
        TopAppBar(
            title = titleContent,
            navigationIcon = navigationIcon,
            actions = actions,
            colors = colors,
            scrollBehavior = scrollBehavior,
        )
    }
}

@Composable
internal fun LibraryIconButton(
    @DrawableRes icon: Int,
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    IconButton(
        onClick = onClick,
        modifier = modifier.sizeIn(
            minWidth = LibraryMetrics.minimumTouchTarget,
            minHeight = LibraryMetrics.minimumTouchTarget,
        ),
    ) {
        Icon(
            painter = painterResource(icon),
            contentDescription = label,
            tint = MaterialTheme.colorScheme.primary,
        )
    }
}
