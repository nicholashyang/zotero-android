package org.zotero.android.library

import android.app.Application
import android.content.Context
import androidx.test.runner.AndroidJUnitRunner

/** Component tests use no account, database, sync controller or licensed reader. */
class LibraryTestRunner : AndroidJUnitRunner() {
    override fun newApplication(cl: ClassLoader, className: String, context: Context): Application =
        super.newApplication(cl, Application::class.java.name, context)
}
