package org.zotero.android.library

import android.graphics.Bitmap
import android.os.Build
import android.view.View
import android.view.ViewGroup
import android.webkit.WebView
import androidx.activity.ComponentActivity
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.dp
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.*
import org.junit.Assert.*
import org.zotero.android.R
import org.zotero.android.preferences.*
import org.zotero.android.screens.login.LoginViewState
import org.zotero.android.screens.login.NativeLoginForm
import org.zotero.android.screens.settings.MobilePreferencesGroup
import org.zotero.android.uicomponents.library.*
import org.zotero.android.uicomponents.math.MathText
import java.io.File
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit

class MobileUiTest {
    @get:Rule val compose = createAndroidComposeRule<ComponentActivity>()
    private lateinit var original: AppPreferencesState
    @Before fun setup() {
        InstrumentationRegistry.getInstrumentation().setInTouchMode(true)
        original = AppPreferences.get(compose.activity).state.value
        AppPreferences.get(compose.activity).apply { appearance(Appearance.SYSTEM); swipe(true, SwipeAction.NONE); swipe(false, SwipeAction.NONE) }
    }
    @After fun restore() { AppPreferences.get(compose.activity).apply { appearance(original.appearance); swipe(true, original.leftSwipe); swipe(false, original.rightSwipe) } }
    private fun text(id: Int) = compose.activity.getString(id)
    private fun screenshot(name: String) {
        val bitmap = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            compose.onRoot().captureToImage().asAndroidBitmap()
        } else {
            checkNotNull(InstrumentationRegistry.getInstrumentation().uiAutomation.takeScreenshot())
        }
        val dir = File(compose.activity.filesDir, "ui-screenshots").apply { mkdirs() }
        File(dir, "mobile-$name.png").outputStream().use { bitmap.compress(Bitmap.CompressFormat.PNG, 100, it) }
        bitmap.recycle()
    }
    @Test fun loginFormSupportsSubmitAndClearsPassword() {
        var submitted: Pair<String, String>? = null
        compose.setContent { LibraryTheme { Surface { NativeLoginForm(LoginViewState(), { u,p -> submitted = u to p }, {}, {}, {}) } } }
        compose.onNodeWithText(text(R.string.mobile_login)).assertIsNotEnabled()
        compose.onNodeWithText(text(R.string.mobile_username)).performTextInput("example")
        compose.onNodeWithText(text(R.string.mobile_password)).performTextInput("test-password")
        compose.onNodeWithText(text(R.string.mobile_login)).performScrollTo().performClick()
        compose.runOnIdle { assertEquals("example" to "test-password", submitted) }
        compose.onNodeWithText(text(R.string.mobile_login)).assertIsNotEnabled()
        screenshot("login")
    }
    @Test fun busyLoginPreventsResubmissionAndWebFallbackRemainsAvailable() {
        var fallback = false
        compose.setContent { LibraryTheme { NativeLoginForm(LoginViewState(nativeBusy = true), { _,_ -> fail("Duplicate login") }, { fallback = true }, {}, {}) } }
        compose.onNodeWithText(text(R.string.mobile_login_busy)).assertIsNotEnabled()
        compose.onNodeWithText(text(R.string.mobile_login_web)).performScrollTo().performClick()
        compose.runOnIdle { assertTrue(fallback) }
    }
    @Test fun preferencesOfferAllThreeThemesAndIndependentSwipes() {
        val prefs = AppPreferences.get(compose.activity)
        compose.setContent { LibraryTheme { MobilePreferencesGroup {} } }
        compose.onNodeWithText(text(R.string.mobile_appearance)).performClick()
        compose.onNodeWithText(text(R.string.mobile_theme_dark)).performClick()
        compose.runOnIdle { assertEquals(Appearance.DARK, prefs.state.value.appearance) }
        compose.onNodeWithText(text(R.string.mobile_swipe_left)).performClick()
        compose.onNodeWithText(text(R.string.mobile_swipe_collection)).performClick()
        compose.runOnIdle { assertEquals(SwipeAction.COLLECTION, prefs.state.value.leftSwipe); assertEquals(SwipeAction.NONE, prefs.state.value.rightSwipe) }
        screenshot("settings-dark")
    }
    @Test fun swipeRevealsActionButDoesNotExecuteUntilTapped() {
        var count = 0
        compose.setContent { LibraryTheme {
            Box(Modifier.fillMaxWidth().testTag("row")) {
                SwipeReveal("item", SwipeAction.COLLECTION, SwipeAction.NONE, { count++ }) { _,_ ->
                    Text("A document", Modifier.fillMaxWidth().height(80.dp))
                }
            }
        } }
        compose.onNodeWithTag("row").performTouchInput { swipeLeft() }
        compose.runOnIdle { assertEquals(0, count) }
        compose.onNodeWithText(text(R.string.mobile_swipe_collection)).performClick()
        compose.runOnIdle { assertEquals(1, count) }
    }
    @Test fun pageSwipeGutterDoesNotExecuteCardAction() {
        var pages = 0
        var cards = 0
        compose.setContent { LibraryTheme {
            Box(Modifier.fillMaxWidth().pageSwipe { pages++ }.testTag("page")) {
                SwipeReveal("gutter", SwipeAction.OPEN, SwipeAction.COLLECTION, { cards++ }) { _,_ ->
                    Text("A document", Modifier.fillMaxWidth().height(100.dp))
                }
            }
        } }
        val density = compose.activity.resources.displayMetrics.density
        compose.onNodeWithTag("page").performTouchInput {
            swipe(Offset(40f * density, centerY), Offset(140f * density, centerY))
        }
        compose.runOnIdle { assertEquals(1, pages); assertEquals(0, cards) }
    }
    @Test fun homeHeaderExposesLibraryAndSettingsOnPhone() { homeFixture(false, "home-phone") }
    @Test fun homeHeaderExposesLibraryAndSettingsOnTablet() { homeFixture(true, "home-tablet") }
    private fun homeFixture(tablet: Boolean, name: String) {
        var libraries = 0
        var settings = 0
        compose.setContent { LibraryTheme { Surface {
            org.zotero.android.screens.allitems.MobileHomeHeader("My Library", "All Items", "", tablet,
                TopAppBarDefaults.pinnedScrollBehavior(), { libraries++ }, {}, { settings++ }, {})
        } } }
        compose.onNodeWithText("My Library ▾").performClick()
        compose.onNodeWithContentDescription(text(org.zotero.android.uicomponents.Strings.settings_title)).performClick()
        compose.runOnIdle { assertEquals(1, libraries); assertEquals(1, settings) }
        screenshot(name)
    }
    @Test fun permissionsPageShowsSystemPermissionState() {
        compose.setContent { LibraryTheme { org.zotero.android.screens.settings.AppPermissionsScreen {} } }
        compose.onNodeWithText(text(R.string.mobile_camera), substring = true).assertIsDisplayed()
        compose.onNodeWithText(text(R.string.mobile_notifications), substring = true).assertIsDisplayed()
        screenshot("permissions")
    }
    @Test fun mathRendersOfflineInLightMode() { mathFixture(false, 1f, "math-light") }
    @Test fun mathRendersOfflineInDarkModeWithLargeText() { mathFixture(true, 2f, "math-dark-large") }
    private fun webViews(view: View): List<WebView> = when (view) {
        is WebView -> listOf(view)
        is ViewGroup -> (0 until view.childCount).flatMap { webViews(view.getChildAt(it)) }
        else -> emptyList()
    }
    private fun awaitWebCondition(script: String) {
        var matched = false
        val deadline = System.currentTimeMillis() + 25000
        while (!matched && System.currentTimeMillis() < deadline) {
            val latch = CountDownLatch(1)
            compose.runOnIdle {
                val view = webViews(compose.activity.window.decorView).firstOrNull()
                if (view == null) latch.countDown() else view.evaluateJavascript(script) {
                    matched = it == "true"; latch.countDown()
                }
            }
            latch.await(2, TimeUnit.SECONDS)
            if (!matched) Thread.sleep(100)
        }
        assertTrue("Offline formula condition failed: $script", matched)
    }
    @Test fun fullWidthFormulaScrollsAndInvalidTexShowsSource() {
        val equation = (1..40).joinToString("+") { "x_{$it}" }
        compose.setContent { LibraryTheme { Surface {
            MathText("\\[$equation\\] \\(\\unknownZoteroCommand\\)", MaterialTheme.colorScheme.onSurface,
                MaterialTheme.typography.bodyLarge, Modifier.padding(16.dp))
        } } }
        awaitWebCondition("(function(){var b=document.querySelector('.block');return !!b && b.scrollWidth>b.clientWidth && document.body.textContent.indexOf('unknownZoteroCommand')>=0})()")
        awaitWebCondition("(function(){var b=document.querySelector('.block');b.scrollLeft=60;return b.scrollLeft>0})()")
    }
    @Test fun twoHundredFormulaRowsCanScrollWithBoundedAttachedWebViews() {
        compose.setContent { LibraryTheme { Surface {
            LazyColumn(Modifier.testTag("formula-list")) {
                items((0 until 200).toList(), key = { it }) { index ->
                    MathText("Paper $index: \\(E=mc^2\\)", MaterialTheme.colorScheme.onSurface,
                        MaterialTheme.typography.bodyLarge, Modifier.fillMaxWidth().heightIn(min = 90.dp).padding(12.dp), maxLines = 2)
                }
            }
        } } }
        awaitWebCondition("document.querySelectorAll('mjx-container svg').length > 0")
        for (index in listOf(30, 60, 100, 150, 199, 0)) {
            compose.onNodeWithTag("formula-list").performScrollToIndex(index)
            compose.waitForIdle()
            compose.runOnIdle { assertTrue(webViews(compose.activity.window.decorView).size in 1..20) }
        }
        awaitWebCondition("document.querySelectorAll('mjx-container svg').length > 0")
    }
    private fun mathFixture(dark: Boolean, scale: Float, name: String) {
        compose.setContent {
            CompositionLocalProvider(LocalDensity provides Density(LocalDensity.current.density, scale)) {
                LibraryTheme(darkTheme = dark) {
                    Surface { Column(Modifier.fillMaxWidth().padding(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                        MathText("研究 \\(E=mc^2\\) and \\(\\frac{a}{b}\\)", MaterialTheme.colorScheme.onSurface, MaterialTheme.typography.bodyLarge, maxLines = 2)
                        MathText("Abstract: \\[\\sum_{i=1}^{n} x_i^2\\]", MaterialTheme.colorScheme.onSurfaceVariant, MaterialTheme.typography.bodyMedium, maxLines = 3)
                    } }
                }
            }
        }
        compose.waitForIdle()
        var rendered = false
        val deadline = System.currentTimeMillis() + 25000
        while (!rendered && System.currentTimeMillis() < deadline) {
            val latch = CountDownLatch(1)
            compose.runOnIdle {
                fun find(view: View): WebView? {
                    if (view is WebView) return view
                    if (view is ViewGroup) for (i in 0 until view.childCount) find(view.getChildAt(i))?.let { return it }
                    return null
                }
                val view = find(compose.activity.window.decorView)
                if (view == null) latch.countDown() else view.evaluateJavascript("document.querySelectorAll('mjx-container svg').length > 0 && document.querySelectorAll('mjx-assistive-mml').length === 0") {
                    rendered = it == "true"; latch.countDown()
                }
            }
            latch.await(2, TimeUnit.SECONDS)
            if (!rendered) Thread.sleep(100)
        }
        assertTrue("Local MathJax must produce SVG, not just display source text", rendered)
        screenshot(name)
    }
}
