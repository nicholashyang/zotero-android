package org.zotero.android.library

import android.graphics.Bitmap
import androidx.activity.ComponentActivity
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.remember
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.dp
import androidx.compose.ui.test.captureToImage
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onRoot
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import java.io.File
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.zotero.android.architecture.ui.CustomLayoutSize
import org.zotero.android.screens.allitems.AllItemsToolbar
import org.zotero.android.screens.allitems.AllItemsPhoneHeader
import org.zotero.android.screens.allitems.table.AllItemsTable
import org.zotero.android.screens.collections.CollectionsTopBar
import org.zotero.android.screens.collections.rows.CollectionRowItem
import org.zotero.android.screens.itemdetails.rows.ItemDetailsFieldRow
import org.zotero.android.screens.itemdetails.topbars.ItemDetailsTopBar
import org.zotero.android.screens.libraries.LibrariesTopBar
import org.zotero.android.screens.libraries.data.LibraryRowData
import org.zotero.android.screens.libraries.data.LibraryState
import org.zotero.android.screens.libraries.table.LibrariesItem
import org.zotero.android.screens.settings.SettingsContent
import org.zotero.android.screens.settings.SettingsTopBar
import org.zotero.android.sync.Collection
import org.zotero.android.sync.CollectionIdentifier
import org.zotero.android.uicomponents.attachmentprogress.State
import org.zotero.android.uicomponents.loading.CircularLoading
import org.zotero.android.uicomponents.library.LibraryDivider
import org.zotero.android.uicomponents.library.LibraryErrorState
import org.zotero.android.uicomponents.library.LibraryGroup
import org.zotero.android.uicomponents.library.LibraryScaffold
import org.zotero.android.uicomponents.library.LibraryTheme

/** Deterministic, account-free examples made from the production UI components. */
@RunWith(AndroidJUnit4::class)
class LibraryScreenshotTest {
    @get:Rule val compose = createAndroidComposeRule<ComponentActivity>()

    private fun capture(name: String, dark: Boolean = false, fontScale: Float = 1f, content: @Composable () -> Unit) {
        val arguments = InstrumentationRegistry.getArguments()
        val captureFontScale = arguments.getString("captureFontScale")?.toFloatOrNull() ?: fontScale
        val captureDark = arguments.getString("captureDark")?.toBooleanStrictOrNull() ?: dark
        compose.setContent {
            val density = LocalDensity.current.density
            CompositionLocalProvider(LocalDensity provides Density(density, captureFontScale)) {
                LibraryTheme(darkTheme = captureDark, content = content)
            }
        }
        compose.waitForIdle()
        val target = File(compose.activity.filesDir, "ui-screenshots/$name.png")
        target.parentFile!!.mkdirs()
        target.outputStream().use {
            compose.onRoot().captureToImage().asAndroidBitmap().compress(Bitmap.CompressFormat.PNG, 100, it)
        }
    }

    @Test fun itemsLight() = capture("items-light") { ItemsExample() }
    @Test fun itemsDark() = capture("items-dark", dark = true) { ItemsExample() }
    @Test fun itemsLargeText() = capture("items-large-text", fontScale = 2f) { ItemsExample() }
    @Test fun itemsDownloading() = capture("items-downloading") { ItemsExample(downloading = true) }
    @Test fun itemsErrorDark() = capture("items-error-dark", dark = true) {
        LibraryScaffold(topBar = { AllItemsPhoneHeader("All Items", "", {}, {}, TopAppBarDefaults.pinnedScrollBehavior()) }) {
            Box(Modifier.fillMaxSize()) { LibraryErrorState("Unable to load items", Modifier.align(Alignment.Center)) }
        }
    }
    @Test fun itemsLoading() = capture("items-loading") {
        LibraryScaffold(topBar = { AllItemsPhoneHeader("All Items", "", {}, {}, TopAppBarDefaults.pinnedScrollBehavior()) }) {
            Box(Modifier.fillMaxSize()) { CircularLoading() }
        }
    }
    @Test fun itemsEmpty() = capture("items-empty") { ItemsExample(empty = true) }

    @Test fun settingsLight() = capture("settings-light") {
        val scroll = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()
        LibraryScaffold(topBar = { SettingsTopBar({}, scroll) }, scrollBehavior = scroll) {
            SettingsContent({}, {}, {}, {}, {}, {})
        }
    }

    @Test fun settingsDark() = capture("settings-dark", dark = true) {
        val scroll = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()
        LibraryScaffold(topBar = { SettingsTopBar({}, scroll) }, scrollBehavior = scroll) {
            SettingsContent({}, {}, {}, {}, {}, {})
        }
    }

    @Test fun updateLight() = capture("update-light") { UpdateExample() }
    @Test fun updateDark() = capture("update-dark", dark = true) { UpdateExample() }
    @Test fun updateLargeText() = capture("update-large-text", fontScale = 2f) { UpdateExample() }

