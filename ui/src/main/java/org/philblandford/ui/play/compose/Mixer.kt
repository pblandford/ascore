package org.philblandford.ui.play.compose

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Slider
import androidx.compose.material.SliderDefaults
import androidx.compose.material.Text
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
import kotlinx.coroutines.flow.Flow
import org.philblandford.ascore2.features.playback.entities.PlaybackState
import org.philblandford.ui.base.compose.VMView
import org.philblandford.ui.common.Gap
import org.philblandford.ui.play.viewmodel.MixerInterface
import org.philblandford.ui.play.viewmodel.MixerModel
import org.philblandford.ui.play.viewmodel.MixerViewModel
import org.philblandford.ui.util.SquareButton
import org.philblandford.ui.R
import org.philblandford.ui.base.viewmodel.VMSideEffect
import org.philblandford.ui.theme.DialogTheme
import org.philblandford.ui.util.ButtonState
import org.philblandford.ui.util.ButtonState.Companion.selected
import timber.log.Timber

data class MixerInstrument(
  val shortName: String,
  val longName: String,
  val level: Int
)

@Composable
fun Mixer() {
  DialogTheme { modifier ->
    VMView(MixerViewModel::class.java) { state, iface, _ ->
      MixerInternal(
        modifier.wrapContentHeight(),
        state,
        iface
      )
    }
  }
}

@Composable
private fun MixerInternal(
  modifier: Modifier, model: MixerModel, iface: MixerInterface
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
      LazyColumn(Modifier.sizeIn(maxHeight = 400.dp)) {
        items(model.instruments.withIndex().toList()) { (idx, instrument) ->
          MixerControl(instrument) { iface.setVolume(idx, it) }
          Gap(0.1f)
        }
      }
      Gap(0.5f)
      ButtonRow(model, iface)
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

@Composable
private fun ButtonRow(model:MixerModel, iface:MixerInterface) {
  Row(
    Modifier
      .fillMaxWidth()
      .border(1.dp, MaterialTheme.colors.onSurface)
      .padding(2.dp),
  verticalAlignment = Alignment.CenterVertically) {
    SquareButton(R.drawable.shuffle,state = selected(model.playbackState.shuffle)) { iface.toggleShuffle() }
    Gap(0.5f)
    SquareButton(R.drawable.chord, state = selected(model.playbackState.harmonies)) { iface.toggleHarmonies() }
    Gap(0.5f)
    SquareButton(R.drawable.loop, state = selected(model.playbackState.loop)) { iface.toggleLoop() }
    Gap(0.5f)
  }
}

@Composable
@Preview
private fun Preview() {
  MixerInternal(
    Modifier
      .fillMaxWidth()
      .height(300.dp),
    MixerModel(
    listOf(
      MixerInstrument("AC", "Acoustic Crumhorn", 50),
      MixerInstrument("ES", "Electric Sackbutt", 80),
      MixerInstrument("PS", "Piccolo Sousaphone", 20),
    ), PlaybackState(false, false, false)),
    object : MixerInterface {
      override fun reset() {
        TODO("Not yet implemented")
      }

      override fun getSideEffects(): Flow<VMSideEffect> {
        TODO("Not yet implemented")
      }

      override fun setVolume(idx: Int, volume: Int) {
        TODO("Not yet implemented")
      }

      override fun toggleLoop() {
        TODO("Not yet implemented")
      }

      override fun toggleShuffle() {
        TODO("Not yet implemented")
      }

      override fun toggleHarmonies() {
        TODO("Not yet implemented")
      }
    }
  )
}