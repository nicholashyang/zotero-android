package org.zotero.android.screens.allitems.table.rows

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import org.zotero.android.uicomponents.math.MathText
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import org.zotero.android.screens.allitems.data.ItemCellModel
import org.zotero.android.uicomponents.Drawables
import org.zotero.android.uicomponents.Strings

@Composable
internal fun RowScope.ItemRowTitleAndSubtitlePart(model: ItemCellModel) {
    Column(
        modifier = Modifier.weight(1f)
    ) {
        MathText(
            text = model.title.ifEmpty { " " },
            maxLines = 2,
            color = MaterialTheme.colorScheme.onSurface,
            style = MaterialTheme.typography.bodyLarge,
        )
        Spacer(modifier = Modifier.height(4.dp))
        Row {
            var subtitleText = model.subtitle.ifEmpty { " " }
            val shouldHideSubtitle =
                model.subtitle.isEmpty() && (model.hasNote || !model.tagColors.isEmpty())
            if (shouldHideSubtitle) {
                subtitleText = ""
            }
            Text(
                modifier = Modifier.weight(1f, fill = false),
                text = subtitleText,
                style = MaterialTheme.typography.bodyMedium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            if (model.hasNote) {
                if (model.subtitle.isNotEmpty()) {
                    Spacer(modifier = Modifier.width(4.dp))
                }
                Image(
                    modifier = Modifier
                        .size(12.dp)
                        .align(Alignment.CenterVertically),
                    painter = painterResource(id = Drawables.cell_note),
                    contentDescription = stringResource(Strings.item_detail_notes),
                )
            }
        }
        if (model.abstract.isNotBlank()) {
            Spacer(Modifier.height(6.dp))
            MathText(model.abstract, MaterialTheme.colorScheme.onSurfaceVariant, MaterialTheme.typography.bodyMedium, maxLines = 3)
        }
        if (model.tagColors.isNotEmpty()) {
            FlowRow(
                modifier = Modifier.padding(top = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                model.tagColors.forEach { color ->
                    Box(Modifier.size(6.dp).background(color, CircleShape))
                }
            }
        }
    }
}
