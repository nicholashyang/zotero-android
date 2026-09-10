package org.zotero.android.uicomponents.library

import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import org.zotero.android.R
import org.zotero.android.preferences.SwipeAction
import kotlin.math.abs
import kotlin.math.roundToInt

@Composable
internal fun SwipeReveal(key: String, left: SwipeAction, right: SwipeAction, onAction: (SwipeAction) -> Unit,
    content: @Composable (isRevealed: Boolean, close: () -> Unit) -> Unit) {
    var offset by remember(key, left, right) { mutableFloatStateOf(0f) }
    var confirming by remember(key) { mutableStateOf(false) }
    val width = with(LocalDensity.current) { 152.dp.toPx() }
    val gutter = with(LocalDensity.current) { 48.dp.toPx() }
    val action = if (offset < 0) left else right
    val enabled = left != SwipeAction.NONE || right != SwipeAction.NONE
    Box(Modifier.fillMaxWidth().background(MaterialTheme.colorScheme.surfaceVariant)) {
        if (offset != 0f && action != SwipeAction.NONE) {
            TextButton(modifier = Modifier.align(if (offset < 0) Alignment.CenterEnd else Alignment.CenterStart).width(152.dp),
                onClick = { offset = 0f; if (action == SwipeAction.TRASH) confirming = true else onAction(action) }) {
                Text(stringResource(action.label), color = if (action == SwipeAction.TRASH) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary)
            }
        }
        Box(Modifier.offset { IntOffset(offset.roundToInt(), 0) }.fillMaxWidth().background(MaterialTheme.colorScheme.surface)
            .then(if (!enabled) Modifier else Modifier.pointerInput(key, left, right) {
                awaitEachGesture {
                    val down = awaitFirstDown(requireUnconsumed = false)
                    if (down.position.x <= gutter) return@awaitEachGesture
                    val initial = offset
                    var dragging = false
                    while (true) {
                        val change = awaitPointerEvent().changes.firstOrNull { it.id == down.id } ?: break
                        if (change.isConsumed && !dragging) break
                        val dx = change.position.x - down.position.x
                        val dy = change.position.y - down.position.y
                        if (!dragging && abs(dy) > viewConfiguration.touchSlop && abs(dy) > abs(dx)) break
                        val next = initial + dx
                        if (abs(dx) > viewConfiguration.touchSlop &&
                            ((next < 0 && left != SwipeAction.NONE) || (next > 0 && right != SwipeAction.NONE))) {
                            dragging = true
                            change.consume()
                            offset = next.coerceIn(-width, width)
                        }
                        if (!change.pressed) break
                    }
                    if (dragging) offset = if (abs(offset) > width / 3) (if (offset < 0) -width else width) else 0f
                }
            })) { content(offset != 0f) { offset = 0f } }
    }
    if (confirming) AlertDialog(onDismissRequest = { confirming = false }, title = { Text(stringResource(R.string.mobile_trash_question)) },
        confirmButton = { TextButton(onClick = { confirming = false; onAction(SwipeAction.TRASH) }) { Text(stringResource(R.string.mobile_swipe_trash)) } },
        dismissButton = { TextButton(onClick = { confirming = false }) { Text(stringResource(android.R.string.cancel)) } })
}
