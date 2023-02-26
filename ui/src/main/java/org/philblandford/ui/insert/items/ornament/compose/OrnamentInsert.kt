package org.philblandford.ui.insert.items.ornament.compose

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.material.Checkbox
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import com.philblandford.kscore.engine.types.Accidental
import org.philblandford.ui.insert.common.compose.InsertVMView
import org.philblandford.ui.insert.common.viewmodel.InsertViewModel
import org.philblandford.ui.insert.items.ornament.model.OrnamentInsertModel
import org.philblandford.ui.insert.items.ornament.viewmodel.OrnamentInsertInterface
import org.philblandford.ui.insert.items.ornament.viewmodel.OrnamentInsertViewModel
import org.philblandford.ui.insert.model.InsertInterface
import org.philblandford.ui.util.Gap
import org.philblandford.ui.util.GridSelection

@Composable
fun OrnamentInsert() {
  InsertVMView<OrnamentInsertModel,
          OrnamentInsertInterface,
          InsertViewModel<OrnamentInsertModel, OrnamentInsertInterface>> { state, _, iface ->
    OrnamentInsertInternal(state, iface)
  }
}

@Composable
private fun OrnamentInsertInternal(model: OrnamentInsertModel, iface: OrnamentInsertInterface) {
  Column {
    Grid(model, iface)
    Gap(0.5f)
    CheckBoxRow(model, iface)
  }
}

@Composable
private fun Grid(model: OrnamentInsertModel, iface: OrnamentInsertInterface) {
  val ornamentIds = model.ids
  GridSelection(images = ornamentIds.map { it.first },
    rows = 1, columns = ornamentIds.size,
    itemBorder = true,
    selected = { model.selected },
    onSelect = {
      iface.insert(it)
    })
}

@Composable
private fun CheckBoxRow(model: OrnamentInsertModel, iface: OrnamentInsertInterface) {
  Row() {
//    if (model.accidentalAbove.isDisplayed) {
//      AccidentalSelector(true, model, cmd)
//    }
//    if (model.accidentalBelow.isDisplayed) {
//      AccidentalSelector(false, model, cmd)
//    }
  }
}

@Composable
private fun AccidentalSelector(
  above: Boolean,
  model: OrnamentInsertModel,
  iface: OrnamentInsertInterface
) {
  Row() {
    AccidentalCheckbox(above, model, iface)
  }
}

@Composable
private fun AccidentalCheckbox(
  above: Boolean,
  model: OrnamentInsertModel,
  iface: OrnamentInsertInterface
) {
//  val desc =
//    if (above) model.accidentalAbove else model.accidentalBelow
//  val tag = if (above) "AccidentalAboveCheckBox" else "AccidentalBelowCheckBox"
//  Checkbox(checked = desc.isSelected, modifier = Modifier.testTag(tag),
//    onCheckedChange = {
//      if (above) cmd(OrnamentInsertIntent.EnableAccidentalAbove(it)) else cmd(
//        OrnamentInsertIntent.EnableAccidentalBelow(
//          it
//        )
//      )
//    })
//  AccidentalChooser(above, model, cmd)
}
//
//@Composable
//fun AccidentalChooser(
//  above: Boolean,
//  model: OrnamentInsertModel,
//  cmd: (OrnamentInsertIntent) -> Unit
//) {
//  val desc =
//    if (above) model.accidentalAbove else model.accidentalBelow
//  val tag = if (above) "AccidentalAboveSpinner" else "AccidentalBelowSpinner"
//  val intent = { a: Accidental ->
//    if (above) OrnamentInsertIntent.SetAccidentalAbove(a) else OrnamentInsertIntent.SetAccidentalBelow(
//      a
//    )
//  }
//  val accidentals = Accidental.values().toList().minus(Accidental.FORCE_FLAT)
//  Box(Modifier.testTag(tag)) {
//    AccidentalSpinner(
//      selectedAccidental = { desc.accidental },
//      accidentals = accidentals
//        .minus(Accidental.FORCE_SHARP),
//      tag = {
//        accidentals[it].toString()
//      },
//      setAccidental = { cmd(intent(it)) })
//  }
//}
