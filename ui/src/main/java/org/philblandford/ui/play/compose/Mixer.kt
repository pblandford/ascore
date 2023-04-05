package org.philblandford.ui.play.compose

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.layout
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.philblandford.ui.base.compose.VMView
import org.philblandford.ui.common.Gap
import org.philblandford.ui.play.viewmodel.MixerViewModel
import timber.log.Timber

data class MixerInstrument(
  val shortName: String,
  val longName: String,
  val level: Int
)

private val gold = Color(0xffFFD700)
private val grey = Color(0xffaaaaaa)

@Composable
fun Mixer(modifier: Modifier) {
  VMView(MixerViewModel::class.java) { state, iface, _ ->
    MixerInternal(
      modifier
        .fillMaxWidth(0.95f).wrapContentHeight(),
      state.instruments,
      iface::setVolume
    )
  }
}


@Composable
private fun MixerInternal(
  modifier: Modifier, instruments: List<MixerInstrument>,
  setVolume: (Int, Int) -> Unit
) {

  Box(
    modifier
      .background(MaterialTheme.colors.surface)
      .padding(2.dp)
      .border(3.dp, MaterialTheme.colors.onSurface)
  ) {
    Column(
      Modifier
        .wrapContentHeight()
        .padding(10.dp)
    ) {
      instruments.withIndex().forEach { (idx, instrument) ->
        MixerControl(instrument) { setVolume(idx, it) }
        Gap(0.1f)
      }
    }
  }

}

@Composable
private fun MixerControl(instrument: MixerInstrument, setVolume: (Int) -> Unit) {
  Row(verticalAlignment = Alignment.CenterVertically) {
    Text(instrument.shortName,
Modifier.width(25.dp),
      fontSize = 18.sp, color = MaterialTheme.colors.onSurface, maxLines = 1)
    Gap(0.2f)
    BoxWithConstraints(
      Modifier
        .width(30.dp)
        .weight(1f)
    ) {
      MixerSlider(instrument.level, setVolume)
    }
  }
}

@Composable
private fun MixerSlider(
  value: Int,
  setVolume: (Int) -> Unit
) {
  Timber.e("value $value")
  Slider(
    value.toFloat() / 100,
    modifier = Modifier
      .fillMaxWidth()
      .height(50.dp),
    onValueChange = {
      Timber.e("change $it")
      setVolume((it * 100).toInt())
    },
    colors = SliderDefaults.colors(
      activeTrackColor = MaterialTheme.colors.onSurface,
      thumbColor = MaterialTheme.colors.onSurface
    )
  )

}

@Composable
private fun MixerButton(modifier: Modifier) {
  val lineModifier = Modifier
    .fillMaxWidth()
    .height(5.dp)
  Column(modifier.width(25.dp)) {
    Box(lineModifier.background(MaterialTheme.colors.secondary))
    Box(lineModifier.background(MaterialTheme.colors.primary))
    Box(lineModifier.background(MaterialTheme.colors.secondary))
  }
}

private val sliderModifier = Modifier
  .graphicsLayer {
    rotationZ = 270f
    transformOrigin = TransformOrigin(0f, 0f)
  }
  .layout { measurable, constraints ->
    val placeable = measurable.measure(
      Constraints(
        minWidth = constraints.minHeight,
        maxWidth = constraints.maxHeight,
        minHeight = constraints.minWidth,
        maxHeight = constraints.maxHeight,
      )
    )
    layout(placeable.height, placeable.width) {
      placeable.place(-placeable.width, 0)
    }
  }
  .fillMaxWidth()
  .height(50.dp)

@Composable
@Preview
private fun Preview() {
  MixerInternal(
    Modifier
      .fillMaxWidth()
      .height(300.dp), listOf(
      MixerInstrument("AC", "Acoustic Crumhorn", 50),
      MixerInstrument("ES", "Electric Sackbutt", 80),
      MixerInstrument("PS", "Piccolo Sousaphone", 20),
    )
  ) { _, _ -> }
}