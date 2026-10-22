package org.zotero.android.library

import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.pdf.PdfDocument
import android.os.Build
import androidx.activity.ComponentActivity
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Surface
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.captureToImage
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.zotero.android.covers.ItemCoverLoader
import org.zotero.android.covers.ItemCoverSource
import org.zotero.android.screens.allitems.data.ItemCellModel
import org.zotero.android.screens.allitems.table.rows.ItemRow
import org.zotero.android.uicomponents.library.LibraryTheme
import java.io.ByteArrayOutputStream
import java.io.File
import java.util.UUID
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream

@RunWith(AndroidJUnit4::class)
class ItemCoverTest {
    @get:Rule val compose = createAndroidComposeRule<ComponentActivity>()
    private val context get() = InstrumentationRegistry.getInstrumentation().targetContext
    private val directory by lazy { File(context.cacheDir, "cover-test-${UUID.randomUUID()}").apply { mkdirs() } }

    @After fun cleanUp() { directory.deleteRecursively() }

    @Test fun rendersOnlyTheFirstPdfPage() = runBlocking {
        val file = File(directory, "document.pdf")
        val pdf = PdfDocument()
        try {
            listOf(Color.RED, Color.BLUE).forEachIndexed { index, color ->
                val page = pdf.startPage(PdfDocument.PageInfo.Builder(300, 450, index + 1).create())
                page.canvas.drawColor(color)
                pdf.finishPage(page)
            }
            file.outputStream().use { pdf.writeTo(it) }
        } finally {
            pdf.close()
        }
        val bitmap = load(file, "application/pdf")!!
        assertTrue(bitmap.width <= 256 && bitmap.height <= 384)
        assertEquals(Color.RED, bitmap.getPixel(bitmap.width / 2, bitmap.height / 2))
    }

    @Test fun readsEpub3CoverImageWithRelativeEncodedPath() = runBlocking {
        val file = epub("epub3", """
            <manifest><item id="cover" href="../images/front%20cover.png" media-type="image/png" properties="nav cover-image"/></manifest>
        """, mapOf("images/front cover.png" to imageBytes(Color.GREEN)))
        assertColor(Color.GREEN, load(file, "application/epub+zip"))
    }

    @Test fun readsEpub2CoverMetadata() = runBlocking {
        val file = epub("epub2", """
            <metadata><meta name="cover" content="jacket"/></metadata>
            <manifest><item id="jacket" href="cover.png" media-type="image/png"/></manifest>
        """, mapOf("OPS/cover.png" to imageBytes(Color.BLUE)))
        assertColor(Color.BLUE, load(file, "application/epub+zip"))
    }

    @Test fun readsCoverPageAndSvgImageReferenceWithoutLoadingExternalDtd() = runBlocking {
        val page = """<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.1//EN" "http://example.invalid/cover.dtd">
            <html xmlns="http://www.w3.org/1999/xhtml" xmlns:xlink="http://www.w3.org/1999/xlink"><body>
            <svg xmlns="http://www.w3.org/2000/svg"><image xlink:href="../art/cover.png"/></svg></body></html>"""
        val file = epub("guide", """
            <manifest><item id="page" href="pages/cover.xhtml" media-type="application/xhtml+xml"/></manifest>
            <guide><reference type="cover" href="pages/cover.xhtml#top"/></guide>
        """, mapOf("OPS/pages/cover.xhtml" to page.toByteArray(), "OPS/art/cover.png" to imageBytes(Color.MAGENTA)))
        assertColor(Color.MAGENTA, load(file, "application/epub+zip"))
    }

    @Test fun missingCorruptAndExternalCoversFallBackSafely() = runBlocking {
        val corrupt = File(directory, "corrupt.pdf").apply { writeText("not a PDF") }
        assertNull(load(corrupt, "application/pdf"))
        val absent = epub("missing", "<manifest/>", emptyMap())
        assertNull(load(absent, "application/epub+zip"))
        val remote = epub("external", """
            <manifest><item id="cover" href="https://example.invalid/cover.png" media-type="image/png" properties="cover-image"/></manifest>
        """, emptyMap())
        assertNull(load(remote, "application/epub+zip"))
        val traversal = epub("traversal", """
            <manifest><item id="cover" href="%2e%2e/cover.png" media-type="image/png" properties="cover-image"/></manifest>
        """, mapOf("cover.png" to imageBytes(Color.RED)))
        assertNull(load(traversal, "application/epub+zip"))
        val entities = epub("entities", """
            <manifest><item id="cover" href="cover.xhtml" media-type="application/xhtml+xml"/></manifest>
            <spine><itemref idref="cover"/></spine>
        """, mapOf("OPS/cover.xhtml" to """<!DOCTYPE html [<!ENTITY test SYSTEM "file:///invalid">]><html><img src="&test;"/></html>""".toByteArray()))
        assertNull(load(entities, "application/epub+zip"))
    }

