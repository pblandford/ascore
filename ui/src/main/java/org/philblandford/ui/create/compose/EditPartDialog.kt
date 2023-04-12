package org.philblandford.ui.create.compose

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.Button
import androidx.compose.material.MaterialTheme
import androidx.compose.material.OutlinedTextField
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.philblandford.kscore.api.Instrument
import com.philblandford.kscore.engine.types.ClefType
import org.philblandford.ui.R
import org.philblandford.ui.common.Gap
import org.philblandford.ui.common.block
import org.philblandford.ui.theme.DialogTheme
import org.philblandford.ui.theme.typographyContrast
import org.philblandford.ui.util.ClefSpinner
import org.philblandford.ui.util.DraggableList
import org.philblandford.ui.util.NumberSelector
import org.philblandford.ui.util.SquareButton

@Composable
fun EditPartDialog(instrument: Instrument, onUpdate: (Instrument) -> Unit, dismiss:()->Unit) {
  Dialog(dismiss) {
    DialogTheme { modifier ->
      Box(modifier) {
        EditPartDialogInternal(instrument, onUpdate, dismiss)
      }
    }
  }
}

@Composable
private fun EditPartDialogInternal(instrument: Instrument, onUpdate: (Instrument) -> Unit,
dismiss: () -> Unit) {

  Column(
    Modifier
      .background(MaterialTheme.colors.surface)
      .padding(10.dp)
      .testTag("InstrumentEdit ${instrument.label}")
  ) {
    Title(instrument)
    Space()
    Label(instrument, onUpdate)
    Space()
    Abbreviation(instrument, onUpdate)
    if (!instrument.percussion) {
      Space()
      Transposition(instrument, onUpdate)
      Space()
      ClefRow(instrument, onUpdate)
    }
    Gap(0.5f)
    Button({dismiss()}, Modifier.align(Alignment.End)) {
      Text(stringResource(R.string.done))
    }
  }
}

@Composable
private fun NewScoreInstrumentLandscape(instrument: Instrument, onUpdate: (Instrument) -> Unit) {
  Column(
    Modifier
      .padding(10.dp)
      .testTag("InstrumentEdit ${instrument.label}")
  ) {
    Title(instrument)
    Space()
    Row {
      Label(instrument, onUpdate)
      Space()
      Abbreviation(instrument, onUpdate)
    }
    if (!instrument.percussion) {
      Space()
      Row {
        Transposition(instrument, onUpdate)
        Space()
        ClefRow(instrument, onUpdate)
      }
      Space()
    }
  }
}

@Composable
private fun Title(instrument: Instrument) {
  Text(
    stringResource(R.string.editing, instrument.name),
    Modifier.fillMaxWidth(),
    textAlign = TextAlign.Center
  )
}

@Composable
private fun Label(instrument: Instrument, onUpdate: (Instrument) -> Unit) {
  OutlinedTextField(
    value = instrument.label,
    onValueChange = {
      onUpdate(instrument.copy(label = it))
    },
    label = { Text(stringResource(R.string.label)) },
    modifier = Modifier.testTag("LabelTextField")
  )
}

@Composable
private fun Abbreviation(instrument: Instrument, onUpdate: (Instrument) -> Unit) {
  OutlinedTextField(
    value = instrument.abbreviation,
    onValueChange = {
      onUpdate(instrument.copy(abbreviation = it))
    },
    label = { Text(stringResource(id = R.string.abbreviation)) },
    modifier = Modifier.testTag("AbbreviationTextField")
  )
}

@Composable
private fun Transposition(instrument: Instrument, onUpdate: (Instrument) -> Unit) {
  Row(verticalAlignment = Alignment.CenterVertically) {
    Text(stringResource(id = R.string.transposition) + ": ", style = typographyContrast.body1,
    color = MaterialTheme.colors.onSurface)
    Gap(1f)
    NumberSelector(min = -7, max = 7, num = instrument.transposition, setNum = {
      onUpdate(instrument.copy(transposition = it))
    })
  }
}

@Composable
private fun Space() {
  Spacer(Modifier.size(block(0.5f)))
}

@Composable
fun ClefRow(instrument: Instrument, onUpdate: (Instrument) -> Unit) {

  Row(
    Modifier.fillMaxWidth(),
    verticalAlignment = Alignment.CenterVertically,
    horizontalArrangement = Arrangement.SpaceBetween
  ) {

    DraggableList(instrument.clefs.withIndex().toList(), { clefItem ->
      ClefSpinner({ clefItem.value }, border = true, tag = "Clef", setClef = { type ->
        onUpdate(instrument.setClef(clefItem.index, type))
      })
    }, vertical = false, reorder = { fromIndex, toIndex ->
      val newItems = instrument.clefs.toMutableList().apply {
        add(toIndex, removeAt(fromIndex))
      }
      onUpdate(instrument.copy(clefs = newItems))
    }, key = { _, iv -> "${iv.index} ${iv.value}" })

    Box(Modifier) {
      SquareButton(resource = R.drawable.plus,
        tag = "AddClefButton",
        onClick = {
          onUpdate(instrument.addClef())
        })
    }
  }
}

private fun Instrument.addClef(): Instrument {
  val newClefs = clefs + ClefType.TREBLE
  return copy(clefs = newClefs)
}

private fun Instrument.removeClef(idx: Int): Instrument {
  return if (clefs.size > 1) {
    val newClefs = clefs.toMutableList()
    newClefs.removeAt(idx)
    copy(clefs = newClefs)
  } else this
}

private fun Instrument.setClef(idx: Int, clefType: ClefType): Instrument {
  val newClefs = clefs.toMutableList()
  newClefs.removeAt(idx)
  newClefs.add(idx, clefType)
  return copy(clefs = newClefs)
}

@Composable
@Preview
private fun Preview() {
  var instrument by remember {
    mutableStateOf(
      Instrument(
        "Theramin", "Ther", "Strings", 1, 0, listOf(ClefType.TREBLE),
        "default", 0
      )
    )
  }

  EditPartDialogInternal(instrument = instrument, onUpdate = { instrument = it }) {}
}
