package org.zotero.android.uicomponents.library

import androidx.annotation.DrawableRes
import org.zotero.android.R
import org.zotero.android.uicomponents.Drawables

/** Original stroke icons, shared by the old and new screen entry points. */
@DrawableRes
internal fun librarySymbol(@DrawableRes icon: Int): Int = when (icon) {
    Drawables.arrow_back_24dp -> R.drawable.ic_native_back
    Drawables.chevron_right_24px -> R.drawable.ic_native_chevron
    Drawables.cell_collection -> R.drawable.ic_native_folder
    Drawables.icon_cell_library -> R.drawable.ic_native_library
    Drawables.add_24px -> R.drawable.ic_native_plus
    Drawables.search_24px -> R.drawable.ic_native_search
    Drawables.swap_vert_24px -> R.drawable.ic_native_sort
    Drawables.filter_list_24px, Drawables.filter_list_off_24px -> R.drawable.ic_native_filter
    else -> icon
}
