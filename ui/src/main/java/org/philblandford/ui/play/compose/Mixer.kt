package org.philblandford.ui.play.compose

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Slider
import androidx.compose.material.Text
import androidx.compose.material.darkColors
import androidx.compose.runtime.Composable
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
fun Mixer() {
  VMView(MixerViewModel::class.java) { state, iface, _ ->
    MixerInternal(
      Modifier
        .fillMaxWidth(0.95f)
        .height(350.dp),
      state.instruments,
      iface::setVolume
    )
  }
}

@Composable
fun MixerTheme(
  modifier: Modifier = Modifier,
  content: @Composable () -> Unit
) {
  val colors = darkColors(
    primary = gold,
    secondary = grey,
    background = Color.Black
  )
  MaterialTheme(colors) {
    Box(modifier.background(MaterialTheme.colors.background)) {
      content()
    }
  }
}

@Composable
private fun MixerInternal(
  modifier: Modifier, instruments: List<MixerInstrument>,
  setVolume: (Int, Int) -> Unit
) {

  MixerTheme(modifier) {
    Box(
      Modifier
        .fillMaxSize()
        .padding(2.dp)
        .border(3.dp, MaterialTheme.colors.primary)
    ) {
      Row(
        Modifier
          .fillMaxSize()
          .padding(10.dp)
      ) {
        instruments.withIndex().forEach { (idx, instrument) ->
          MixerControl(instrument) { setVolume(idx, it) }
          Gap(0.1f)
        }
      }
    }
  }
}

@Composable
private fun MixerControl(instrument: MixerInstrument, setVolume: (Int) -> Unit) {
  Column {

    BoxWithConstraints(
      Modifier
        .width(30.dp)
        .weight(1f)
    ) {
      MixerSlider(instrument.level, setVolume)
    }
    Gap(0.2f)
    Text(instrument.shortName, fontSize = 18.sp, color = MaterialTheme.colors.primary)
  }
}

@Composable
private fun MixerSlider(value: Int,
setVolume: (Int) -> Unit) {
  Timber.e("value $value")
  Slider(value.toFloat() / 100,
    modifier = sliderModifier, onValueChange = {
      Timber.e("change $it")
      setVolume((it * 100).toInt())
    })

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
  ){ _,_ -> }
}