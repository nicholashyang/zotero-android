package org.zotero.android.appupdate

import android.content.Context
import android.content.Intent
import androidx.core.net.toUri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.FileProvider
import androidx.lifecycle.lifecycleScope
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import org.zotero.android.uicomponents.library.LibraryScaffold
import org.zotero.android.uicomponents.library.LibraryTheme
import org.zotero.android.screens.settings.SettingsTopBar
import androidx.compose.material3.TopAppBarDefaults
import javax.inject.Inject

/** Also reachable from an update notification before signing into a Zotero account. */
@AndroidEntryPoint
class UpdateActivity : ComponentActivity() {
    @Inject lateinit var updates: UpdateRepository
    private val permission = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
        if (Build.VERSION.SDK_INT < 26 || packageManager.canRequestPackageInstalls()) install()
    }
    private val installer = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
        lifecycleScope.launch {
            updates.refresh()
            if (it.resultCode != RESULT_OK && it.resultCode != RESULT_CANCELED) updates.installationFailed()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            LibraryTheme {
                val scroll = TopAppBarDefaults.pinnedScrollBehavior()
                LibraryScaffold(topBar = { SettingsTopBar({ finish() }, scroll) }) {
                    UpdatePanel(updates, onInstall = ::install)
                }
            }
        }
    }

    private fun install() {
        if (Build.VERSION.SDK_INT >= 26 && !packageManager.canRequestPackageInstalls()) {
            try {
                permission.launch(Intent(Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES, "package:$packageName".toUri()))
            } catch (_: Exception) { updates.installationFailed() }
            return
        }
        lifecycleScope.launch {
            val file = updates.fileForInstall() ?: return@launch
            try {
                val uri = FileProvider.getUriForFile(this@UpdateActivity, "$packageName.provider", file)
                @Suppress("DEPRECATION")
                val intent = Intent(Intent.ACTION_INSTALL_PACKAGE).apply {
                    setDataAndType(uri, "application/vnd.android.package-archive")
                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                    putExtra(Intent.EXTRA_RETURN_RESULT, true)
                }
                installer.launch(intent)
            } catch (_: Exception) { updates.installationFailed() }
        }
    }

    companion object {
        fun open(context: Context) { context.startActivity(Intent(context, UpdateActivity::class.java)) }
    }
}
