import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.viewinterop.AndroidView
import com.google.accompanist.web.WebView
import com.google.accompanist.web.rememberWebViewState
import com.philblandford.kscore.log.ksLogt
import org.philblandford.ui.R
import org.philblandford.ui.common.block
import org.philblandford.ui.theme.DialogTheme
import org.philblandford.ui.util.SquareButton
import org.philblandford.ui.util.StyledBox

@Composable
fun Help(helpKey: String, dismiss: () -> Unit) {

  ksLogt(helpKey)
  val path = "file:///android_asset/manual/en/$helpKey.html"
  val webViewState = rememberWebViewState(path)

  DialogTheme(dismiss) { modifier ->
    Box(modifier.wrapContentHeight()) {
      WebView(webViewState, Modifier.clip(RoundedCornerShape(10)))
      SquareButton(
        R.drawable.cross, size = block(0.5), onClick = dismiss, modifier = Modifier.align(
          Alignment.TopEnd
        )
      )
    }
  }
}

