package org.philblandford.ui.insert.items.tuplet.compose

import androidx.compose.foundation.layout.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.constraintlayout.compose.ConstraintLayout
import com.philblandford.kscore.engine.types.EventParam
import org.philblandford.ascore2.features.ui.model.InsertItem
import org.philblandford.ui.R
import org.philblandford.ui.insert.common.compose.InsertVMView
import org.philblandford.ui.insert.common.constants.panelHeight
import org.philblandford.ui.insert.items.tuplet.model.TupletInsertModel
import org.philblandford.ui.insert.items.tuplet.viewmodel.TupletInsertInterface
import org.philblandford.ui.insert.items.tuplet.viewmodel.TupletInsertViewModel
import org.philblandford.ui.util.NumberPicker
import org.philblandford.ui.util.ToggleButton

@Composable
fun TupletInsert() {
  InsertVMView<TupletInsertModel,
          TupletInsertInterface,
          TupletInsertViewModel> { state, insertItem, iface ->
    TupletInsertInternal(state, insertItem, iface)
  }
}

@Composable
private fun TupletInsertInternal(
  model: TupletInsertModel,
  insertItem: InsertItem,
  iface: TupletInsertInterface
) {
  Box(Modifier.fillMaxWidth().height(panelHeight/2)) {
    Row(
      Modifier
        .align(Alignment.Center)
    ) {
      NumberPicker(min = model.minNumerator,
        max = model.maxNumerator, editable = false, getNum = {
          insertItem.getParam(EventParam.NUMERATOR)
        }, setNum =
        { iface.setParam(EventParam.NUMERATOR, it) })
      ToggleButton(resource = R.drawable.hidden,
        tag = "HiddenToggle",
        selected = insertItem.getParam(EventParam.HIDDEN),
        toggle = {
          iface.setParam(
            EventParam.HIDDEN,
            !insertItem.getParam<Boolean>(EventParam.HIDDEN)
          )
        }
      )
    }
  }
}
