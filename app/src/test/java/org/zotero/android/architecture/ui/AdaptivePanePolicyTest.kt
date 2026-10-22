package org.zotero.android.architecture.ui

import org.junit.Assert.*
import org.junit.Test

class AdaptivePanePolicyTest {
    @Test fun coverDisplayAndNarrowMultiWindowUseOnePane() {
        assertFalse(workspacePlan(393, 850, 1f, 0, null).hasSidebar)
        assertFalse(workspacePlan(700, 850, 1f, 0, null).hasSidebar)
    }
    @Test fun unfoldedWindowUsesBoundedSidebar() {
        val plan = workspacePlan(840, 900, 1f, 0, null)
        assertEquals(260, plan.sidebarWidth)
        assertEquals(579, plan.contentWidth)
        assertEquals(840, plan.contentLeft + plan.contentWidth)
        assertEquals(320, workspacePlan(1600, 900, 1f, 0, null).sidebarWidth)
    }
    @Test fun shortLandscapeWindowKeepsRoomForControls() {
        assertFalse(workspacePlan(900, 350, 1f, 0, null).hasSidebar)
    }
    @Test fun breakpointsUseDensityIndependentPixels() {
        val plan = workspacePlan(1680, 1800, 2f, 0, null)
        assertEquals(520, plan.sidebarWidth)
        assertFalse(workspacePlan(1400, 1800, 2f, 0, null).hasSidebar)
    }
    @Test fun verticalHingeIsTheGutterBetweenPanes() {
        val fold = WindowFold(PaneBounds(400, 0, 424, 900), FoldAxis.VERTICAL)
        assertEquals(WorkspacePlan(400, 424, 416), workspacePlan(840, 900, 1f, 0, fold))
    }
    @Test fun foldThatLeavesTooLittleContentFallsBackToSafePane() {
        val fold = WindowFold(PaneBounds(600, 0, 620, 900), FoldAxis.VERTICAL)
        assertFalse(workspacePlan(840, 900, 1f, 0, fold).hasSidebar)
        assertEquals(PaneBounds(0, 0, 600, 900), safePane(840, 900, 0, 0, fold))
    }
    @Test fun zeroWidthSeparatingFoldStillSplitsContent() {
        val fold = WindowFold(PaneBounds(420, 0, 420, 900), FoldAxis.VERTICAL)
        assertEquals(WorkspacePlan(420, 420, 420), workspacePlan(840, 900, 1f, 0, fold))
        assertEquals(420, safePane(840, 900, 0, 0, fold).width)
    }
    @Test fun rightPaneAlreadyBesideHingeIsUnchanged() {
        val fold = WindowFold(PaneBounds(400, 0, 424, 900), FoldAxis.VERTICAL)
        assertEquals(PaneBounds(0, 0, 416, 900), safePane(416, 900, 424, 0, fold))
        assertEquals(PaneBounds(0, 0, 400, 900), safePane(400, 900, 0, 0, fold))
    }
    @Test fun tabletopControlsStayEntirelyInsideOneUsableHalf() {
        val fold = WindowFold(PaneBounds(0, 400, 840, 424), FoldAxis.HORIZONTAL)
        assertEquals(PaneBounds(0, 424, 840, 900), safePane(840, 900, 0, 0, fold))
    }
    @Test fun nestedContainersTranslateWindowCoordinates() {
        val fold = WindowFold(PaneBounds(0, 500, 840, 524), FoldAxis.HORIZONTAL)
        assertEquals(PaneBounds(0, 0, 840, 400), safePane(840, 800, 0, 100, fold))
        val vertical = WindowFold(PaneBounds(450, 0, 474, 900), FoldAxis.VERTICAL)
        assertEquals(WorkspacePlan(400, 424, 416), workspacePlan(840, 900, 1f, 50, vertical))
    }
    @Test fun foldOutsideWindowDoesNotChangeLayout() {
        val fold = WindowFold(PaneBounds(900, 0, 920, 900), FoldAxis.VERTICAL)
        assertEquals(PaneBounds(0, 0, 400, 800), safePane(400, 800, 0, 0, fold))
        val lower = WindowFold(PaneBounds(0, 900, 840, 920), FoldAxis.HORIZONTAL)
        assertEquals(PaneBounds(0, 0, 400, 800), safePane(400, 800, 0, 0, lower))
    }
}
