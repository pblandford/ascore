import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.TextView
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.text.HtmlCompat
import com.google.accompanist.web.rememberWebViewState
import com.philblandford.kscore.log.ksLogt
import org.apache.commons.io.FileUtils
import org.apache.commons.io.IOUtils
import org.philblandford.ui.R
import org.philblandford.ui.common.block
import org.philblandford.ui.theme.DialogTheme
import org.philblandford.ui.util.SquareButton
import org.philblandford.ui.util.StyledBox
import java.io.File
import java.nio.charset.Charset

@Composable
fun Help(helpKey: String, dismiss: () -> Unit) {

  ksLogt(helpKey)
  val path = "file://android_asset/manual/en/$helpKey.html"
  val webViewState = rememberWebViewState(path)

  DialogTheme(dismiss) { modifier ->
    Column(modifier.wrapContentHeight()) {
      SquareButton(
        R.drawable.cross, size = block(0.5), onClick = dismiss, modifier = Modifier.align(
          Alignment.End
        )
      )
      HtmlText(getHtml(helpKey),
        Modifier
          .fillMaxWidth()
          .wrapContentHeight())

    }
  }
}

@Composable
private fun getHtml(key: String): String {
  val assets = LocalContext.current.assets
  return try {
    val stream = assets.open("manual/en/$key.html")
    return IOUtils.toString(stream, Charset.defaultCharset())
  } catch (e: Exception) {
    "Oops ${e.message}"
  }
}

@Composable
fun HtmlText(html: String, modifier: Modifier = Modifier) {
  val foreground = MaterialTheme.colorScheme.onSurface.value shr 32
  val background = MaterialTheme.colorScheme.surface.value shr 32
  AndroidView(
    modifier = modifier.background(color = MaterialTheme.colorScheme.surface),
    factory = { context ->
      TextView(context).apply {
        setBackgroundColor(background.toInt())
        setTextColor(foreground.toInt())
        textSize = 16f
      }
    },
    update = { it.text = HtmlCompat.fromHtml(html, HtmlCompat.FROM_HTML_MODE_LEGACY) }
  )
}


