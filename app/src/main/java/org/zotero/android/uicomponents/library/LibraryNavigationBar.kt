package org.zotero.android.uicomponents.library

import androidx.compose.foundation.clickable
import androidx.annotation.DrawableRes
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LargeTopAppBar
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.Alignment
import androidx.compose.ui.draw.rotate
import org.zotero.android.R
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
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
        Row(modifier = if (onTitleClick != null) Modifier.clickable(onClick = onTitleClick) else Modifier,
            verticalAlignment = Alignment.CenterVertically) {
            Text(title, modifier = Modifier.weight(1f, fill = false), maxLines = 1, overflow = TextOverflow.Ellipsis)
            if (onTitleClick != null) {
                Spacer(Modifier.width(8.dp))
                Icon(painterResource(R.drawable.ic_native_chevron), null, Modifier.size(14.dp).rotate(90f),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
    if (largeTitle) {
        LargeTopAppBar(
            title = titleContent,
            navigationIcon = navigationIcon,
            actions = actions,
            colors = colors,
            scrollBehavior = scrollBehavior,
            collapsedHeight = 52.dp,
            expandedHeight = 112.dp,
        )
    } else {
        CenterAlignedTopAppBar(
            title = titleContent,
            navigationIcon = navigationIcon,
            actions = actions,
            colors = colors,
            scrollBehavior = scrollBehavior,
            expandedHeight = 52.dp,
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
            painter = painterResource(librarySymbol(icon)),
            contentDescription = label,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(22.dp),
        )
    }
}
