package org.philblandford.ui.keyboard.compose

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Divider
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.philblandford.kscore.api.NoteInputDescriptor
import org.philblandford.ui.base.compose.VMView
import org.philblandford.ui.input.compose.keyboard.KeyboardImage
import org.philblandford.ui.input.compose.keyboard.NoteInputButtonsColumn
import org.philblandford.ui.input.compose.keyboard.NoteInputButtonsRow
import org.philblandford.ui.input.model.InputModel
import org.philblandford.ui.input.viewmodel.InputInterface
import org.philblandford.ui.input.viewmodel.InputViewModel
import org.philblandford.ui.main.window.LocalWindowSizeClass
import org.philblandford.ui.main.window.compact
import org.philblandford.ui.stubs.StubInputInterface
import org.philblandford.ui.stubs.stubInputModel
import org.philblandford.ui.util.AScorePreview
import org.philblandford.ui.util.Gap

@Composable
fun KeyboardPanel() {
  VMView(InputViewModel::class.java) { state, iface, _ ->
    KeyboardPanelInternal(state, iface)
  }
}

@Composable
private fun KeyboardPanelInternal(
  model: InputModel, iface: InputInterface, modifier: Modifier = Modifier) {

  Column(
    modifier
      .fillMaxWidth()
      .padding(2.dp)) {
    Divider(color = MaterialTheme.colorScheme.onSurface)
    Gap(1.dp)
    if (LocalWindowSizeClass.current.compact()) {
      NoteInputButtonsColumn(model, iface)
    } else {
      NoteInputButtonsRow(model, iface)
    }
    KeyboardImage(Modifier, iface::insertNote)
  }
}

@Composable
@Preview
private fun Preview() {
  AScorePreview {
    KeyboardPanelInternal(stubInputModel, StubInputInterface())
  }
}