package org.zotero.android.architecture.ui

import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

object CustomLayoutSize {
    enum class LayoutType {
        big, medium, small;

        fun calculateItemCreatorDeleteStartPadding(): Dp {
            return when (this) {
                big, medium -> 18.dp
                small -> 11.dp
            }
        }

        fun calculateIconSize(): Dp {
            return when (this) {
                big, medium -> 30.dp
                small -> 22.dp
            }
        }

        fun calculateItemFieldLabelWidth(): Dp {
            return when (this) {
                big, medium -> 160.dp
                small -> 120.dp
            }
        }

        fun calculateMainLoaderIconSize(): Dp {
            return when (this) {
                big, medium -> 100.dp
                small -> 60.dp
            }
        }

        fun calculateItemsRowTextSize(): TextUnit {
            return when (this) {
                big, medium -> 20.sp
                small -> 14.sp
            }
        }

        fun calculatePdfSettingsOptionTextSize(): TextUnit {
            return when (this) {
                big, medium -> 14.sp
                small -> 14.sp
            }
        }

        fun calculateAllItemsBottomPanelHeight(): Dp {
            return 48.dp
        }

        fun calculateSelectorHeight(): Dp {
            return when (this) {
                big, medium -> 50.dp
                small -> 36.dp
            }
        }
        fun isTablet(): Boolean {
            return when (this) {
                big, medium -> true
                small -> false
            }
        }

    }

    @Composable
    fun calculateLayoutType(): LayoutType {
        val configuration = LocalConfiguration.current

        val density = LocalDensity.current.density
        val bounds = safePane((configuration.screenWidthDp * density).toInt(),
            (configuration.screenHeightDp * density).toInt(), 0, 0, LocalWindowFold.current)
        val screenWidth = bounds.width / density
        val screenHeight = bounds.height / density

        // Window width changes on fold/unfold and in split screen. The shortest
        // physical dimension incorrectly classifies landscape and inner displays.
        if (screenWidth >= 1100 && screenHeight >= 400) {
            return LayoutType.big
        } else if (screenWidth >= 720 && screenHeight >= 400) {
            return LayoutType.medium
        }
        return LayoutType.small
    }
}
