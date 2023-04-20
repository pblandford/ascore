package org.philblandford.ui.manual.compose

import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.viewinterop.AndroidView
import org.philblandford.ui.R
import org.philblandford.ui.theme.DialogTheme

@Composable
fun Manual() {

    val path = "file:///android_asset/manual/en/intro.html"

    DialogTheme {

        AndroidView({ context ->
            WebView(context).apply {
                webViewClient = ManualClient()
                loadUrl(path)
            }
        })
    }
}

private class ManualClient : WebViewClient() {
    override fun shouldOverrideUrlLoading(view: WebView, request: WebResourceRequest): Boolean {
        view.loadUrl(request.url.toString())
        return true
    }
}