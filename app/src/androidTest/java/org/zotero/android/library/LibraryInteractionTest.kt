package org.zotero.android.library

import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.Surface
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsOff
import androidx.compose.ui.test.hasScrollToIndexAction
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performScrollToIndex
import androidx.compose.ui.test.junit4.StateRestorationTester
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.compose.ui.test.assertIsNotFocused
import androidx.compose.ui.test.assertIsOn
import androidx.compose.ui.test.hasSetTextAction
import androidx.compose.ui.test.isToggleable
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performTextInput
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.Before
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.zotero.android.screens.allitems.AllItemsPhoneHeader
import org.zotero.android.screens.allitems.AllItemsToolbar
import org.zotero.android.screens.allitems.table.AllItemsTable
import org.zotero.android.screens.allitems.data.ItemCellModel
import org.zotero.android.screens.allitems.table.rows.ItemRow
import org.zotero.android.screens.itemdetails.topbars.ItemDetailsEditingTopBar
import org.zotero.android.screens.settings.SettingsContent
import org.zotero.android.uicomponents.Strings
import org.zotero.android.uicomponents.library.LibraryScaffold
import org.zotero.android.uicomponents.library.LibrarySearchField
import org.zotero.android.uicomponents.library.LibraryTheme

@RunWith(AndroidJUnit4::class)
class LibraryInteractionTest {
    @get:Rule val compose = createAndroidComposeRule<ComponentActivity>()

    private fun text(id: Int) = compose.activity.getString(id)

    @Before fun useTouchInput() {
        // Older emulators start in keyboard mode and automatically refocus the first field.
        InstrumentationRegistry.getInstrumentation().setInTouchMode(true)
    }

    @Test fun searchClearAndCancelUpdateTheSameQuery() {
        var query by mutableStateOf("")
        compose.setContent {
            LibraryTheme { LibrarySearchField(query) { query = it } }
        }
        compose.onNode(hasSetTextAction()).performTextInput("文献 PDF")
        compose.runOnIdle { assertEquals("文献 PDF", query) }
        compose.onNodeWithContentDescription(text(Strings.searchbar_accessibility_clear)).performClick()
        compose.runOnIdle { assertEquals("", query) }
        compose.onNode(hasSetTextAction()).performTextInput("Zotero")
        compose.onNodeWithText(text(Strings.cancel)).performClick()
        compose.runOnIdle { assertEquals("", query) }
        compose.onNode(hasSetTextAction()).assertIsNotFocused()
    }

    @Test fun backDismissesSearchBeforeLeavingTheList() {
        var query by mutableStateOf("")
        var backCount = 0
        compose.setContent {
            LibraryTheme {
                BackHandler { backCount++ }
                AllItemsPhoneHeader(
                    title = "All Items", query = query, onQueryChange = { query = it },
                    onBack = { backCount++ },
                    scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior(),
                )
            }
        }
        compose.onNode(hasSetTextAction()).performTextInput("retained query")
        compose.runOnIdle { compose.activity.onBackPressedDispatcher.onBackPressed() }
        compose.runOnIdle {
            assertEquals("retained query", query)
            assertEquals(0, backCount)
        }
        compose.onNodeWithContentDescription(text(Strings.back)).performClick()
        compose.runOnIdle { assertEquals(1, backCount) }
    }

    @Test fun detailsAndSelectionDoNotTriggerTheAttachmentOpenAction() {
        var editing by mutableStateOf(false)
        var selected by mutableStateOf(false)
        var openCount = 0
        var detailsCount = 0
        val item = sampleItem()
        compose.setContent {
            LibraryTheme {
                Surface {
                    ItemRow(
                        cellModel = item, itemAccessory = item.accessory,
                        isItemSelected = { selected }, isEditing = editing,
                        onItemTapped = { if (editing) selected = !selected else openCount++ },
                        onItemLongTapped = { editing = true },
                        onAccessoryTapped = { detailsCount++ },
                    )
                }
            }
        }
        compose.onNodeWithContentDescription(text(Strings.all_items_details)).performClick()
        compose.runOnIdle { assertEquals(1, detailsCount); assertEquals(0, openCount) }
        compose.onNodeWithText(item.title).performClick()
        compose.runOnIdle { assertEquals(1, openCount); editing = true }
        compose.onNode(isToggleable()).performClick().assertIsOn()
        compose.runOnIdle { assertEquals(1, openCount); assertEquals(true, selected) }
    }

    @Test fun cancelAndSaveHaveSeparateCallbacks() {
        var cancelCount = 0
        var saveCount = 0
        compose.setContent {
            LibraryTheme {
                ItemDetailsEditingTopBar(
                    onViewOrEditClicked = { saveCount++ },
                    onCancelOrBackClicked = { cancelCount++ },
                )
            }
        }
        compose.onNodeWithText(text(Strings.cancel)).performClick()
        compose.runOnIdle { assertEquals(1, cancelCount); assertEquals(0, saveCount) }
        compose.onNodeWithText(text(Strings.save)).performClick()
        compose.runOnIdle { assertEquals(1, saveCount) }
    }

