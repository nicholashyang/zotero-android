package org.zotero.android.screens.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import org.zotero.android.R
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.*
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.hilt.navigation.compose.hiltViewModel
import org.zotero.android.appupdate.AppUpdateState
import org.zotero.android.appupdate.UpdateSection
import org.zotero.android.appupdate.UpdatePanel
import org.zotero.android.uicomponents.Strings
import org.zotero.android.uicomponents.library.LibraryDivider
import org.zotero.android.uicomponents.library.LibraryGroup
import org.zotero.android.uicomponents.library.LibraryMetrics
import org.zotero.android.uicomponents.library.LibraryRow
import org.zotero.android.uicomponents.library.LibraryScaffold
import org.zotero.android.uicomponents.library.LibraryBadge

@Composable
internal fun SettingsScreen(
    onBack: () -> Unit,
    onOpenWebpage: (url: String) -> Unit,
    toAccountScreen: () -> Unit,
    toDebugScreen: () -> Unit,
    toCiteScreen: () -> Unit,
    toQuickCopyScreen: () -> Unit,
    viewModel: SettingsViewModel = hiltViewModel(),
) {
    var permissions by androidx.compose.runtime.saveable.rememberSaveable { mutableStateOf(false) }
    if (permissions) {
        AppPermissionsScreen(onBack = { permissions = false })
        return
    }
    androidx.activity.compose.BackHandler(onBack = onBack)
    val viewEffect by viewModel.viewEffects.observeAsState()
    LaunchedEffect(key1 = viewModel) {
        viewModel.init()
    }

    LaunchedEffect(key1 = viewEffect) {
        when (val consumedEffect = viewEffect?.consume()) {
            null -> Unit
            is SettingsViewEffect.OnBack -> {
                onBack()
            }

            is SettingsViewEffect.OpenWebpage -> {
                onOpenWebpage(consumedEffect.url)
            }
        }
    }
    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()
    LibraryScaffold(
        scrollBehavior = scrollBehavior,
        topBar = { SettingsTopBar(onClose = onBack, scrollBehavior = scrollBehavior) },
    ) {
        SettingsContent(
            toAccountScreen = toAccountScreen,
            toQuickCopyScreen = toQuickCopyScreen,
            toCiteScreen = toCiteScreen,
            toDebugScreen = toDebugScreen,
            openSupport = viewModel::openSupportAndFeedback,
            openPrivacyPolicy = viewModel::openPrivacyPolicy,
            preferencesContent = { MobilePreferencesGroup(onPermissions = { permissions = true }) },
            updateContent = { UpdatePanel(viewModel.updates, standalone = false) },
        )
    }
}

@Composable
internal fun SettingsContent(
    toAccountScreen: () -> Unit,
    toQuickCopyScreen: () -> Unit,
    toCiteScreen: () -> Unit,
    toDebugScreen: () -> Unit,
    openSupport: () -> Unit,
    openPrivacyPolicy: () -> Unit,
    preferencesContent: @Composable () -> Unit = {},
    updateContent: @Composable () -> Unit = { UpdateSection(AppUpdateState()) },
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(LibraryMetrics.pageInset),
        verticalArrangement = Arrangement.spacedBy(LibraryMetrics.groupGap),
    ) {
        item {
            LibraryGroup {
                LibraryRow(stringResource(Strings.settings_sync_account), toAccountScreen,
                    leading = { LibraryBadge(R.drawable.ic_native_person, Color(0xFF007AFF)) })
            }
        }
        item {
            LibraryGroup {
                LibraryRow(stringResource(Strings.settings_export_title), toQuickCopyScreen,
                    leading = { LibraryBadge(R.drawable.ic_native_copy, Color(0xFF5856D6)) })
                LibraryDivider()
                LibraryRow(stringResource(Strings.settings_cite_title), toCiteScreen,
                    leading = { LibraryBadge(R.drawable.ic_native_quote, Color(0xFFFF9500)) })
            }
        }
        item { preferencesContent() }
        item {
            LibraryGroup {
                LibraryRow(stringResource(Strings.settings_debug), toDebugScreen,
                    leading = { LibraryBadge(R.drawable.ic_native_debug, Color(0xFF8E8E93)) })
            }
        }
        item {
            LibraryGroup {
                LibraryRow(stringResource(Strings.support_feedback), openSupport,
                    leading = { LibraryBadge(R.drawable.ic_native_help, Color(0xFF30A46C)) })
                LibraryDivider()
                LibraryRow(stringResource(Strings.privacy_policy), openPrivacyPolicy,
                    leading = { LibraryBadge(R.drawable.ic_native_shield, Color(0xFF007AFF)) })
            }
        }
        item { updateContent() }
        item {
            Column(Modifier.fillMaxWidth().padding(bottom = 16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                Text("Zotero", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text(org.zotero.android.BuildConfig.VERSION_NAME, style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}
