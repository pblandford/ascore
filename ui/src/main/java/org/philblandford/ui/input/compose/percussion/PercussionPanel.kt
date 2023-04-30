package org.philblandford.ui.input.compose.percussion

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Popup
import com.philblandford.kscore.api.NoteInputDescriptor
import com.philblandford.kscore.api.PercussionDescr
import com.philblandford.kscore.engine.types.Accidental
import com.philblandford.kscore.log.ksLogt
import org.philblandford.ui.base.compose.VMView
import org.philblandford.ui.common.Gap
import org.philblandford.ui.common.block
import org.philblandford.ui.input.compose.keyboard.NoteInputButtonsColumn
import org.philblandford.ui.input.model.InputModel
import org.philblandford.ui.input.viewmodel.InputInterface
import org.philblandford.ui.input.viewmodel.InputViewModel
import org.philblandford.ui.stubs.StubInputInterface
import org.philblandford.ui.util.isCompact

@Composable
fun PercussionInputPanel() {
  VMView(InputViewModel::class.java) { model, iface, _ ->
    PercussionInputPanelInternal(model, iface)
  }
}

@Composable
private fun PercussionInputPanelInternal(model: InputModel, iface: InputInterface) {
  ksLogt("Percussion panel ${model.percussionDescrs}")

  val maxPerRow = if (isCompact()) 4 else 10
  Column(
    Modifier
      .testTag("PercussionInputPanel")
      .fillMaxWidth()
      .wrapContentHeight()
  ) {
    NoteInputButtonsColumn(model, iface)
    PercussionButtons(maxPerRow, model, iface)
  }
}

@Composable
private fun PercussionButtons(maxPerRow: Int, model: InputModel, iface: InputInterface) {
  val descrs = model.percussionDescrs
  val nRows = descrs.size / maxPerRow + 1
  (0 until nRows).forEach { row ->
    Gap(0.3f)
    Row(
      Modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.SpaceEvenly
    ) {
      (0 until maxPerRow).forEach { item ->
        val idx = row * maxPerRow + item
        descrs.getOrNull(idx)?.let { descr ->
          InstrumentButton(descr, iface)
        }
      }
    }
  }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun InstrumentButton(descr: PercussionDescr, iface: InputInterface) {

  Box(
    Modifier
      .size(getButtonWidth(), block())
      .background(Color.Transparent, RoundedCornerShape(10))
      .border(2.dp, MaterialTheme.colorScheme.onSurface)
  ) {
    val showPopup = remember { mutableStateOf(false) }

    Text(
      getAbbreviation(descr.name),
      Modifier
        .combinedClickable(onClick = {
          if (showPopup.value) {
            showPopup.value = false
          } else {
            iface.insertNote(descr.midiId, false)
          }
        },
          onLongClick = { showPopup.value = true }
        )
        .fillMaxSize(), textAlign = TextAlign.Center)

    if (showPopup.value) {
      Popup(onDismissRequest = { showPopup.value = false }) {
        Text(
          descr.name,
          Modifier
            .clickable(onClick = { showPopup.value = false })
            .background(MaterialTheme.colorScheme.onSurface)
            .border(1.dp, MaterialTheme.colorScheme.surface)
            .padding(5.dp),
          color = MaterialTheme.colorScheme.surface
        )
      }
    }
  }
}

private fun getAbbreviation(name: String): String {
  val filtered = name.filter { it.isLetterOrDigit() || it.isWhitespace() }
  ksLogt("${filtered.split(" ")}")
  return String(filtered.split(" ").toList().map { it.firstOrNull() ?: ' ' }.toCharArray())
}

@Composable
private fun getButtonWidth(): Dp {
  return if (isCompact()) block(2) else block(1.5)
}

@Composable
@Preview
private fun Preview() {

  val descrs = (0..10).map {
    PercussionDescr(1, 41, true, "Perc$it")
  }

  val model = InputModel(
    NoteInputDescriptor(), Accidental.values().toList(),
    descrs
  )

  PercussionInputPanelInternal(model, StubInputInterface())

}