    @Test fun multipleRowsCanBeSelectedIndependently() {
        val selected = mutableStateListOf<String>()
        compose.setContent {
            LibraryTheme {
                Column {
                    repeat(2) { index ->
                        ItemRow(
                            cellModel = sampleItem(index), itemAccessory = null,
                            isItemSelected = { it in selected }, isEditing = true,
                            onItemTapped = { if (!selected.remove(it.key)) selected.add(it.key) },
                            onItemLongTapped = {}, onAccessoryTapped = {},
                        )
                    }
                }
            }
        }
        compose.onAllNodes(isToggleable())[0].performClick().assertIsOn()
        compose.onAllNodes(isToggleable())[1].performClick().assertIsOn()
        compose.runOnIdle { assertEquals(2, selected.size) }
        compose.mainClock.advanceTimeBy(700)
        compose.onAllNodes(isToggleable())[0].performClick().assertIsOff()
        compose.runOnIdle { assertEquals(listOf("fixture-1"), selected.toList()) }
    }

    @Test fun longListRetainsPositionAndQueryAcrossSavedStateRestoration() {
        val restoration = StateRestorationTester(compose)
        val items = mutableStateListOf(*Array(200) { sampleItem(it) })
        lateinit var state: LazyListState
        restoration.setContent {
            var query by rememberSaveable { mutableStateOf("attention") }
            state = rememberLazyListState()
            LibraryTheme {
                LibraryScaffold(topBar = { LibrarySearchField(query) { query = it } }) {
                    AllItemsTable(state, items, { false }, { null }, false, false, {}, {}, {}, {})
                }
            }
        }
        compose.onNode(hasScrollToIndexAction()).performScrollToIndex(120)
        val position = compose.runOnIdle { state.firstVisibleItemIndex to state.firstVisibleItemScrollOffset }
        // Accessory/title updates must retain the same lazy identity and viewport.
        compose.runOnIdle { items[120] = items[120].copy(subtitle = "Downloaded") }
        restoration.emulateSavedInstanceStateRestore()
        compose.runOnIdle {
            assertEquals(position, state.firstVisibleItemIndex to state.firstVisibleItemScrollOffset)
        }
        compose.onNodeWithText("attention").assertIsDisplayed()
    }

    @Test fun lastRowCanBeReachedAboveToolbarWithKeyboardOpen() {
        val items = mutableStateListOf(*Array(30) { sampleItem(it) })
        compose.setContent {
            var query by remember { mutableStateOf("") }
            LibraryTheme {
                LibraryScaffold(
                    topBar = { LibrarySearchField(query) { query = it } },
                    bottomBar = { Box(Modifier.testTag("toolbar")) { AllItemsToolbar(false, {}, {}, {}) } },
                ) {
                    AllItemsTable(rememberLazyListState(), items, { false }, { null }, false, false, {}, {}, {}, {})
                }
            }
        }
        compose.onNode(hasSetTextAction()).performTextInput("research")
        compose.waitUntil(10_000) {
            ViewCompat.getRootWindowInsets(compose.activity.window.decorView)
                ?.isVisible(WindowInsetsCompat.Type.ime()) == true
        }
        compose.onNode(hasScrollToIndexAction()).performScrollToIndex(29)
        compose.onNodeWithText(items.last().title).assertIsDisplayed()
        val itemBottom = compose.onNodeWithText(items.last().title).fetchSemanticsNode().boundsInRoot.bottom
        val toolbarTop = compose.onNodeWithTag("toolbar").fetchSemanticsNode().boundsInRoot.top
        assertTrue("Last row must remain above the toolbar", itemBottom <= toolbarTop)
    }

    @Test fun everySettingsDestinationRemainsReachable() {
        val counts = IntArray(6)
        compose.setContent {
            LibraryTheme {
                LibraryScaffold(topBar = {}) {
                    SettingsContent(
                        { counts[0]++ }, { counts[1]++ }, { counts[2]++ },
                        { counts[3]++ }, { counts[4]++ }, { counts[5]++ },
                    )
                }
            }
        }
        listOf(
            Strings.settings_sync_account, Strings.settings_export_title, Strings.settings_cite_title,
            Strings.settings_debug, Strings.support_feedback, Strings.privacy_policy,
        ).forEach { compose.onNodeWithText(text(it)).performScrollTo().assertIsDisplayed().performClick() }
        compose.runOnIdle { counts.forEach { assertEquals(1, it) } }
    }
}

internal fun sampleItem(index: Int = 0) = ItemCellModel(
    key = "fixture-$index",
    typeIconName = "item_type_journalarticle",
    typeName = "Journal Article",
    title = if (index == 0) "Attention Is All You Need: Understanding Attention in Scientific Literature" else "研究文献 $index · Reading and organizing scientific papers",
    subtitle = "Vaswani et al. · 2017",
    hasNote = true,
    accessory = ItemCellModel.Accessory.doi,
    tagColors = mutableStateListOf(Color(0xFF3877B8), Color(0xFFD6A847)),
)
