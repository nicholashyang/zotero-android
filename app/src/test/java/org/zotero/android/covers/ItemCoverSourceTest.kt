package org.zotero.android.covers

import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder

class ItemCoverSourceTest {
    @get:Rule val temporary = TemporaryFolder()

    @Test fun acceptsOnlySupportedNonemptyLocalFiles() {
        val file = temporary.newFile()
        assertNull(ItemCoverSource.fromFile(file, "application/pdf"))
        file.writeText("fixture")
        assertEquals(ItemCoverSource.Kind.PDF, ItemCoverSource.fromFile(file, "Application/PDF; version=1.7")?.kind)
        assertEquals(ItemCoverSource.Kind.EPUB, ItemCoverSource.fromFile(file, "application/epub+zip")?.kind)
        assertEquals(ItemCoverSource.Kind.IMAGE, ItemCoverSource.fromFile(file, "image/jpeg")?.kind)
        assertNull(ItemCoverSource.fromFile(file, "text/html"))
        assertNull(ItemCoverSource.fromFile(file, "image/svg+xml"))
        assertNull(ItemCoverSource.fromFile(temporary.root, "image/png"))
        file.delete()
        assertNull(ItemCoverSource.fromFile(file, "application/pdf"))
    }

    @Test fun revisionsAndLibrariesCannotReuseTheSameCover() {
        val first = temporary.newFile("library-one.pdf").apply { writeText("original") }
        val second = temporary.newFile("library-two.pdf").apply { writeText("original") }
        val original = ItemCoverSource.fromFile(first, "application/pdf")!!
        assertTrue(original.isCurrent())
        assertNotEquals(original.cacheKey, ItemCoverSource.fromFile(second, "application/pdf")!!.cacheKey)
        first.writeText("new document")
        assertFalse(original.isCurrent())
        assertNotEquals(original.cacheKey, ItemCoverSource.fromFile(first, "application/pdf")!!.cacheKey)
        first.delete()
        assertFalse(original.isCurrent())
    }
}
