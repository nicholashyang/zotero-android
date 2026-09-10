package org.zotero.android.appupdate

import android.Manifest
import android.os.Build
import android.text.format.Formatter
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.selection.toggleable
import androidx.compose.ui.semantics.Role
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.repeatOnLifecycle
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.zotero.android.BuildConfig
import org.zotero.android.R
import org.zotero.android.uicomponents.library.LibraryDivider
import org.zotero.android.uicomponents.library.LibraryGroup
import java.text.DateFormat
import java.util.Date

@Composable
fun UpdatePanel(repository: UpdateRepository, onInstall: (() -> Unit)? = null, standalone: Boolean = true) {
    val state by repository.state.collectAsState()
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    val lifecycle = LocalLifecycleOwner.current.lifecycle
    var meteredConfirmation by rememberSaveable { mutableStateOf(false) }
    val notifications = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { }
    LaunchedEffect(repository, lifecycle) {
        lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
            while (true) {
                repository.refresh()
                delay(1000)
            }
        }
    }
    val content: @Composable () -> Unit = {
        UpdateSection(state,
            onCheck = { scope.launch { repository.check() } },
            onDownload = {
                if (repository.isMetered()) meteredConfirmation = true
                else scope.launch { repository.download() }
            },
            onInstall = onInstall ?: { UpdateActivity.open(context) },
            onNotifications = {
                if (Build.VERSION.SDK_INT >= 33 &&
                    androidx.core.content.ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) !=
                    android.content.pm.PackageManager.PERMISSION_GRANTED) {
                    notifications.launch(Manifest.permission.POST_NOTIFICATIONS)
                } else {
                    val intent = if (Build.VERSION.SDK_INT >= 26) {
                        android.content.Intent(android.provider.Settings.ACTION_APP_NOTIFICATION_SETTINGS)
                            .putExtra(android.provider.Settings.EXTRA_APP_PACKAGE, context.packageName)
                    } else android.content.Intent(android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                        android.net.Uri.parse("package:${context.packageName}"))
                    runCatching { context.startActivity(intent) }
                }
            },
        )
    }
    if (standalone) Column(Modifier.verticalScroll(rememberScrollState()).padding(16.dp)) { content() }
    else content()
    if (meteredConfirmation) AlertDialog(
        onDismissRequest = { meteredConfirmation = false },
        title = { Text(stringResource(R.string.update_mobile_title)) },
        text = { Text(stringResource(R.string.update_mobile_message,
            Formatter.formatFileSize(context, state.manifest?.sizeBytes ?: 0))) },
        confirmButton = { TextButton(onClick = {
            meteredConfirmation = false
            scope.launch { repository.download() }
        }) { Text(stringResource(R.string.update_download)) } },
        dismissButton = { TextButton(onClick = { meteredConfirmation = false }) { Text(stringResource(android.R.string.cancel)) } },
    )
}

/** Stateless so screenshots and UI tests exercise the same UI without network or an account. */
@Composable
fun UpdateSection(
    state: AppUpdateState,
    onCheck: () -> Unit = {},
    onDownload: () -> Unit = {},
    onInstall: () -> Unit = {},
    onNotifications: () -> Unit = {},
) {
    val context = LocalContext.current
    LibraryGroup {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(stringResource(R.string.update_title), style = MaterialTheme.typography.titleMedium)
            Text(stringResource(R.string.update_current_version, BuildConfig.VERSION_NAME, BuildConfig.VERSION_CODE))
            if (BuildConfig.SELF_UPDATE_ENABLED) {
                val time = if (state.lastChecked == 0L) stringResource(R.string.update_never)
                    else DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT).format(Date(state.lastChecked))
                Text(stringResource(R.string.update_last_checked, time), style = MaterialTheme.typography.bodySmall)
                val status = when (state.status) {
                    UpdateStatus.IDLE -> R.string.update_idle
                    UpdateStatus.CHECKING -> R.string.update_checking
                    UpdateStatus.UP_TO_DATE -> R.string.update_up_to_date
                    UpdateStatus.NO_RELEASE -> R.string.update_no_release
                    UpdateStatus.AVAILABLE -> R.string.update_available
                    UpdateStatus.WAITING_FOR_NETWORK -> R.string.update_waiting
                    UpdateStatus.DOWNLOADING -> R.string.update_downloading
                    UpdateStatus.VERIFYING -> R.string.update_verifying
                    UpdateStatus.READY -> R.string.update_ready
                    UpdateStatus.ERROR -> when (state.error) {
                        UpdateError.NETWORK -> R.string.update_error_network
                        UpdateError.MANIFEST -> R.string.update_error_manifest
                        UpdateError.INCOMPATIBLE -> R.string.update_error_incompatible
                        UpdateError.SPACE -> R.string.update_error_space
                        UpdateError.VERIFICATION -> R.string.update_error_verification
                        UpdateError.INSTALL -> R.string.update_error_install
                        else -> R.string.update_error_download
                    }
                }
                Text(stringResource(status), color = if (state.status == UpdateStatus.ERROR)
                    MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurfaceVariant)
                state.manifest?.let { manifest ->
                    Text("${manifest.versionName} · ${Formatter.formatFileSize(context, manifest.sizeBytes)}")
                    if (manifest.releaseNotes.isNotBlank()) Text(manifest.releaseNotes,
                        style = MaterialTheme.typography.bodySmall)
                    if (state.status == UpdateStatus.DOWNLOADING) {
                        val progress = (state.downloadedBytes.toFloat() / manifest.sizeBytes).coerceIn(0f, 1f)
                        LinearProgressIndicator(progress = { progress }, modifier = Modifier.fillMaxWidth())
                        Text("${(progress * 100).toInt()}%", style = MaterialTheme.typography.bodySmall)
                    }
                }
                TextButton(onClick = onCheck, enabled = !state.busy) { Text(stringResource(R.string.update_check)) }
                if (state.status == UpdateStatus.READY || state.error == UpdateError.INSTALL) {
                    TextButton(onClick = onInstall) { Text(stringResource(R.string.update_install)) }
                } else if (state.manifest != null && !state.busy) {
                    TextButton(onClick = onDownload) { Text(stringResource(R.string.update_download)) }
                }
            }
        }
        if (BuildConfig.SELF_UPDATE_ENABLED) {
            LibraryDivider()
            Text(stringResource(R.string.mobile_update_policy), modifier = Modifier.padding(16.dp),
                style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            TextButton(onClick = onNotifications, modifier = Modifier.padding(horizontal = 8.dp)) {
                Text(stringResource(R.string.update_notifications))
            }
        }
    }
}

@Composable
fun UpdateHomeBanner(repository: UpdateRepository) {
    val state by repository.state.collectAsState()
    val context = LocalContext.current
    var dismissedVersion by rememberSaveable { mutableStateOf<Long?>(null) }
    val manifest = state.manifest
    if (manifest != null && manifest.versionCode != dismissedVersion) {
        LibraryGroup {
            Column(Modifier.padding(16.dp)) {
                Text(stringResource(if (state.status == UpdateStatus.READY) R.string.update_ready else R.string.update_available))
                Row {
                    TextButton(onClick = { UpdateActivity.open(context) }) { Text(stringResource(R.string.update_title)) }
                    TextButton(onClick = { dismissedVersion = manifest.versionCode }) { Text(stringResource(R.string.update_later)) }
                }
            }
        }
    }
}
