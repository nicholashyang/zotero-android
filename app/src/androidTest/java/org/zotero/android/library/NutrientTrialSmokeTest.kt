package org.zotero.android.library

import android.net.Uri
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.pspdfkit.PSPDFKit
import com.pspdfkit.document.PdfDocumentLoader
import java.io.File
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith

/** SDK parsing smoke test only: does not certify reader UI, annotations or export licensing. */
@RunWith(AndroidJUnit4::class)
class NutrientTrialSmokeTest {
    @Test fun trialInitializationCanOpenLocalPdf() {
        val instrumentation = InstrumentationRegistry.getInstrumentation()
        val context = instrumentation.targetContext
        instrumentation.runOnMainSync { PSPDFKit.initialize(context, null) }
        val pdf = File(context.cacheDir, "ui-smoke.pdf")
        instrumentation.context.assets.open("ui-smoke.pdf").use { source ->
            pdf.outputStream().use { target -> source.copyTo(target) }
        }
        val document = PdfDocumentLoader.openDocument(context, Uri.fromFile(pdf))
        try {
            assertEquals(1, document.pageCount)
            assertTrue(document.getPageTextLength(0) > 0)
        } finally {
            pdf.delete()
        }
    }
}
