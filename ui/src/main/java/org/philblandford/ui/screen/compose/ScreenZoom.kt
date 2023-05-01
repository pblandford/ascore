package org.philblandford.ui.screen.compose

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.inset
import androidx.compose.ui.graphics.drawscope.scale
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.philblandford.kscore.api.Location
import com.philblandford.kscore.engine.types.EventAddress
import org.philblandford.ui.base.compose.VMView
import org.philblandford.ui.main.window.LocalWindowSizeClass
import org.philblandford.ui.main.window.compact
import org.philblandford.ui.screen.viewmodels.ScreenZoomInterface
import org.philblandford.ui.screen.viewmodels.ScreenZoomModel
import org.philblandford.ui.screen.viewmodels.ScreenZoomViewModel

@Composable
fun ScreenZoom(modifier: Modifier, eventAddress: EventAddress) {
  VMView(ScreenZoomViewModel::class.java, modifier) { state, iface, _ ->
    LaunchedEffect(eventAddress) {
      iface.setAddress(eventAddress)
    }
    ScreenZoomInternal(state, iface)
  }
}

@Composable
private fun ScreenZoomInternal(state: ScreenZoomModel, iface: ScreenZoomInterface) {

  val scale = if (LocalWindowSizeClass.current.compact()) 0.5f else 0.3f
  val centreOffsetX = if (LocalWindowSizeClass.current.compact()) 120f else 60f
  val centreOffsetY = if (LocalWindowSizeClass.current.compact()) 50f else 60f

  state.location?.let { location ->

    val offset = Offset(location.x.toFloat() * scale - centreOffsetX, location.y.toFloat() * scale -
    centreOffsetY)
    BoxWithConstraints(
      Modifier
        .size(100.dp)
        .clip(CircleShape)
        .border(1.dp, Color.Black, CircleShape)
        .background(Color.White)
    ) {
      Canvas(
        Modifier
          .fillMaxSize()
          .background(Color.White)
      ) {
        inset(-offset.x, -offset.y) {

          scale(scale, pivot = Offset(0f, 0f)) {
            state.updateCounter.let {
              iface.drawPage(location.page, this)
            }
          }
        }
      }
    }
  }
}

@Composable
@Preview
private fun Preview() {


}