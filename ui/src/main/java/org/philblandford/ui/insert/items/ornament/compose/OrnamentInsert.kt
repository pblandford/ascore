package org.philblandford.ui.insert.items.ornament.compose

import GridSelection
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.material.Checkbox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import com.philblandford.kscore.engine.types.Accidental
import com.philblandford.kscore.engine.types.ArpeggioType
import com.philblandford.kscore.engine.types.EventParam
import com.philblandford.kscore.engine.types.EventType
import com.philblandford.kscore.engine.types.OrnamentType
import org.philblandford.ascore2.features.ui.model.InsertItem
import org.philblandford.ui.R
import org.philblandford.ui.insert.row.compose.RowInsert
import org.philblandford.ui.insert.row.viewmodel.RowInsertInterface
import org.philblandford.ui.insert.row.viewmodel.RowInsertModel
import org.philblandford.ui.input.compose.selectors.AccidentalSpinner
import org.philblandford.ui.util.Gap
import org.philblandford.ui.util.ornamentIds

@Composable
fun OrnamentInsert() {
  RowInsert(ornamentIds + (R.drawable.arpeggio to ArpeggioType.NORMAL)) { model, item, iface ->
    OrnamentInsertInternal(item, model, iface)
  }
}

@Composable
private fun OrnamentInsertInternal(
  insertItem: InsertItem,
  model: RowInsertModel<Enum<*>>,
  iface: RowInsertInterface<Enum<*>>
) {
  Column {
    Grid(model, iface)
    Gap(0.5f)
    CheckBoxRow(insertItem, iface)
  }
}

@Composable
private fun Grid(
  model: RowInsertModel<Enum<*>>,
  iface: RowInsertInterface<Enum<*>>
) {
  val ornamentIds = model.ids
  GridSelection(images = ornamentIds.map { it.first },
    rows = 1, columns = ornamentIds.size,
    itemBorder = true,
    selected = { model.selected },
    onSelect = {
      iface.selectItem(it)
    })
}

@Composable
private fun CheckBoxRow(item: InsertItem, iface: RowInsertInterface<Enum<*>>) {
  val type = item.getParam<Enum<*>>(EventParam.TYPE) ?: OrnamentType.TRILL
  Row() {
    if ((type as? OrnamentType)?.accidentalAbove() == true) {
      AccidentalCheckbox(true, iface)
    }
    if ((type as? OrnamentType)?.accidentalBelow() == true) {
      AccidentalCheckbox(false, iface)
    }
  }
}


@Composable
private fun AccidentalCheckbox(
  above: Boolean,
  iface: RowInsertInterface<Enum<*>>
) {
  val param = if (above) EventParam.ACCIDENTAL_ABOVE else EventParam.ACCIDENTAL_BELOW

  val enabled = remember { mutableStateOf(false) }
  val selectedAccidental = remember { mutableStateOf(Accidental.SHARP) }

  Checkbox(checked = enabled.value,
    onCheckedChange = { yes ->
      enabled.value = yes
      if (yes) {
        iface.setParam(param, selectedAccidental.value)
      } else {
        iface.setParam(param, null)
      }
    })

  AccidentalChooser(selectedAccidental.value) {
    selectedAccidental.value = it
    if (enabled.value) {
      iface.setParam(param, selectedAccidental.value)
    }
  }
}

@Composable
fun AccidentalChooser(
  selectedAccidental: Accidental,
  select: (Accidental) -> Unit
) {

  val accidentals = Accidental.values().toList().minus(Accidental.FORCE_FLAT)
  AccidentalSpinner(
    selectedAccidental = selectedAccidental,
    accidentals = accidentals
      .minus(Accidental.FORCE_SHARP),
    setAccidental = select
  )
}

private fun OrnamentType.accidentalAbove() = this != OrnamentType.LOWER_MORDENT
private fun OrnamentType.accidentalBelow() =
  this == OrnamentType.TURN || this == OrnamentType.LOWER_MORDENT