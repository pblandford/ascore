package org.philblandford.ui.keyboard.compose

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import org.philblandford.ui.base.compose.VMView
import org.philblandford.ui.keyboard.viewmodel.InputInterface
import org.philblandford.ui.keyboard.viewmodel.InputModel
import org.philblandford.ui.keyboard.viewmodel.InputViewModel

@Composable
fun KeyboardPanel() {
  VMView(InputViewModel::class.java) { state, iface, _ ->
    KeyboardPanelInternal(state, iface)
  }
}

@Composable
private fun KeyboardPanelInternal(
  model: InputModel, iface: InputInterface, modifier: Modifier = Modifier) {

  Column(modifier.fillMaxWidth()) {
    NoteInputButtonsColumn(model, iface)
    KeyboardImage(Modifier, iface::insertNote)
  }
}