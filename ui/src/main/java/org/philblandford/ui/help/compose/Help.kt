import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.viewinterop.AndroidView
import com.philblandford.kscore.log.ksLogt
import org.philblandford.ui.R
import org.philblandford.ui.common.block
import org.philblandford.ui.theme.DialogTheme
import org.philblandford.ui.util.SquareButton
import org.philblandford.ui.util.StyledBox

@Composable
fun Help(helpKey: String, dismiss:()->Unit) {

  ksLogt(helpKey)
  val path ="file:///android_asset/manual/en/$helpKey.html"
  var webView = WebView(LocalContext.current)

  DialogTheme { modifier ->
    Box(modifier) {
      Box(Modifier.testTag("HelpPopup")) {
        AndroidView(factory = { ctx ->
          WebView(ctx).apply {
            this.webViewClient = client
            webView = this
          }
        })
        ksLogt(path)
        webView.loadUrl(path)
        SquareButton(
          R.drawable.cross, size = block(0.5), onClick = dismiss, modifier = Modifier.align(
          Alignment.TopEnd))
      }
    }
  }

}

private val client = object : WebViewClient() {
  override fun shouldOverrideUrlLoading(
    view: WebView,
    request: WebResourceRequest
  ): Boolean {
    view.loadUrl(request.url.toString())
    return true
  }
}
