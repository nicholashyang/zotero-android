package org.zotero.android.screens.settings.account.sections

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import org.zotero.android.screens.settings.account.SettingsAccountViewModel
import org.zotero.android.screens.settings.account.SettingsAccountViewState
import org.zotero.android.uicomponents.Strings
import org.zotero.android.uicomponents.library.*

@Composable
internal fun SettingsAccountDataSyncSection(viewState: SettingsAccountViewState, viewModel: SettingsAccountViewModel) {
    LibrarySection(stringResource(Strings.settings_sync_data_syncing)) {
        LibraryRow(viewState.account, {}, trailing = {})
        LibraryDivider()
        LibraryRow(stringResource(Strings.settings_logout), viewModel::onShowSignOutDialog,
            titleColor = MaterialTheme.colorScheme.error, trailing = {})
    }
}
