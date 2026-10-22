package org.zotero.android.covers

import android.util.Xml
import org.xmlpull.v1.XmlPullParser
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.IOException
import java.net.URI
import java.util.zip.ZipFile

/** Reads only bounded, explicitly referenced entries; never extracts files or follows web URLs. */
internal object EpubCoverReader {
    private const val MAX_XML_BYTES = 1024 * 1024
    private const val MAX_IMAGE_BYTES = 16 * 1024 * 1024

    fun read(file: File): ByteArray? = ZipFile(file).use { zip ->
        var packagePath: String? = null
        readXml(zip, "META-INF/container.xml") { parser ->
            if (parser.name == "rootfile" && packagePath == null) {
                packagePath = resolve("", parser.attribute("full-path"))
            }
        }
        val opf = packagePath ?: return null
        val manifest = linkedMapOf<String, Entry>()
        var coverId: String? = null
        var guideHref: String? = null
        var firstSpineId: String? = null
        readXml(zip, opf) { parser ->
            when (parser.name) {
                "item" -> {
                    val id = parser.attribute("id")
                    val href = parser.attribute("href")
                    if (id != null && href != null) {
                        manifest[id] = Entry(href, parser.attribute("media-type").orEmpty(),
                            parser.attribute("properties").orEmpty().split(Regex("\\s+")))
                    }
                }
                "meta" -> if (parser.attribute("name") == "cover") coverId = parser.attribute("content")
                "reference" -> if (parser.attribute("type") == "cover") guideHref = parser.attribute("href")
                "itemref" -> if (firstSpineId == null) firstSpineId = parser.attribute("idref")
            }
        }
        val candidates = listOfNotNull(
            manifest.values.firstOrNull { "cover-image" in it.properties }?.href,
            manifest[coverId]?.href,
            guideHref,
            manifest[firstSpineId]?.href,
        ).distinct()
        for (href in candidates) {
            val path = resolve(opf, href) ?: continue
            val entry = manifest.values.firstOrNull { resolve(opf, it.href) == path }
            val isImage = entry?.mediaType?.startsWith("image/") == true ||
                path.substringAfterLast('.').lowercase() in listOf("jpg", "jpeg", "png", "gif", "webp", "bmp")
            if (isImage) {
                readEntry(zip, path, MAX_IMAGE_BYTES)?.let { return it }
            } else {
                var imageHref: String? = null
                readXml(zip, path) { parser ->
                    if (imageHref == null) {
                        imageHref = when (parser.name) {
                            "img" -> parser.attribute("src")
                            "image" -> parser.attribute("href")
                            else -> null
                        }
                    }
                }
                val imagePath = resolve(path, imageHref) ?: continue
                readEntry(zip, imagePath, MAX_IMAGE_BYTES)?.let { return it }
            }
        }
        null
    }

    private data class Entry(val href: String, val mediaType: String, val properties: List<String>)

    private fun XmlPullParser.attribute(name: String): String? =
        (0 until attributeCount).firstOrNull { getAttributeName(it) == name }?.let { getAttributeValue(it) }

    private fun readXml(zip: ZipFile, path: String, onElement: (XmlPullParser) -> Unit) {
        val bytes = readEntry(zip, path, MAX_XML_BYTES) ?: return
        val parser = Xml.newPullParser()
        parser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, true)
        parser.setFeature(XmlPullParser.FEATURE_PROCESS_DOCDECL, false)
        parser.setInput(bytes.inputStream(), null)
        while (true) {
            when (parser.nextToken()) {
                XmlPullParser.END_DOCUMENT -> return
                XmlPullParser.DOCDECL -> if ('[' in parser.text.orEmpty()) {
                    throw IOException("EPUB cover XML contains entity declarations")
                }
                XmlPullParser.START_TAG -> onElement(parser)
            }
        }
    }

    private fun readEntry(zip: ZipFile, path: String, limit: Int): ByteArray? {
        val entry = zip.getEntry(path) ?: return null
        if (entry.isDirectory || entry.size > limit) return null
        return zip.getInputStream(entry).use { stream ->
            val output = ByteArrayOutputStream()
            val buffer = ByteArray(8192)
            while (output.size() <= limit) {
                val count = stream.read(buffer, 0, minOf(buffer.size, limit + 1 - output.size()))
                if (count == -1) return@use output.toByteArray()
                output.write(buffer, 0, count)
            }
            null
        }
    }

    private fun resolve(base: String, href: String?): String? {
        if (href.isNullOrBlank()) return null
        return try {
            val reference = URI(href.replace(" ", "%20"))
            if (reference.isAbsolute || reference.rawAuthority != null) return null
            val path = URI(null, null, base, null).resolve(reference).normalize().path ?: return null
            if (path.startsWith('/') || '\\' in path || path.split('/').any { it == ".." || it == "." }) null else path
        } catch (_: IllegalArgumentException) {
            null
        } catch (_: java.net.URISyntaxException) {
            null
        }
    }
}
