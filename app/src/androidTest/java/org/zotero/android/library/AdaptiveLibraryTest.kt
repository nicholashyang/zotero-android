package org.zotero.android.library

import android.graphics.Bitmap
import android.os.Build
import androidx.activity.ComponentActivity
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.unit.dp
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test
import org.zotero.android.architecture.ui.*
import org.zotero.android.architecture.navigation.ZoteroNavigation
import org.zotero.android.pdf.pdfReaderScreenAndNavigationForTablet
import org.zotero.android.screens.reader.readerNavScreensForTablet
import androidx.navigation.NavGraph
import androidx.navigation.compose.rememberNavController
import androidx.navigation.compose.composable
import androidx.navigation.createGraph
import org.zotero.android.screens.allitems.AllItemsToolbar
import org.zotero.android.screens.allitems.MobileHomeHeader
import org.zotero.android.screens.allitems.HomeCollectionsSidebar
import org.zotero.android.screens.allitems.table.AllItemsTable
import org.zotero.android.screens.settings.SettingsContent
import org.zotero.android.screens.settings.SettingsTopBar
import org.zotero.android.screens.settings.MobilePreferencesGroup
import org.zotero.android.sync.Collection
import org.zotero.android.sync.CollectionIdentifier
import org.zotero.android.uicomponents.library.*
import java.io.File

class AdaptiveLibraryTest {
    @get:Rule val compose = createAndroidComposeRule<ComponentActivity>()

    @Test fun resizingKeepsTheContentAndSearchState() {
        var expanded by mutableStateOf(false)
        var compositions = 0
        compose.setContent { LibraryTheme {
            Box(Modifier.requiredSize(if (expanded) 900.dp else 390.dp, 800.dp)) {
                AdaptiveLibraryWorkspace(sidebar = { Text("Sidebar") }) {
                    DisposableEffect(Unit) { compositions++; onDispose {} }
                    var query by rememberSaveable { mutableStateOf("") }
                    Column { Text("Current document"); LibrarySearchField(query) { query = it } }
                }
            }
        } }
        compose.onNode(hasSetTextAction()).performTextInput("saved research")
        compose.runOnIdle { expanded = true }
        compose.onNode(hasSetTextAction()).assertTextEquals("saved research")
        compose.runOnIdle { expanded = false }
        compose.onNode(hasSetTextAction()).assertTextEquals("saved research")
        compose.runOnIdle { assertEquals(1, compositions) }
    }

    @Test fun stableGraphContainsToolsForBothReaderLayouts() {
        lateinit var graph: NavGraph
        compose.setContent {
            val controller = rememberNavController()
            val navigation = remember(controller) { ZoteroNavigation(controller, null) }
            graph = remember(controller) { controller.createGraph(startDestination = "sentinel") {
                composable("sentinel") {}
                pdfReaderScreenAndNavigationForTablet({}, navigation, controller, {})
                readerNavScreensForTablet({}, {}, navigation, controller)
            } }
            Text("Navigation ready")
        }
        compose.runOnIdle {
            listOf("pdfAnnotationNavigation", "pdfAnnotationMoreNavigation", "pdfColorPicker",
                "readerAnnotationNavigation", "readerAnnotationMoreNavigation", "readerColorPicker")
                .forEach { assertNotNull("Missing reader route: $it", graph.findNode(it)) }
        }
    }

    @Test fun singlePaneDoesNotCrossASeparatingHinge() {
        val d = compose.activity.resources.displayMetrics.density
        val width = compose.activity.resources.configuration.screenWidthDp
        val center = width * d / 2
        compose.setContent { LibraryTheme {
            CompositionLocalProvider(LocalWindowFold provides WindowFold(
                PaneBounds((center - 10*d).toInt(), 0, (center + 10*d).toInt(), 10000), FoldAxis.VERTICAL)) {
                FoldSafeContent(Modifier.fillMaxSize()) { Box(Modifier.fillMaxSize().testTag("safe-pane")) }
            }
        } }
        assertTrue(compose.onNodeWithTag("safe-pane").fetchSemanticsNode().boundsInRoot.width <= (width/2 + 20) * d)
    }

    @Test fun homePreview() = capture("home") { NativeHomeExample() }
    @Test fun darkHomePreview() = capture("home-dark", true) { NativeHomeExample() }
    @Test fun separatingHingePreview() = capture("home-hinge") {
        val density = LocalDensity.current.density
        val config = LocalConfiguration.current
        val center = (config.screenWidthDp * density / 2).toInt()
        CompositionLocalProvider(LocalWindowFold provides WindowFold(
            PaneBounds(center - (8*density).toInt(), 0, center + (8*density).toInt(),
                (config.screenHeightDp*density).toInt()), FoldAxis.VERTICAL)) {
            NativeHomeExample()
        }
    }
    @Test fun settingsPreview() = capture("settings") {
        val scroll = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()
        LibraryScaffold(topBar = { SettingsTopBar({}, scroll) }, scrollBehavior = scroll) {
            SettingsContent({}, {}, {}, {}, {}, {}, preferencesContent = { MobilePreferencesGroup {} })
        }
    }

    private fun capture(name: String, dark: Boolean = false, content: @Composable () -> Unit) {
        compose.setContent { LibraryTheme(darkTheme = dark, content = content) }
        compose.waitForIdle()
        // Window-coordinate callbacks can request a second layout pass. Capture
        // after those frames, rather than during the first display-list update.
        repeat(2) { compose.mainClock.advanceTimeByFrame(); compose.waitForIdle() }
        if (name == "home-hinge" && compose.activity.resources.configuration.screenWidthDp >= 700) {
            compose.onNodeWithText("Research").assertIsDisplayed()
        }
        val target = File(compose.activity.filesDir, "ui-screenshots/native-$name.png")
        target.parentFile!!.mkdirs()
        val bitmap = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            compose.onRoot().captureToImage().asAndroidBitmap()
        } else {
            checkNotNull(InstrumentationRegistry.getInstrumentation().uiAutomation.takeScreenshot())
        }
        target.outputStream().use { bitmap.compress(Bitmap.CompressFormat.PNG, 100, it) }
        bitmap.recycle()
    }
}

@Composable
private fun NativeHomeExample() {
    val layout = CustomLayoutSize.calculateLayoutType()
    var query by rememberSaveable { mutableStateOf("") }
    val scroll = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()
    val collections = remember { listOf(
        Collection.initWithCustomType(CollectionIdentifier.CustomType.all),
        Collection.initWithCustomType(CollectionIdentifier.CustomType.recentlyRead),
        Collection(CollectionIdentifier.collection("research"), "Research", 0),
        Collection(CollectionIdentifier.collection("reading"), "Reading list", 0),
        Collection.initWithCustomType(CollectionIdentifier.CustomType.unfiled),
        Collection.initWithCustomType(CollectionIdentifier.CustomType.trash)) }
    AdaptiveLibraryWorkspace(sidebar = { HomeCollectionsSidebar("My Library", "All Items", collections, {}, {}) }) {
        LibraryScaffold(topBar = {
            MobileHomeHeader("My Library", "All Items", query, layout.isTablet(), scroll, {}, {}, {}, { query = it })
        }, scrollBehavior = scroll, bottomBar = { AllItemsToolbar(false, {}, {}, {}) }) {
            AllItemsTable(rememberLazyListState(), remember { mutableStateListOf(*Array(12) { sampleItem(it) }) },
                { false }, { org.zotero.android.screens.allitems.data.ItemCellModel.Accessory.doi },
                false, false, {}, {}, {}, {})
        }
    }
}
