package org.zotero.android.screens.settings.quickcopy

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import org.zotero.android.screens.settings.elements.NewSettingsItemWithDescription
import org.zotero.android.uicomponents.Strings
import org.zotero.android.uicomponents.library.LibraryDivider


@Composable
internal fun SettingsQuickCopySections(
    selectedStyle: String,
    selectedLanguage: String,
    languagePickerEnabled: Boolean,
    copyAsHtml: Boolean,
    onDefaultFormatTapped: () -> Unit,
    onLanguageTapped: () -> Unit,
    onQuickCopySwitchTapped: (Boolean) -> Unit,
) {
    NewSettingsItemWithDescription(
        title = stringResource(Strings.settings_export_default_format),
        description = selectedStyle,
        onItemTapped = onDefaultFormatTapped
    )

    LibraryDivider()
    NewSettingsItemWithDescription(
        title = stringResource(Strings.settings_export_language),
        description = selectedLanguage,
        isEnabled = languagePickerEnabled,
        onItemTapped = onLanguageTapped
    )

    LibraryDivider()
    SettingsQuickCopySwitchItem(
        title = stringResource(Strings.settings_export_copy_as_html),
        isChecked = copyAsHtml,
        onCheckedChange = onQuickCopySwitchTapped
    )
}