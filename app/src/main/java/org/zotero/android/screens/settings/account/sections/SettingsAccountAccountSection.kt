package org.zotero.android.screens.settings.account.sections

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import org.zotero.android.screens.settings.account.SettingsAccountViewModel
import org.zotero.android.uicomponents.Strings
import org.zotero.android.uicomponents.library.*

@Composable
internal fun SettingsAccountAccountSection(viewModel: SettingsAccountViewModel) {
    LibrarySection(stringResource(Strings.settings_sync_title)) {
        LibraryRow(stringResource(Strings.settings_sync_manage_account), viewModel::openManageAccount,
            titleColor = MaterialTheme.colorScheme.primary)
        LibraryDivider()
        LibraryRow(stringResource(Strings.settings_sync_delete_account), viewModel::openDeleteAccount,
            titleColor = MaterialTheme.colorScheme.error)
    }
}
