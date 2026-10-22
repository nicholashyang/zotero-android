package org.zotero.android.architecture.ui

internal enum class FoldAxis { VERTICAL, HORIZONTAL }
internal data class PaneBounds(val left: Int, val top: Int, val right: Int, val bottom: Int) {
    val width get() = (right - left).coerceAtLeast(0)
    val height get() = (bottom - top).coerceAtLeast(0)
}
internal data class WindowFold(val bounds: PaneBounds, val axis: FoldAxis)
internal data class WorkspacePlan(val sidebarWidth: Int, val contentLeft: Int, val contentWidth: Int) {
    val hasSidebar get() = sidebarWidth > 0
}

/** Coordinates are physical window pixels, including zero-width separating folds. */
internal fun safePane(width: Int, height: Int, windowX: Int, windowY: Int, fold: WindowFold?): PaneBounds {
    val full = PaneBounds(0, 0, width, height)
    if (fold == null) return full
    val b = fold.bounds
    if (fold.axis == FoldAxis.VERTICAL && b.bottom > windowY && b.top < windowY + height) {
        val start = b.left - windowX
        val end = b.right - windowX
        if (start < width && end > 0) {
            val left = PaneBounds(0, 0, start.coerceIn(0, width), height)
            val right = PaneBounds(end.coerceIn(0, width), 0, width, height)
            return if (left.width >= right.width) left else right
        }
    }
    if (fold.axis == FoldAxis.HORIZONTAL && b.right > windowX && b.left < windowX + width) {
        val start = b.top - windowY
        val end = b.bottom - windowY
        if (start < height && end > 0) {
            val top = PaneBounds(0, 0, width, start.coerceIn(0, height))
            val bottom = PaneBounds(0, end.coerceIn(0, height), width, height)
            return if (top.height >= bottom.height) top else bottom
        }
    }
    return full
}

internal fun workspacePlan(width: Int, height: Int, density: Float, windowX: Int, fold: WindowFold?): WorkspacePlan {
    if (fold?.axis == FoldAxis.VERTICAL) {
        val left = (fold.bounds.left - windowX).coerceIn(0, width)
        val right = (fold.bounds.right - windowX).coerceIn(0, width)
        if (left >= 240 * density && width - right >= 320 * density && height >= 360 * density) {
            return WorkspacePlan(left, right, width - right)
        }
        return WorkspacePlan(0, 0, width)
    }
    if (width >= 760 * density && height >= 400 * density) {
        val sidebar = (width * 0.28f).toInt().coerceIn((260 * density).toInt(), (320 * density).toInt())
        return WorkspacePlan(sidebar, sidebar + 1, width - sidebar - 1)
    }
    return WorkspacePlan(0, 0, width)
}
