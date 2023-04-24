package org.philblandford.ui.insert.items.barnumbering.compose

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.sp
import com.philblandford.kscore.engine.types.BarNumbering
import com.philblandford.kscore.log.ksLogt
import org.philblandford.ui.insert.items.barnumbering.model.BarNumberingModel
import org.philblandford.ui.insert.items.barnumbering.viewmodel.BarNumberingInterface
import org.philblandford.ui.R
import org.philblandford.ui.base.compose.VMView
import org.philblandford.ui.common.block
import org.philblandford.ui.insert.common.compose.InsertVMView
import org.philblandford.ui.insert.items.barnumbering.viewmodel.BarNumberingViewModel
import org.philblandford.ui.util.Gap
import org.philblandford.ui.util.NumberSelector
import org.philblandford.ui.util.Stoppable

@Composable
fun BarNumberingInsert() {
  InsertVMView<BarNumberingModel, BarNumberingInterface, BarNumberingViewModel>() { model, _, iface ->
    BarNumberingInternal(model, iface)
  }
}

@Composable
fun BarNumberingInternal(model: BarNumberingModel, iface: BarNumberingInterface) {
  Column(Modifier.fillMaxWidth()) {
    OptionRow(textId = R.string.no_bar_numbers, BarNumbering.NONE, model, iface)
    Gap(0.3f)
    OptionRow(textId = R.string.bars_system, BarNumbering.EVERY_SYSTEM, model, iface)
    Gap(0.3f)
    OptionRow(textId = R.string.bars_every_x, getNum(model), model, iface)
  }
}

@Composable
fun OptionRow(textId: Int, value: Any, model: BarNumberingModel, iface: BarNumberingInterface) {
  val text = stringResource(id = textId)
  val selected = model.currentOption == value
  ksLogt("$selected ${model.currentOption} $value")
  Row(verticalAlignment = Alignment.CenterVertically) {
    RadioButton(
      selected = selected,
      onClick = { iface.setOption(value) },
      modifier = Modifier.testTag(text),
      colors = RadioButtonDefaults.colors(selectedColor = MaterialTheme.colorScheme.onSurface)
    )
    Text(stringResource(id = textId), Modifier.width(block(4)), fontSize = 15.sp)
    if (value is Int) {
      Stoppable(enable = selected) {
        ksLogt("here $value")
        NumberSelector(min = 1, max = 50, num = value, setNum = {
          iface.setOption(it)
        })
      }
    }
  }
}

private fun getNum(model: BarNumberingModel): Int {
  return when (model.currentOption) {
    is Int -> model.currentOption
    else -> 4
  }
}