    @Composable
    private fun UpdateExample() {
        org.zotero.android.appupdate.UpdateSection(
            org.zotero.android.appupdate.AppUpdateState(
                status = org.zotero.android.appupdate.UpdateStatus.READY,
                manifest = org.zotero.android.appupdate.AppUpdateManifest(
                    282, "1.0.0-282", "org.zotero.android.debug", "devDebug", 23,
                    190795550, "a".repeat(64),
                    "https://github.com/nicholashyang/zotero-android/releases/download/dev-v1.0.0-282/Zotero-dev-debug.apk",
                    "Version information and reliable background downloads.",
                ),
            ),
        )
    }

    @Test fun librariesLight() = capture("libraries-light") {
        val scroll = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()
        LibraryScaffold(topBar = { LibrariesTopBar(scroll, {}) }, scrollBehavior = scroll) {
            LazyColumn(contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(24.dp)) {
                item {
                    LibraryGroup { LibrariesItem(LibraryRowData(1, "My Library", LibraryState.normal), {}, {}) }
                }
                item {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text("Group Libraries", modifier = Modifier.padding(start = 16.dp), style = MaterialTheme.typography.titleSmall)
                        LibraryGroup {
                            LibrariesItem(LibraryRowData(2, "Research Lab", LibraryState.normal), {}, {})
                            LibraryDivider()
                            LibrariesItem(LibraryRowData(3, "Reading Group", LibraryState.locked), {}, {})
                        }
                    }
                }
            }
        }
    }

    @Test fun collectionsLight() = capture("collections-light") {
        val scroll = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()
        LibraryScaffold(topBar = { CollectionsTopBar("My Library", scroll, {}, {}) }, scrollBehavior = scroll) {
            Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(24.dp)) {
                LibraryGroup {
                    CollectionExample(Collection.initWithCustomType(CollectionIdentifier.CustomType.all, 128))
                    LibraryDivider()
                    CollectionExample(Collection.initWithCustomType(CollectionIdentifier.CustomType.recentlyRead, 12))
                }
                LibraryGroup {
                    CollectionExample(Collection(CollectionIdentifier.collection("ml"), "Machine Learning", 42), children = true)
                    LibraryDivider()
                    CollectionExample(Collection(CollectionIdentifier.collection("reading"), "待读文献 · Reading list", 18))
                }
                LibraryGroup {
                    CollectionExample(Collection.initWithCustomType(CollectionIdentifier.CustomType.unfiled, 7))
                    LibraryDivider()
                    CollectionExample(Collection.initWithCustomType(CollectionIdentifier.CustomType.trash, 2))
                }
            }
        }
    }

    @Test fun detailsLight() = capture("details-light") {
        LibraryScaffold(topBar = { ItemDetailsTopBar({}, {}, TopAppBarDefaults.pinnedScrollBehavior()) }) {
            LazyColumn(contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(24.dp)) {
                item { Text(sampleItem().title, style = MaterialTheme.typography.headlineSmall) }
                item {
                    LibraryGroup {
                        Column(Modifier.padding(16.dp)) {
                            ItemDetailsFieldRow("Item Type", "Journal Article")
                            ItemDetailsFieldRow("Author", "Ashish Vaswani")
                            ItemDetailsFieldRow("Date", "2017")
                            ItemDetailsFieldRow("Publication", "Advances in Neural Information Processing Systems")
                        }
                    }
                }
                item {
                    LibraryGroup {
                        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text("Abstract", style = MaterialTheme.typography.titleSmall)
                            Text("A reading note about attention, sequence modelling, and how we organize research literature.\n\n把文献信息与阅读笔记放在一起，便于回顾和整理。")
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ItemsExample(empty: Boolean = false, downloading: Boolean = false) {
    var query by rememberSaveable { mutableStateOf("") }
    val scroll = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()
    LibraryScaffold(
        scrollBehavior = scroll,
        topBar = { AllItemsPhoneHeader("All Items", query, { query = it }, {}, scroll) },
        bottomBar = {
            AllItemsToolbar(false, {}, {}, {})
        },
    ) {
        AllItemsTable(
            lazyListState = rememberLazyListState(),
            itemCellModels = remember(empty) { mutableStateListOf(*if (empty) emptyArray() else Array(12) { sampleItem(it) }) },
            isItemSelected = { false }, getItemAccessory = { if (downloading) org.zotero.android.screens.allitems.data.ItemCellModel.Accessory.attachment(State.progress(45)) else org.zotero.android.screens.allitems.data.ItemCellModel.Accessory.doi },
            isEditing = false, isRefreshing = false,
            onItemTapped = {}, onItemLongTapped = {}, onAccessoryTapped = {}, onStartSync = {},
        )
    }
}

@Composable
private fun CollectionExample(collection: Collection, children: Boolean = false) {
    CollectionRowItem(
        layoutType = CustomLayoutSize.LayoutType.small, levelPadding = 4.dp,
        collection = collection, selectedCollectionId = CollectionIdentifier.custom(CollectionIdentifier.CustomType.all),
        showCollectionItemCounts = true, hasChildren = children, isCollapsed = true,
        onItemTapped = {}, onItemLongTapped = {}, onItemChevronTapped = {},
    )
}
