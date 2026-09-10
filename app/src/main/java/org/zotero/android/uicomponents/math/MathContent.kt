package org.zotero.android.uicomponents.math

/** Delimiters are parsed before HTML handling; ordinary currency and escaped dollars stay text. */
internal data class MathSegment(val source: String, val tex: String? = null, val display: Boolean = false)
internal object MathContent {
    private fun isEscaped(text: String, offset: Int): Boolean {
        var slash = offset - 1
        while (slash >= 0 && text[slash] == '\\') slash--
        return (offset - slash - 1) % 2 == 1
    }
    fun segments(text: String): List<MathSegment> {
        val result = mutableListOf<MathSegment>()
        var start = 0
        var index = 0
        while (index < text.length) {
            val escaped = isEscaped(text, index)
            val open = when {
                escaped -> null
                text.startsWith("\\(", index) -> "\\("
                text.startsWith("\\[", index) -> "\\["
                text.startsWith("$$", index) -> "$$"
                text[index] == '$' && index + 1 < text.length && !text[index + 1].isWhitespace() -> "$"
                else -> null
            }
            if (open == null) { index++; continue }
            val close = when (open) { "\\(" -> "\\)"; "\\[" -> "\\]"; else -> open }
            var end = text.indexOf(close, index + open.length)
            while (end >= 0 && isEscaped(text, end)) {
                end = text.indexOf(close, end + close.length)
            }
            if (end < 0) { index += open.length; continue }
            val body = text.substring(index + open.length, end)
            if (body.isBlank() || (open == "$" && (body.last().isWhitespace() || body.contains('\n') ||
                    (end + 1 < text.length && text[end + 1].isDigit())))) { index += open.length; continue }
            if (index > start) result.add(MathSegment(text.substring(start, index)))
            result.add(MathSegment(text.substring(index, end + close.length), body, open == "$$" || open == "\\["))
            index = end + close.length
            start = index
        }
        if (start < text.length) result.add(MathSegment(text.substring(start)))
        return result
    }
}
