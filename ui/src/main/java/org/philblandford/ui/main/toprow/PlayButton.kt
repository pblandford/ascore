package org.philblandford.ui.main.toprow

import androidx.compose.runtime.Composable
import com.philblandford.kscore.sound.PlayState
import org.philblandford.ui.R
import org.philblandford.ui.base.compose.VMView
import org.philblandford.ui.util.ButtonState.Companion.selected
import org.philblandford.ui.util.SquareButton

@Composable
fun PlayButton() {
  VMView(PlayViewModel::class.java) { state, iface, _ ->
    PlayButtonInternal(state, iface)
  }
}

@Composable
private fun PlayButtonInternal(state: PlayModel, iface: PlayInterface) {
  SquareButton(
    R.drawable.ic_media_play,
    state = selected(state.playState != PlayState.STOPPED),
    onLongPress = { iface.pause() }
  ) { iface.togglePlay() }
}