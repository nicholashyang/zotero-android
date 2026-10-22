package org.zotero.android.screens.allitems

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.foundation.background
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.res.painterResource
import org.zotero.android.architecture.ui.AdaptiveLibraryWorkspace
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
        collections = viewModel.availableCollections()
        scope.launch { drawer.open() }
    }
    LaunchedEffect(viewState.libraryName, viewState.lce) {
        if (viewState.libraryName.isNotBlank()) collections = viewModel.availableCollections()
    }
    LaunchedEffect(pickingLibrary, viewState.libraryName) {
        if (pickingLibrary && viewState.libraryName.isNotBlank()) libraries = viewModel.availableLibraries()
    }
    BackHandler(drawer.isOpen) { scope.launch { drawer.close() } }
    ModalNavigationDrawer(drawerState = drawer, gesturesEnabled = drawer.isOpen, drawerContent = {
        ModalDrawerSheet(drawerContainerColor = MaterialTheme.colorScheme.background) {
            HomeCollectionsSidebar(viewState.libraryName, viewState.collectionName, collections,
                onSelect = { viewModel.selectHomeCollection(it); scope.launch { drawer.close() } },
                onManage = { scope.launch { drawer.close() }; viewModel.navigateToCollections() })
        }
    }) {
        AdaptiveLibraryWorkspace(sidebar = {
            HomeCollectionsSidebar(viewState.libraryName, viewState.collectionName, collections,
                onSelect = viewModel::selectHomeCollection, onManage = viewModel::navigateToCollections)
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
    Column(Modifier.background(MaterialTheme.colorScheme.background)) {
        LibraryNavigationBar(title = library, largeTitle = !isTablet, onTitleClick = onLibrary,
            scrollBehavior = scrollBehavior,
            navigationIcon = { LibraryIconButton(Drawables.cell_collection, stringResource(R.string.mobile_collections), onCollections) },
            actions = { LibraryIconButton(Drawables.settings_24px, stringResource(Strings.settings_title), onSettings) })
        if (collection.isNotBlank()) Text(collection,
            Modifier.padding(horizontal = LibraryMetrics.pageInset).padding(top = 2.dp, bottom = 12.dp),
            style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        LibrarySearchField(query, onSearch)
    }
}


@Composable
internal fun HomeCollectionsSidebar(library: String, selected: String, collections: List<Collection>,
    onSelect: (Collection) -> Unit, onManage: () -> Unit) {
    LibraryScaffold(topBar = {
        LibraryNavigationBar(stringResource(R.string.mobile_collections), actions = {
            LibraryIconButton(Drawables.settings_24px, stringResource(R.string.mobile_collections), onManage)
        })
    }) {
        LazyColumn(Modifier.fillMaxSize(), contentPadding = PaddingValues(horizontal = 12.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)) {
            item {
                Text(library, Modifier.padding(horizontal = 12.dp, vertical = 12.dp),
                    style = MaterialTheme.typography.titleSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            items(collections, key = { it.identifier.toString() }) { collection ->
                val active = selected == collection.name
                Surface(shape = RoundedCornerShape(10.dp),
                    color = if (active) MaterialTheme.colorScheme.secondaryContainer else androidx.compose.ui.graphics.Color.Transparent) {
                    LibraryRow(collection.name, { onSelect(collection) }, leading = {
                        Icon(painterResource(librarySymbol(collection.iconName)), null,
                            Modifier.size(22.dp), tint = MaterialTheme.colorScheme.primary)
                    }, trailing = {})
                }
            }
            item {
                TextButton(onClick = onManage, modifier = Modifier.fillMaxWidth().padding(top = 12.dp)) {
                    Text(stringResource(R.string.mobile_collections))
                }
            }
        }
    }
}
