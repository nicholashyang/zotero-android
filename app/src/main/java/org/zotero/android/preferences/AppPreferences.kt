package org.zotero.android.preferences

import android.content.Context
import androidx.annotation.StringRes
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.zotero.android.R

enum class Appearance(@StringRes val label: Int) {
    SYSTEM(R.string.mobile_theme_system), LIGHT(R.string.mobile_theme_light), DARK(R.string.mobile_theme_dark);
    fun isDark(systemDark: Boolean) = when (this) { SYSTEM -> systemDark; LIGHT -> false; DARK -> true }
}
enum class SwipeAction(@StringRes val label: Int) {
    NONE(R.string.mobile_swipe_none), COLLECTION(R.string.mobile_swipe_collection),
    TRASH(R.string.mobile_swipe_trash), OPEN(R.string.mobile_swipe_open), MORE(R.string.mobile_swipe_more)
}
data class AppPreferencesState(
    val appearance: Appearance = Appearance.SYSTEM,
    val leftSwipe: SwipeAction = SwipeAction.NONE,
    val rightSwipe: SwipeAction = SwipeAction.NONE,
)
/** Device preferences survive sign-out. Only the application context is retained. */
class AppPreferences private constructor(context: Context) {
    private val prefs = context.getSharedPreferences("mobile_preferences", Context.MODE_PRIVATE)
    private inline fun <reified T : Enum<T>> read(key: String, default: T): T =
        enumValues<T>().firstOrNull { it.name == prefs.getString(key, null) } ?: default
    private val mutable = MutableStateFlow(AppPreferencesState(
        read("appearance", Appearance.SYSTEM), read("leftSwipe", SwipeAction.NONE), read("rightSwipe", SwipeAction.NONE)))
    val state = mutable.asStateFlow()
    fun appearance(value: Appearance) { prefs.edit().putString("appearance", value.name).apply(); mutable.value = mutable.value.copy(appearance = value) }
    fun swipe(left: Boolean, value: SwipeAction) {
        prefs.edit().putString(if (left) "leftSwipe" else "rightSwipe", value.name).apply()
        mutable.value = if (left) mutable.value.copy(leftSwipe = value) else mutable.value.copy(rightSwipe = value)
    }
    companion object {
        @Volatile private var instance: AppPreferences? = null
        fun get(context: Context): AppPreferences = instance ?: synchronized(this) {
            instance ?: AppPreferences(context.applicationContext).also { instance = it }
        }
    }
}
@Composable
fun appDarkTheme(): Boolean {
    val preferences = rememberAppPreferences()
    val state by preferences.state.collectAsState()
    val dark = state.appearance.isDark(isSystemInDarkTheme())
    val view = androidx.compose.ui.platform.LocalView.current
    SideEffect {
        var context = view.context
        while (context is android.content.ContextWrapper && context !is android.app.Activity) context = context.baseContext
        (context as? android.app.Activity)?.window?.let { window ->
            androidx.core.view.WindowCompat.getInsetsController(window, view).apply {
                isAppearanceLightStatusBars = !dark
                isAppearanceLightNavigationBars = !dark
            }
        }
    }
    return dark
}
@Composable
fun rememberAppPreferences(): AppPreferences {
    val context = LocalContext.current
    return remember(context.applicationContext) { AppPreferences.get(context) }
}
