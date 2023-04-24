package org.philblandford.ui.edit.items.harmony.compose

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.R
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.philblandford.kscore.engine.types.EventParam
import com.philblandford.kscore.engine.types.Pitch
import com.philblandford.kscore.log.ksLogt
import org.philblandford.ui.base.compose.VMView
import org.philblandford.ui.common.Gap
import org.philblandford.ui.common.block
import org.philblandford.ui.edit.compose.EditFrame
import org.philblandford.ui.edit.items.harmony.compose.model.HarmonyEditModel
import org.philblandford.ui.edit.items.harmony.viewmodel.HarmonyEditInterface
import org.philblandford.ui.edit.items.harmony.viewmodel.HarmonyEditViewModel
import org.philblandford.ui.edit.model.EditModel
import org.philblandford.ui.edit.viewmodel.EditInterface
import org.philblandford.ui.util.TextSpinner

@Composable
fun HarmonyEdit(scale:Float) {
  VMView(HarmonyEditViewModel::class.java) { model, iface, _ ->
    EditFrame(iface, scale = scale) {
      HarmonyEditInternal(model, iface as HarmonyEditInterface)
    }
  }
}

@Composable
private fun HarmonyEditInternal(model: EditModel, iface:HarmonyEditInterface) {

  Column(Modifier.padding(5.dp)) {
    ParamRow(model, iface)
  //  TextEdit(model, iface)
  }
}

@Composable
private fun ParamRow(model: EditModel, iface: HarmonyEditInterface) {
  val notes = iface.getNotes()
  Row {
    TextSpinner(strings = notes.map { it.letterString() },
      grid = true,
      gridRows = 4, gridColumns = 5,
      buttonBorder = true,
      tag = "Harmony",
      modifier = Modifier.size(block(2), block(1)),
      selected = { model.editItem.event.getParam<Pitch>(EventParam.TONE)?.letterString() ?: "" },
      onSelect = {
        iface.updateParam(EventParam.TONE, notes[it])
      },
      textAlign = TextAlign.Center
    )
    Gap(0.5f)
    TextSpinner(strings = iface.getQualities(),
      grid = true,
      gridRows = 4, gridColumns = 5,
      buttonBorder = true,
      tag = "Quality",
      textStyle = { MaterialTheme.typography.body2 },
      modifier = Modifier.size(block(2), block(1)),
      itemModifier = Modifier.width(block(2)),
      selected = { model.editItem.event.getParam<String>(EventParam.QUALITY) ?: "" },
      onSelect = { iface.updateParam(EventParam.QUALITY, iface.getQualities()[it]) },
      textAlign = TextAlign.Center
    )
    Text("/", Modifier.width(block()), textAlign = TextAlign.Center)
    TextSpinner(strings = iface.getNotes().map { it.letterString() },
      grid = true,
      gridRows = 7, gridColumns = 3,
      buttonBorder = true,
      tag = "Root",
      modifier = Modifier.size(block(2), block(1)),
      itemModifier = androidx.compose.ui.Modifier.size(block(1.5)),
      selected = { model.editItem.event.getParam<Pitch>(EventParam.ROOT)?.letterString() ?: "" },
      onSelect = { iface.updateParam(EventParam.ROOT, notes[it]) },
      textAlign = TextAlign.Center
    )
  }
}
//
//@Composable
//private fun TextEdit(model: HarmonyEditModel, iface: HarmonyEditInterface) {
//  Item {
//    TextSpinner(strings = model.fontStrings, selected = {
//      model.currentFont
//    }, onSelect = {
//      cmd(EditIntent.SetParam(model.fontParam, it))
//    }, tag = "Font"
//    )
//  }
//
//  Item {
//    NumberPicker(min = model.minTextSize, max = model.maxTextSize, step = 10,
//      getNum = { model.getParamNullable(model.sizeParam) ?: model.defaultTextSize }, setNum = {
//        cmd(EditIntent.SetParam(model.sizeParam, it))
//      }, editable = false
//    )
//  }
//}

@Composable
private fun Item(children: @Composable() () -> Unit) {
  Box(Modifier.padding(10.dp)) {
    children()
  }
}