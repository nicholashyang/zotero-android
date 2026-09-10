package org.zotero.android.screens.settings

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import org.zotero.android.BuildConfig
import org.zotero.android.R
import org.zotero.android.preferences.*
import org.zotero.android.uicomponents.library.*

@Composable
internal fun MobilePreferencesGroup(onPermissions: () -> Unit) {
    val preferences = rememberAppPreferences()
    val state by preferences.state.collectAsState()
    var picker by remember { mutableStateOf<String?>(null) }
    LibraryGroup {
        LibraryRow(stringResource(R.string.mobile_appearance), { picker = "theme" }, trailing = { Text(stringResource(state.appearance.label)) })
        LibraryDivider()
        LibraryRow(stringResource(R.string.mobile_swipe_left), { picker = "left" }, trailing = { Text(stringResource(state.leftSwipe.label)) })
        LibraryDivider()
        LibraryRow(stringResource(R.string.mobile_swipe_right), { picker = "right" }, trailing = { Text(stringResource(state.rightSwipe.label)) })
        LibraryDivider()
        LibraryRow(stringResource(R.string.mobile_permissions), onPermissions)
    }
    picker?.let { choice ->
        AlertDialog(onDismissRequest = { picker = null },
            title = { Text(stringResource(if (choice == "theme") R.string.mobile_appearance else R.string.mobile_swipes)) },
            text = {
                Column(Modifier.verticalScroll(rememberScrollState())) {
                    if (choice == "theme") Appearance.entries.forEach { value ->
                        TextButton(onClick = { preferences.appearance(value); picker = null }) {
                            Text((if (state.appearance == value) "✓  " else "") + stringResource(value.label))
                        }
                    } else {
                        Text(stringResource(R.string.mobile_swipe_hint))
                        SwipeAction.entries.forEach { value ->
                            TextButton(onClick = { preferences.swipe(choice == "left", value); picker = null }) {
                                val selected = if (choice == "left") state.leftSwipe else state.rightSwipe
                                Text((if (selected == value) "✓  " else "") + stringResource(value.label))
                            }
                        }
                    }
                }
            }, confirmButton = { TextButton(onClick = { picker = null }) { Text(stringResource(android.R.string.cancel)) } })
    }
}

@Composable
internal fun AppPermissionsScreen(onBack: () -> Unit) {
    BackHandler(onBack = onBack)
    val context = LocalContext.current
    val lifecycle = LocalLifecycleOwner.current.lifecycle
    var revision by remember { mutableIntStateOf(0) }
    val requested = remember { context.getSharedPreferences("permission_requests", android.content.Context.MODE_PRIVATE) }
    val request = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { revision++ }
    DisposableEffect(lifecycle) {
        val observer = LifecycleEventObserver { _, event -> if (event == Lifecycle.Event.ON_RESUME) revision++ }
        lifecycle.addObserver(observer)
        onDispose { lifecycle.removeObserver(observer) }
    }
    fun appSettings() { context.startActivity(Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, Uri.parse("package:${context.packageName}"))) }
    fun manage(permission: String) {
        if (ContextCompat.checkSelfPermission(context, permission) != PackageManager.PERMISSION_GRANTED && !requested.getBoolean(permission, false)) {
            requested.edit().putBoolean(permission, true).apply()
            request.launch(permission)
        } else appSettings()
    }
    val camera = remember(revision) { ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED }
    val notifications = remember(revision) { NotificationManagerCompat.from(context).areNotificationsEnabled() }
    val install = remember(revision) { Build.VERSION.SDK_INT < 26 || context.packageManager.canRequestPackageInstalls() }
    LibraryScaffold(topBar = { LibraryNavigationBar(stringResource(R.string.mobile_permissions), onBack = onBack) }) {
        Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
            LibraryGroup {
                PermissionRow(R.string.mobile_camera, camera) { manage(Manifest.permission.CAMERA) }
                LibraryDivider()
                PermissionRow(R.string.mobile_notifications, notifications) {
                    if (Build.VERSION.SDK_INT >= 33 && !notifications && !requested.getBoolean(Manifest.permission.POST_NOTIFICATIONS, false)) {
                        manage(Manifest.permission.POST_NOTIFICATIONS)
                    } else if (Build.VERSION.SDK_INT >= 26) {
                        context.startActivity(Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS).putExtra(Settings.EXTRA_APP_PACKAGE, context.packageName))
                    } else appSettings()
                }
                if (BuildConfig.SELF_UPDATE_ENABLED && Build.VERSION.SDK_INT >= 26) {
                    LibraryDivider()
                    PermissionRow(R.string.mobile_install, install) {
                        context.startActivity(Intent(Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES, Uri.parse("package:${context.packageName}")))
                    }
                }
            }
            Text(stringResource(R.string.mobile_files_hint), style = MaterialTheme.typography.bodyMedium)
            TextButton(onClick = ::appSettings) { Text(stringResource(R.string.mobile_system_settings)) }
        }
    }
}

@Composable
private fun PermissionRow(title: Int, granted: Boolean, onClick: () -> Unit) {
    LibraryRow(stringResource(title) + "\n" + stringResource(if (granted) R.string.mobile_permission_granted else R.string.mobile_permission_denied), onClick)
}
