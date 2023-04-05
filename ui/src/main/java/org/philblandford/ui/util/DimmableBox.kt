package org.philblandford.ui.util

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.onPlaced
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import com.philblandford.ascore.android.ui.style.unselectedColor
import timber.log.Timber

@Composable
fun DimmableBox(dim:Boolean, modifier: Modifier, content : @Composable BoxScope.()->Unit) {

  var size by remember{ mutableStateOf(IntSize(0,0)) }
  val density = LocalDensity.current.density

  Box(modifier.onGloballyPositioned {
    size = it.size }) {
    content()
    key(size) {
      if (dim) {
        Box(
          Modifier
            .size((size.width / density).dp, (size.height / density).dp)
            .background(unselectedColor)
        )
      }
    }
  }
}