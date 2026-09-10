package org.zotero.android.screens.login

import android.webkit.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.viewinterop.AndroidView

@Composable
internal fun LoginWebView(viewModel: LoginViewModel) {
    AndroidView(modifier = Modifier.fillMaxSize(), factory = { context ->
        WebView(context).apply {
            settings.javaScriptEnabled = true
            settings.domStorageEnabled = true
            settings.useWideViewPort = true
            settings.loadWithOverviewMode = true
            settings.allowFileAccess = false
            settings.allowContentAccess = false
            webViewClient = object : WebViewClient() {
                override fun onReceivedError(view: WebView, request: WebResourceRequest, error: WebResourceError) {
                    if (request.isForMainFrame) viewModel.webError(true)
                }
                override fun onReceivedHttpError(view: WebView, request: WebResourceRequest, response: WebResourceResponse) {
                    if (request.isForMainFrame) viewModel.webError(true)
                }
                override fun shouldOverrideUrlLoading(view: WebView, request: WebResourceRequest): Boolean =
                    request.url.scheme != "https"
            }
            viewModel.init(this)
        }
    }, onRelease = { view ->
        if (viewModel.webView === view) viewModel.webView = null
        view.stopLoading(); view.destroy()
    })
}
