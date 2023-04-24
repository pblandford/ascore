package org.philblandford.ui.play.compose

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
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
import org.philblandford.ascore2.features.playback.entities.MixerInstrument
import org.philblandford.ascore2.features.playback.entities.PlaybackState
import org.philblandford.ui.base.compose.VMView
import org.philblandford.ui.common.Gap
import org.philblandford.ui.play.viewmodel.MixerInterface
import org.philblandford.ui.play.viewmodel.MixerModel
import org.philblandford.ui.play.viewmodel.MixerViewModel
import org.philblandford.ui.util.SquareButton
import org.philblandford.ui.R
import org.philblandford.ui.base.viewmodel.VMSideEffect
import org.philblandford.ui.stubs.StubMixerInterface
import org.philblandford.ui.theme.DialogTheme
import org.philblandford.ui.util.ButtonState
import org.philblandford.ui.util.ButtonState.Companion.selected
import timber.log.Timber


@Composable
fun Mixer() {
  VMView(MixerViewModel::class.java) { state, iface, _ ->
    DialogTheme { modifier ->

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
      .background(MaterialTheme.colorScheme.surface)
      .padding(2.dp)
      .border(3.dp, MaterialTheme.colorScheme.onSurface)
  ) {
    Column(
      Modifier
        .wrapContentHeight()
        .padding(10.dp)
    ) {
      LazyColumn(Modifier.sizeIn(maxHeight = 400.dp)) {
        items(model.playbackState.mixerInstruments.withIndex().toList()) { (idx, instrument) ->
          MixerControl(instrument, idx, iface)
          Gap(0.1f)
        }
      }
      Gap(0.5f)
      ButtonRow(Modifier.align(Alignment.CenterHorizontally), model, iface)
    }
  }

}

@Composable
private fun MixerControl(instrument: MixerInstrument, idx: Int, iface: MixerInterface) {
  Row(verticalAlignment = Alignment.CenterVertically) {
    Text(
      instrument.shortName,
      Modifier.width(25.dp),
      fontSize = 18.sp, color = MaterialTheme.colorScheme.onSurface, maxLines = 1
    )
    Gap(0.2f)
    BoxWithConstraints(
      Modifier
        .width(30.dp)
        .weight(1f)
    ) {
      MixerSlider(instrument.level) { iface.setVolume(idx, it) }
    }
    Gap(0.3f)
    MixerButton(Modifier, "M", instrument.muted) { iface.toggleMute(idx) }
    MixerButton(Modifier, "S", instrument.solo) { iface.toggleSolo(idx) }
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
      activeTrackColor = MaterialTheme.colorScheme.onSurface,
      thumbColor = MaterialTheme.colorScheme.onSurface
    )
  )
}

@Composable
private fun MixerButton(
  modifier: Modifier, text: String, selected: Boolean,
  toggle: () -> Unit
) {
  val background =
    if (selected) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.surface
  val foreground =
    if (selected) MaterialTheme.colorScheme.surface else MaterialTheme.colorScheme.onSurface

  Box(
    modifier
      .size(25.dp)
      .background(background)
      .clickable { toggle() }) {
    Text(
      text,
      Modifier.align(Alignment.Center), fontSize = 20.sp,
      color = foreground
    )
  }
}

@Composable
private fun ButtonRow(modifier: Modifier, model: MixerModel, iface: MixerInterface) {
  Row(
    modifier
      .border(1.dp, MaterialTheme.colorScheme.onSurface)
      .padding(2.dp),
    verticalAlignment = Alignment.CenterVertically
  ) {
    SquareButton(
      R.drawable.shuffle,
      state = selected(model.playbackState.shuffle)
    ) { iface.toggleShuffle() }
    Gap(0.5f)
    SquareButton(
      R.drawable.chord,
      state = selected(model.playbackState.harmonies)
    ) { iface.toggleHarmonies() }
    Gap(0.5f)
    SquareButton(R.drawable.loop, state = selected(model.playbackState.loop)) { iface.toggleLoop() }
    Gap(0.5f)
  }
}

@Composable
@Preview
private fun Preview() {
  val instruments = listOf(
    MixerInstrument("AC", "Acoustic Crumhorn", 50, true),
    MixerInstrument("ES", "Electric Sackbutt", 80, false),
    MixerInstrument("PS", "Piccolo Sousaphone", 20, false, true),
  )
  MixerInternal(
    Modifier
      .fillMaxWidth()
      .height(300.dp),
    MixerModel(
      PlaybackState(false, false, false, instruments)
    ),
    StubMixerInterface()
  )
}