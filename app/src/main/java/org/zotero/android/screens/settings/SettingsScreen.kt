package org.zotero.android.screens.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
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
    updateContent: @Composable () -> Unit = { UpdateSection(AppUpdateState()) },
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(LibraryMetrics.pageInset),
        verticalArrangement = Arrangement.spacedBy(LibraryMetrics.groupGap),
    ) {
        item {
            LibraryGroup {
                LibraryRow(stringResource(Strings.settings_sync_account), toAccountScreen)
            }
        }
        item {
            LibraryGroup {
                LibraryRow(stringResource(Strings.settings_export_title), toQuickCopyScreen)
                LibraryDivider()
                LibraryRow(stringResource(Strings.settings_cite_title), toCiteScreen)
            }
        }
        item {
            LibraryGroup {
                LibraryRow(stringResource(Strings.settings_debug), toDebugScreen)
            }
        }
        item {
            LibraryGroup {
                LibraryRow(stringResource(Strings.support_feedback), openSupport)
                LibraryDivider()
                LibraryRow(stringResource(Strings.privacy_policy), openPrivacyPolicy)
            }
        }
        item { updateContent() }
    }
}
