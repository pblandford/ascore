package org.philblandford.ui.keyboard.compose

import android.content.res.Configuration.ORIENTATION_LANDSCAPE
import androidx.compose.foundation.Image
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.rememberScrollState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import org.philblandford.ui.R
import org.philblandford.ui.common.ScrollableRow
import org.philblandford.ui.common.block
import java.util.*

@Composable
fun KeyboardImage(modifier:Modifier = Modifier, insertNote: (Int, Boolean) -> Unit) {
  val orientation = with(LocalConfiguration.current) {orientation}

  val imageHeight = if (orientation == ORIENTATION_LANDSCAPE) block(2) else block(3)
  val imageWidth = imageHeight * 20

  val virtualHeight = imageHeight.value * LocalDensity.current.density
  val virtualWidth = imageWidth.value * LocalDensity.current.density

  val initPosition = KeyPositionCalculator.getNotePosition(56, virtualWidth)

  ScrollableRow(modifier = modifier.wrapContentHeight().fillMaxWidth(),
    scrollState = rememberScrollState(initPosition.toInt())) {
      Column {
        Image(
          painterResource(id = R.drawable.key_reconstructed_scaled),"",
          contentScale = ContentScale.FillHeight,
          modifier = Modifier.pointerInput(Unit) {
            detectTapGestures(
              onTap = {
                onTap(
                offset = it,
                imageWidth = virtualWidth,
                imageHeight = virtualHeight,
                hold = false,
                insertNote = insertNote)
              },
              onLongPress = {
                onTap(
                offset = it,
                imageWidth = virtualWidth,
                imageHeight = virtualHeight,
                hold = true,
                insertNote = insertNote)
              }
            )
          }.size(imageWidth, imageHeight).testTag("KeyboardImage")
        )
        Image(
          painterResource(id = R.drawable.cheap_diagonal_fabric), "",
          contentScale = ContentScale.FillWidth,
          modifier = Modifier.size(imageWidth, block())
        )
      }
    }
}


private fun onTap(
  offset: Offset,
  imageWidth: Float,
  imageHeight: Float,
  hold: Boolean,
  insertNote: (Int, Boolean) -> Unit
) {
  val midiVal = KeyPositionCalculator.calculateMidiVal(offset.x, offset.y, imageWidth, imageHeight)
  insertNote(midiVal, hold)
}

object KeyPositionCalculator {

  private val sWhiteNoteStartMap = TreeMap<Float, Int>()
  private val sBlackNoteStartMap = TreeMap<Float, Int>()
  private var sLastPos: Float = 0.toFloat()


  init {
    val sNotePositionsFromC =
      booleanArrayOf(true, false, true, false, true, true, false, true, false, true, false, true)

    var lastWhite = true
    var pos = -1f
    for (note in 21..108) {
      val white = sNotePositionsFromC[note % 12]
      if (white) {
        pos += if (lastWhite) 1f else 0.5f
        sWhiteNoteStartMap[pos] = note
      } else {
        pos += 0.5f
        sBlackNoteStartMap[pos] = note
      }
      lastWhite = white
      sLastPos = pos
    }
  }


  fun calculateMidiVal(x: Float, y: Float, totalWidth: Float, totalHeight: Float): Int {
    val relX = x / totalWidth * (sLastPos + 1)
    if (y > totalHeight * 0.67) {
      return sWhiteNoteStartMap.floorEntry(relX).value
    } else {
      val floorKey = sBlackNoteStartMap.floorKey(relX)
      if (floorKey != null && floorKey < relX - 1f) {
        val ceiling = sBlackNoteStartMap.ceilingEntry(relX)
        return if (ceiling != null) {
          ceiling.value
        } else {
          sBlackNoteStartMap.floorEntry(relX).value
        }
      } else {
        val floor = sBlackNoteStartMap.floorEntry(relX)
        return if (floor != null) {
          sBlackNoteStartMap.floorEntry(relX).value
        } else {
          sBlackNoteStartMap.ceilingEntry(relX).value
        }
      }
    }
  }

  fun getNotePosition(midiVal: Int, totalWidth: Float): Float {
    var pos = 1.0f
    for ((key, value) in sWhiteNoteStartMap) {
      if (value == midiVal) {
        pos = key
      }
    }
    for ((key, value) in sBlackNoteStartMap) {
      if (value == midiVal) {
        pos = key
      }
    }
    return pos / sLastPos * totalWidth
  }


}