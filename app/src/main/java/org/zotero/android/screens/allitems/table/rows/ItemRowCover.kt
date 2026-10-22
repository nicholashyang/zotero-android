package org.zotero.android.screens.allitems.table.rows

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.produceState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import org.zotero.android.androidx.content.getDrawableByItemType
import org.zotero.android.covers.ItemCoverLoader
import org.zotero.android.screens.allitems.data.ItemCellModel

@Composable
internal fun ItemRowCover(model: ItemCellModel) {
    val context = LocalContext.current.applicationContext
    // Reset the image immediately when a row is reused or its attachment changes.
    key(model.cover) {
        val bitmap by produceState<android.graphics.Bitmap?>(null, model.cover) {
            value = model.cover?.let { ItemCoverLoader.load(it, context.cacheDir) }
        }
        val image = bitmap
        val shape = RoundedCornerShape(4.dp)
        Box(
            modifier = Modifier.size(48.dp, 64.dp)
                .clip(shape)
                .background(MaterialTheme.colorScheme.surfaceContainerLow)
                .border(0.5.dp, MaterialTheme.colorScheme.outlineVariant, shape),
            contentAlignment = Alignment.Center,
        ) {
            if (image != null) {
                Image(
                    bitmap = image.asImageBitmap(),
                    contentDescription = model.typeName,
                    contentScale = ContentScale.Fit,
                    modifier = Modifier.matchParentSize().testTag("item-cover-${model.key}"),
                )
            } else {
                ItemTypeIcon(model)
            }
        }
    }
}

@Composable
private fun ItemTypeIcon(model: ItemCellModel) {
    Image(
        modifier = Modifier.size(24.dp),
        painter = painterResource(LocalContext.current.getDrawableByItemType(model.typeIconName)),
        contentDescription = model.typeName,
    )
}
