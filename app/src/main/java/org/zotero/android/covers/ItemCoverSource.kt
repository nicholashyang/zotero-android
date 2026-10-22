package org.zotero.android.covers

import java.io.File
import java.util.Locale

/** A snapshot of a local attachment. File revisions and libraries have distinct cache keys. */
data class ItemCoverSource(
    val path: String,
    val kind: Kind,
    val modified: Long,
    val size: Long,
) {
    enum class Kind { PDF, EPUB, IMAGE }

    internal val cacheKey: String get() = "$path\u0000$kind\u0000$modified\u0000$size"

    internal fun isCurrent(): Boolean {
        val file = File(path)
        return file.isFile && file.lastModified() == modified && file.length() == size
    }

    companion object {
        fun fromFile(file: File, contentType: String): ItemCoverSource? {
            val mime = contentType.substringBefore(';').trim().lowercase(Locale.ROOT)
            val kind = when {
                mime == "application/pdf" -> Kind.PDF
                mime == "application/epub+zip" -> Kind.EPUB
                mime.startsWith("image/") && mime != "image/svg+xml" -> Kind.IMAGE
                else -> return null
            }
            if (!file.isFile || file.length() == 0L) return null
            return ItemCoverSource(file.absolutePath, kind, file.lastModified(), file.length())
        }
    }
}
