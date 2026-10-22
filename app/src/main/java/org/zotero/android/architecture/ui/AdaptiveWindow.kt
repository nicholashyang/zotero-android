package org.zotero.android.architecture.ui

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.background
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInWindow
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.IntOffset
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.window.layout.FoldingFeature
import androidx.window.layout.WindowInfoTracker
import androidx.window.layout.WindowLayoutInfo
import kotlin.math.roundToInt

internal val LocalWindowFold = staticCompositionLocalOf<WindowFold?> { null }
private val LocalWindowTracking = staticCompositionLocalOf { false }
private tailrec fun Context.activity(): Activity? = when (this) {
    is Activity -> this
    is ContextWrapper -> baseContext.activity()
    else -> null
}

@Composable
internal fun ProvideAdaptiveWindow(content: @Composable () -> Unit) {
    val activity = LocalContext.current.activity()
    if (LocalWindowTracking.current || activity == null) { content(); return }
    val flow = remember(activity) { WindowInfoTracker.getOrCreate(activity).windowLayoutInfo(activity) }
    val info by flow.collectAsStateWithLifecycle(initialValue = WindowLayoutInfo(emptyList()))
    val feature = info.displayFeatures.filterIsInstance<FoldingFeature>().firstOrNull {
        it.isSeparating || it.occlusionType == FoldingFeature.OcclusionType.FULL
    }
    val fold = feature?.let {
        WindowFold(PaneBounds(it.bounds.left, it.bounds.top, it.bounds.right, it.bounds.bottom),
            if (it.orientation == FoldingFeature.Orientation.VERTICAL) FoldAxis.VERTICAL else FoldAxis.HORIZONTAL)
    }
    CompositionLocalProvider(LocalWindowFold provides fold, LocalWindowTracking provides true, content = content)
}

/** Also protects full-screen forms and detail pages that do not need two panes. */
@Composable
internal fun FoldSafeContent(modifier: Modifier = Modifier, content: @Composable () -> Unit) {
    val fold = LocalWindowFold.current
    var origin by remember { mutableStateOf(IntOffset.Zero) }
    Layout(modifier = modifier.background(MaterialTheme.colorScheme.background).onGloballyPositioned {
        val p = it.positionInWindow()
        origin = IntOffset(p.x.roundToInt(), p.y.roundToInt())
    }, content = { Box(Modifier.fillMaxSize()) { content() } }) { children, constraints ->
        val w = constraints.maxWidth
        val h = constraints.maxHeight
        val pane = safePane(w, h, origin.x, origin.y, fold)
        val child = children.single().measure(Constraints.fixed(pane.width, pane.height))
        layout(w, h) { child.place(pane.left, pane.top) }
    }
}

/** The content slot stays at the same composition position while folding. */
@Composable
internal fun AdaptiveLibraryWorkspace(sidebar: @Composable () -> Unit, content: @Composable () -> Unit) {
    val fold = LocalWindowFold.current
    val density = LocalDensity.current.density
    var origin by remember { mutableStateOf(IntOffset.Zero) }
    BoxWithConstraints(Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background).onGloballyPositioned {
        val p = it.positionInWindow()
        origin = IntOffset(p.x.roundToInt(), p.y.roundToInt())
    }) {
        val plan = workspacePlan(constraints.maxWidth, constraints.maxHeight, density, origin.x, fold)
        Layout(content = {
            Box { if (plan.hasSidebar) sidebar() }
            Box(Modifier.fillMaxSize()) { content() }
        }) { children, limits ->
            val left = children[0].measure(Constraints.fixed(plan.sidebarWidth, limits.maxHeight))
            val main = children[1].measure(Constraints.fixed(plan.contentWidth, limits.maxHeight))
            layout(limits.maxWidth, limits.maxHeight) {
                left.place(0, 0)
                main.place(plan.contentLeft, 0)
            }
        }
    }
}
