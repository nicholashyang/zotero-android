package org.zotero.android.uicomponents.library

import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import kotlin.math.abs

/** Leave the outer 24 dp to Android. Only the inner gutter can initiate page navigation. */
internal fun Modifier.pageSwipe(enabled: Boolean = true, onRight: () -> Unit): Modifier =
    if (!enabled) this else pointerInput(onRight) {
        awaitEachGesture {
            val down = awaitFirstDown(requireUnconsumed = false)
            if (down.position.x < 24.dp.toPx() || down.position.x > 64.dp.toPx()) return@awaitEachGesture
            var horizontal = 0f
            var vertical = 0f
            while (true) {
                val event = awaitPointerEvent()
                val change = event.changes.firstOrNull { it.id == down.id } ?: break
                if (change.isConsumed) break
                horizontal = change.position.x - down.position.x
                vertical = change.position.y - down.position.y
                if (abs(vertical) > viewConfiguration.touchSlop && abs(vertical) > abs(horizontal)) break
                if (horizontal > 64.dp.toPx() && horizontal > abs(vertical) * 2) {
                    change.consume()
                    onRight()
                    break
                }
                if (!change.pressed) break
            }
        }
    }
