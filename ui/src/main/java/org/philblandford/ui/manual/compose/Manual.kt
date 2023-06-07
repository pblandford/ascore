package org.philblandford.ui.manual.compose

import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.viewinterop.AndroidView
import com.google.accompanist.web.WebView
import com.google.accompanist.web.rememberWebViewState
import org.philblandford.ui.R
import org.philblandford.ui.theme.DialogTheme

@Composable
fun Manual() {

  val path = "file:///android_asset/manual/en/intro.html"
  val state = rememberWebViewState(path)

  DialogTheme { modifier ->
    WebView(state,
      modifier.fillMaxHeight(0.8f).clip(RoundedCornerShape(10)))
  }
}

