package org.zotero.android.library

import android.content.Context
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createEmptyComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performScrollTo
import androidx.test.core.app.ActivityScenario
import androidx.test.platform.app.InstrumentationRegistry
import dagger.hilt.android.EntryPointAccessors
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeout
import org.junit.Assert.*
import org.junit.Assume.assumeTrue
import org.junit.Rule
import org.junit.Test
import org.zotero.android.ZoteroApplication
import org.zotero.android.appupdate.*

/** Opt-in production integration checks on an empty emulator, using the normal runner. */
class UpdateLiveTest {
    @get:Rule val compose = createEmptyComposeRule()
    private val context get() = InstrumentationRegistry.getInstrumentation().targetContext

    @Test fun opensProductionUpdateScreen() {
        assumeTrue(context.applicationContext is ZoteroApplication)
        ActivityScenario.launch(UpdateActivity::class.java).use {
            compose.onNodeWithText("Software Update").assertIsDisplayed()
        }
    }

    @Test fun downloadsAndVerifiesPublishedUpdate(): Unit = runBlocking {
        assumeTrue(InstrumentationRegistry.getArguments().getString("runLiveUpdateTests") == "true")
        assumeTrue(context.applicationContext is ZoteroApplication)
        val repository = EntryPointAccessors.fromApplication(context, UpdateEntryPoint::class.java).updateRepository()
        var checked = false
        repeat(3) {
            if (!checked) {
                checked = repository.check()
                if (!checked) delay(2000)
            }
        }
        assertTrue("Published update check failed: ${repository.state.value.error}", checked)
        assertEquals(UpdateStatus.AVAILABLE, repository.state.value.status)
        repository.download()
        withTimeout(15 * 60 * 1000L) {
            while (repository.state.value.status != UpdateStatus.READY) {
                delay(1000)
                repository.refresh()
                assertNotEquals(repository.state.value.toString(), UpdateStatus.ERROR, repository.state.value.status)
            }
        }
        assertNotNull(repository.fileForInstall())
        context.getSharedPreferences("update_acceptance", Context.MODE_PRIVATE).edit()
            .putBoolean("download_verified", true).commit()
        ActivityScenario.launch(UpdateActivity::class.java).use {
            compose.onNodeWithText("Install Update").performScrollTo().assertIsDisplayed()
        }
    }
}
