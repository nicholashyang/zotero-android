package org.zotero.android.screens.allitems

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import kotlinx.coroutines.launch
import org.zotero.android.R
import org.zotero.android.sync.Collection
import org.zotero.android.sync.Library
import org.zotero.android.screens.settings.SettingsNavigation
import org.zotero.android.uicomponents.Drawables
import org.zotero.android.uicomponents.Strings
import org.zotero.android.uicomponents.library.*

@Composable
internal fun HomeLibraryScaffold(viewModel: AllItemsViewModel, viewState: AllItemsViewState, isTablet: Boolean,
    onOpenWebpage: (String) -> Unit, scrollBehavior: TopAppBarScrollBehavior,
    bottomBar: @Composable () -> Unit, content: @Composable () -> Unit) {
    val drawer = rememberDrawerState(DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    var pickingLibrary by rememberSaveable { mutableStateOf(false) }
    var settings by rememberSaveable { mutableStateOf(false) }
    var libraries by remember { mutableStateOf(emptyList<Library>()) }
    var collections by remember { mutableStateOf(emptyList<Collection>()) }
    fun showCollections() {
        if (isTablet) viewModel.navigateToCollections() else {
            collections = viewModel.availableCollections()
            scope.launch { drawer.open() }
        }
    }
    BackHandler(drawer.isOpen) { scope.launch { drawer.close() } }
    ModalNavigationDrawer(drawerState = drawer, gesturesEnabled = drawer.isOpen, drawerContent = {
        ModalDrawerSheet {
            Text(viewState.libraryName, Modifier.padding(16.dp), style = MaterialTheme.typography.titleLarge)
            LazyColumn(Modifier.weight(1f)) {
                items(collections, key = { it.identifier.toString() }) { collection ->
                    NavigationDrawerItem(label = { Text(collection.name) }, selected = collection.name == viewState.collectionName,
                        onClick = { viewModel.selectHomeCollection(collection); scope.launch { drawer.close() } }, modifier = Modifier.padding(horizontal = 12.dp))
                }
            }
            TextButton(onClick = { scope.launch { drawer.close() }; viewModel.navigateToCollections() }) {
                Text(stringResource(R.string.mobile_collections))
            }
        }
    }) {
        LibraryScaffold(
            modifier = Modifier.pageSwipe(!isTablet && !viewState.isEditing && !drawer.isOpen, ::showCollections),
            scrollBehavior = scrollBehavior,
            topBar = {
                if (viewState.isEditing) {
                    AllItemsEditingTopBar(viewState.selectedKeys?.size ?: 0, viewState.areAllSelected, viewState.isCollectionTrash,
                        viewModel::onDone, viewModel::toggleSelectionState, viewModel::onEmptyTrash)
                } else {
                    MobileHomeHeader(viewState.libraryName, viewState.collectionName, viewState.searchTerm.orEmpty(), isTablet, scrollBehavior,
                        onLibrary = { libraries = viewModel.availableLibraries(); pickingLibrary = true },
                        onCollections = ::showCollections, onSettings = { settings = true }, onSearch = viewModel::onSearch)
                }
            }, bottomBar = bottomBar, content = content)
    }
    if (pickingLibrary) ModalBottomSheet(onDismissRequest = { pickingLibrary = false }) {
        Text(stringResource(R.string.mobile_library_switch), Modifier.padding(16.dp), style = MaterialTheme.typography.titleLarge)
        LazyColumn(Modifier.fillMaxWidth().heightIn(max = 500.dp)) {
            items(libraries, key = { it.identifier.toString() }) { library ->
                LibraryRow(library.name, { viewModel.selectLibrary(library.identifier); pickingLibrary = false })
            }
        }
    }
    if (settings) Dialog(onDismissRequest = { settings = false }, properties = DialogProperties(usePlatformDefaultWidth = false, decorFitsSystemWindows = false)) {
        Surface(Modifier.fillMaxSize()) { SettingsNavigation(onOpenWebpage, onClose = { settings = false }) }
    }
}

@Composable
internal fun MobileHomeHeader(library: String, collection: String, query: String, isTablet: Boolean,
    scrollBehavior: TopAppBarScrollBehavior, onLibrary: () -> Unit, onCollections: () -> Unit,
    onSettings: () -> Unit, onSearch: (String) -> Unit) {
    Column {
        LibraryNavigationBar(title = "$library ▾", largeTitle = !isTablet, onTitleClick = onLibrary,
            scrollBehavior = scrollBehavior,
            navigationIcon = { LibraryIconButton(Drawables.cell_collection, stringResource(R.string.mobile_collections), onCollections) },
            actions = { LibraryIconButton(Drawables.settings_24px, stringResource(Strings.settings_title), onSettings) })
        if (collection.isNotBlank()) Text(collection, Modifier.padding(horizontal = 16.dp), style = MaterialTheme.typography.bodySmall)
        LibrarySearchField(query, onSearch)
    }
}
