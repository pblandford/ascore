package org.philblandford.ui.util

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.width
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.philblandford.kscore.engine.duration.dot
import com.philblandford.kscore.engine.duration.numDots
import com.philblandford.kscore.engine.duration.undot
import com.philblandford.kscore.engine.tempo.Tempo
import com.philblandford.kscore.log.ksLogv
import org.philblandford.ui.R
import org.philblandford.ui.common.block
import org.philblandford.ui.util.ButtonState.Companion.selected


@Composable
fun TempoSelector(tempo: Tempo,
                  size: Dp = block(),
                  update:(Tempo)->Unit,
) {

  Row(verticalAlignment = Alignment.CenterVertically) {
    Box(Modifier.align(Alignment.CenterVertically)) {
      DurationSelector(
        tempo.duration.undot(),
        {
          ksLogv("Duration selected $it")
          update(tempo.copy(duration = it.dot(tempo.duration.numDots())))
        }, size = size
      )
    }
    Spacer(modifier = Modifier.width(block(0.25f)))
    SquareButton(
      R.drawable.onedot, tag = "Dot", modifier = Modifier.align(Alignment.CenterVertically),
      state = selected(tempo.duration.numDots() > 0), size = size
    ) {
      update(tempo.copy(duration = if (tempo.duration.numDots() > 0) tempo.duration.undot() else
        tempo.duration.dot(1)))
    }
    Spacer(modifier = Modifier.width(size / 2))
    Text("=", Modifier.align(Alignment.CenterVertically))
    Spacer(modifier = Modifier.width(size / 2))
    val bpm: String = run {
      val num = tempo.bpm
      if (num > 0) num.toString() else ""
    }
    Box(
      Modifier
        .width(size * 3)
        .align(Alignment.CenterVertically)
        .border(
          if (tempo.bpm < 1) BorderStroke(1.dp, Color.Red) else BorderStroke(
            0.dp,
            Color.Transparent
          )
        )
    ) {
      DefocusableTextField(
        value = bpm,
        onValueChange = { str ->
          val int = str.toIntOrNull() ?: 0
          update(tempo.copy(bpm = int))
        },
        modifier = Modifier.width(block(2.5)),
        keyboardType = KeyboardType.Number
      )
    }
  }
}