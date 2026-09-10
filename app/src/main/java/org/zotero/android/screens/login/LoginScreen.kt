package org.zotero.android.screens.login

import android.content.Intent
import android.net.Uri
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.autofill.ContentType
import androidx.compose.ui.semantics.contentType
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import org.zotero.android.R
import org.zotero.android.screens.login.data.RequestKind
import org.zotero.android.uicomponents.library.*

@Composable
internal fun LoginScreen(onBack: () -> Unit, navigateToDashboard: () -> Unit, viewModel: LoginViewModel = hiltViewModel()) {
    val state by viewModel.viewStates.observeAsState(LoginViewState())
    val effect by viewModel.viewEffects.observeAsState()
    var web by remember { mutableStateOf(viewModel.screenArgs.requestKind == RequestKind.createAccount) }
    val context = LocalContext.current
    val back = { viewModel.cancelNativeLogin(); onBack() }
    BackHandler(onBack = back)
    LaunchedEffect(effect) {
        when (effect?.consume()) {
            LoginViewEffect.NavigateBack -> onBack()
            LoginViewEffect.NavigateToDashboard -> navigateToDashboard()
            null -> Unit
        }
    }
    LibraryTheme {
        LibraryScaffold(topBar = { LibraryNavigationBar("Zotero", onBack = back) }) {
            if (web) {
                Column(Modifier.fillMaxSize()) {
                    if (state.webError) Text(stringResource(R.string.mobile_login_network), Modifier.padding(16.dp), color = MaterialTheme.colorScheme.error)
                    TextButton(onClick = viewModel::retryWebLogin) { Text(stringResource(R.string.mobile_retry)) }
                    LoginWebView(viewModel)
                }
            } else NativeLoginForm(state, viewModel::signIn,
                onWeb = { viewModel.cancelNativeLogin(); web = true },
                onRegister = { context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("https://www.zotero.org/user/register"))) },
                onForgot = { context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("https://www.zotero.org/user/lostpassword"))) })
        }
    }
}

@Composable
internal fun NativeLoginForm(state: LoginViewState, onSignIn: (String, String) -> Unit, onWeb: () -> Unit, onRegister: () -> Unit, onForgot: () -> Unit) {
    // Never save credentials into SavedStateHandle, instance state, preferences or logs.
    var username by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var visible by remember { mutableStateOf(false) }
    val keyboard = LocalSoftwareKeyboardController.current
    val submit = { if (!state.nativeBusy && username.isNotBlank() && password.isNotEmpty()) {
        keyboard?.hide(); onSignIn(username, password); password = ""
    } }
    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(24.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Spacer(Modifier.height(24.dp))
        Text(stringResource(R.string.mobile_login_title), style = MaterialTheme.typography.headlineLarge)
        Text(stringResource(R.string.mobile_login_description), color = MaterialTheme.colorScheme.onSurfaceVariant)
        OutlinedTextField(username, { username = it }, label = { Text(stringResource(R.string.mobile_username)) }, singleLine = true,
            enabled = !state.nativeBusy, modifier = Modifier.fillMaxWidth().semantics { contentType = ContentType.Username },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email, imeAction = ImeAction.Next))
        OutlinedTextField(password, { password = it }, label = { Text(stringResource(R.string.mobile_password)) }, singleLine = true,
            enabled = !state.nativeBusy, modifier = Modifier.fillMaxWidth().semantics { contentType = ContentType.Password },
            visualTransformation = if (visible) VisualTransformation.None else PasswordVisualTransformation(),
            trailingIcon = { TextButton(onClick = { visible = !visible }) { Text(stringResource(if (visible) R.string.mobile_hide_password else R.string.mobile_show_password)) } },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password, imeAction = ImeAction.Go), keyboardActions = KeyboardActions(onGo = { submit() }))
        state.nativeError?.let { Text(stringResource(it), color = MaterialTheme.colorScheme.error) }
        Button(onClick = { submit() }, enabled = !state.nativeBusy && username.isNotBlank() && password.isNotEmpty(), modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp)) {
            Text(stringResource(if (state.nativeBusy) R.string.mobile_login_busy else R.string.mobile_login))
        }
        TextButton(onClick = onForgot) { Text(stringResource(R.string.mobile_forgot)) }
        OutlinedButton(onClick = { password = ""; onWeb() }, modifier = Modifier.fillMaxWidth()) { Text(stringResource(R.string.mobile_login_web)) }
        TextButton(onClick = onRegister, modifier = Modifier.fillMaxWidth()) { Text(stringResource(R.string.mobile_register)) }
    }
}
