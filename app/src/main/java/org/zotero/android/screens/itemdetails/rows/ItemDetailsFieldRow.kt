package org.zotero.android.screens.itemdetails.rows

import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import org.zotero.android.uicomponents.foundation.safeClickable

@Composable
internal fun ItemDetailsFieldRow(
    detailTitle: String,
    detailValue: String,
    textColor: Color = MaterialTheme.colorScheme.onSurface,
    additionalInfoString: String? = null,
    onRowTapped: (() -> Unit)? = null,
) {
    BoxWithConstraints(Modifier.fillMaxWidth().safeClickable(
        onClick = onRowTapped, interactionSource = remember { MutableInteractionSource() }, indication = ripple())) {
        val stacked = maxWidth < 400.dp || LocalDensity.current.fontScale > 1.3f
        if (stacked) {
            Column(Modifier.fillMaxWidth().heightIn(min = if (onRowTapped != null) 48.dp else 40.dp)
                .padding(vertical = 8.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(detailTitle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text(detailValue, style = MaterialTheme.typography.bodyLarge, color = textColor)
                if (additionalInfoString != null) Text(additionalInfoString,
                    style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        } else {
            Row(Modifier.fillMaxWidth().heightIn(min = 48.dp).padding(vertical = 8.dp),
                verticalAlignment = Alignment.Top) {
                Text(detailTitle, Modifier.width(140.dp).padding(end = 16.dp),
                    style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text(detailValue, Modifier.weight(1f), style = MaterialTheme.typography.bodyLarge, color = textColor)
                if (additionalInfoString != null) Text(additionalInfoString, Modifier.padding(start = 12.dp),
                    style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}
