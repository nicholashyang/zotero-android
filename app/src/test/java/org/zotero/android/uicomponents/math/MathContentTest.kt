package org.zotero.android.uicomponents.math

import org.junit.Assert.*
import org.junit.Test

class MathContentTest {
    @Test fun preservesMixedTextAndCompleteFormulaSegments() {
        val source = "研究 \\(x^2\\) and ${'$'}${'$'}\\frac{a}{b}${'$'}${'$'} end"
        val segments = MathContent.segments(source)
        assertEquals(source, segments.joinToString("") { it.source })
        assertEquals(listOf("x^2", "\\frac{a}{b}"), segments.mapNotNull { it.tex })
        assertTrue(segments.first { it.display }.source.startsWith("$$"))
    }
    @Test fun currencyAndEscapedDollarsStayLiteral() {
        for (source in listOf("Costs ${'$'}5 and ${'$'}10", "\\${'$'}x\\${'$'}", "no equations", "${'$'} unmatched")) {
            assertTrue(source, MathContent.segments(source).none { it.tex != null })
            assertEquals(source, MathContent.segments(source).joinToString("") { it.source })
        }
    }
    @Test fun invalidTexIsPreservedForDisplayFallback() {
        val result = MathContent.segments("\\[\\notacommand{x}\\]")
        assertEquals("\\notacommand{x}", result.single().tex)
        assertTrue(result.single().display)
    }
}
