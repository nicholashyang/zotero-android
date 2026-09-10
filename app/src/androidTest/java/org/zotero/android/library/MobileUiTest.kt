package org.zotero.android.library

import android.graphics.Bitmap
import android.view.View
import android.view.ViewGroup
import android.webkit.WebView
import androidx.activity.ComponentActivity
import androidx.compose.foundation.layout.*
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
        val bitmap = compose.onRoot().captureToImage().asAndroidBitmap()
        val dir = File(compose.activity.filesDir, "ui-screenshots").apply { mkdirs() }
        File(dir, "mobile-$name.png").outputStream().use { bitmap.compress(Bitmap.CompressFormat.PNG, 100, it) }
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