    @Test fun oversizedZipEntriesAreNotReadAsImages() = runBlocking {
        val file = epub("oversized", """
            <manifest><item id="cover" href="cover.png" media-type="image/png" properties="cover-image"/></manifest>
        """, mapOf("OPS/cover.png" to ByteArray(16 * 1024 * 1024 + 1)))
        assertNull(load(file, "application/epub+zip"))
    }

    @Test fun imageRevisionsAndDeletedAttachmentsInvalidateCachedCovers() = runBlocking {
        val file = File(directory, "image.png").apply { writeBytes(imageBytes(Color.RED)) }
        val original = ItemCoverSource.fromFile(file, "image/png")!!
        assertColor(Color.RED, ItemCoverLoader.load(original, directory))
        file.writeBytes(imageBytes(Color.BLUE))
        assertTrue(file.setLastModified(original.modified + 2000))
        assertNull(ItemCoverLoader.load(original, directory))
        assertColor(Color.BLUE, load(file, "image/png"))
        file.delete()
        assertNull(ItemCoverLoader.load(original, directory))
    }

    @Test fun visibleRowsRefreshCoversAndKeepClickBehavior() {
        val file = File(directory, "visible.png").apply { writeBytes(imageBytes(Color.CYAN)) }
        val cover = ItemCoverSource.fromFile(file, "image/png")!!
        var model by mutableStateOf(item("visible", "A book with a cover"))
        var opens = 0
        compose.setContent {
            LibraryTheme {
                Surface {
                    Column {
                        ItemRow(model, null, { false }, false, { opens++ }, {}, {})
                        ItemRow(item("missing", "A book without a cover"), null, { false }, false, {}, {}, {})
                    }
                }
            }
        }
        compose.runOnIdle { model = model.copy(cover = cover) }
        compose.waitUntil(10_000) {
            compose.onAllNodes(androidx.compose.ui.test.hasTestTag("item-cover-visible"), useUnmergedTree = true).fetchSemanticsNodes().isNotEmpty()
        }
        compose.onNodeWithTag("item-cover-visible", useUnmergedTree = true).assertIsDisplayed()
        compose.onNodeWithText(model.title).performClick()
        compose.runOnIdle { assertEquals(1, opens) }
        val screenshot = File(context.filesDir, "ui-screenshots/items-covers.png").apply { parentFile!!.mkdirs() }
        screenshot.outputStream().use {
            val bitmap = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                compose.onRoot().captureToImage().asAndroidBitmap()
            } else {
                checkNotNull(InstrumentationRegistry.getInstrumentation().uiAutomation.takeScreenshot())
            }
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, it)
            bitmap.recycle()
        }
        compose.runOnIdle { model = model.copy(cover = null) }
        compose.onNodeWithTag("item-cover-visible", useUnmergedTree = true).assertDoesNotExist()
        compose.onNodeWithText("A book without a cover").assertIsDisplayed()
    }

    private fun item(key: String, title: String) = ItemCellModel(
        key, "item_type_book", "Book", title, "Test author", false, null, mutableStateListOf(),
    )

    private suspend fun load(file: File, mime: String): Bitmap? =
        ItemCoverLoader.load(ItemCoverSource.fromFile(file, mime)!!, directory)

    private fun assertColor(color: Int, bitmap: Bitmap?) {
        assertNotNull(bitmap)
        assertEquals(color, bitmap!!.getPixel(bitmap.width / 2, bitmap.height / 2))
    }

    private fun imageBytes(color: Int): ByteArray {
        val bitmap = Bitmap.createBitmap(600, 900, Bitmap.Config.ARGB_8888)
        bitmap.eraseColor(color)
        return ByteArrayOutputStream().use {
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, it)
            bitmap.recycle()
            it.toByteArray()
        }
    }

    private fun epub(name: String, contents: String, entries: Map<String, ByteArray>): File {
        val file = File(directory, "$name.epub")
        val container = """<container xmlns="urn:oasis:names:tc:opendocument:xmlns:container"><rootfiles>
            <rootfile full-path="OPS/package.opf" media-type="application/oebps-package+xml"/></rootfiles></container>"""
        ZipOutputStream(file.outputStream()).use { zip ->
            (mapOf("META-INF/container.xml" to container.toByteArray(),
                "OPS/package.opf" to """<package xmlns="http://www.idpf.org/2007/opf">$contents</package>""".toByteArray()) + entries).forEach { (path, bytes) ->
                zip.putNextEntry(ZipEntry(path)); zip.write(bytes); zip.closeEntry()
            }
        }
        return file
    }
}
