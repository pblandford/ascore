package org.philblandford.ui.util

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.philblandford.kscore.engine.duration.Duration
import com.philblandford.kscore.engine.duration.dot
import com.philblandford.kscore.engine.duration.numDots
import com.philblandford.kscore.engine.duration.undot
import com.philblandford.kscore.log.ksLogv
import org.philblandford.ui.R
import org.philblandford.ui.common.block


@Composable
fun TempoSelector(
  getDuration: () -> Duration, setDuration: (Duration) -> Unit,
  getDot: () -> Boolean, setDot: (Boolean) -> Unit,
  getBpm: () -> Int, setBpm: (Int) -> Unit, size: Dp = block()
) {

  Row(verticalAlignment = Alignment.CenterVertically) {
    Box(Modifier.align(Alignment.CenterVertically)) {
      DurationSelector(
        { getDuration().undot() },
        {
          ksLogv("Duration selected $it")
          setDuration(it.dot(getDuration().numDots()))
        }, size = size
      )
    }
    Spacer(modifier = Modifier.width(block(0.25f)))
    SquareButton(
      R.drawable.onedot, tag = "Dot", modifier = Modifier.align(Alignment.CenterVertically),
      dim = getDot(), size = size
    ) {
      setDot(!getDot())
    }
    Spacer(modifier = Modifier.width(size / 2))
    Text("=", Modifier.align(Alignment.CenterVertically))
    Spacer(modifier = Modifier.width(size / 2))
    val bpm: String = run {
      val num = getBpm()
      if (num > 0) num.toString() else ""
    }
    Box(
      Modifier
        .width(size * 3)
        .align(Alignment.CenterVertically)
        .border(
          if (getBpm() < 1) BorderStroke(1.dp, Color.Red) else BorderStroke(
            0.dp,
            Color.Transparent
          )
        )
    ) {
      DefocusableTextField(
        value = bpm,
        onValueChange = { str ->
          val int = str.toIntOrNull() ?: 0
          setBpm(int)
        },
        modifier = Modifier.size(block(2.5), block(2)),
        keyboardType = KeyboardType.Number
      )
    }
  }
}