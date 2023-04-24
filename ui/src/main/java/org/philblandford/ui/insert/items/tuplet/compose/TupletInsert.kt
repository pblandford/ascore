package org.philblandford.ui.insert.items.tuplet.compose

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.philblandford.kscore.engine.types.EventParam
import org.philblandford.ascore2.features.ui.model.InsertItem
import org.philblandford.ui.R
import org.philblandford.ui.insert.common.compose.InsertVMView
import org.philblandford.ui.insert.common.constants.panelHeight
import org.philblandford.ui.insert.items.tuplet.model.TupletInsertModel
import org.philblandford.ui.insert.items.tuplet.viewmodel.TupletInsertInterface
import org.philblandford.ui.insert.items.tuplet.viewmodel.TupletInsertViewModel
import org.philblandford.ui.util.NumberSelector
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
  Box(Modifier.fillMaxWidth().height(panelHeight/2).padding(10.dp)) {
    Row {
      NumberSelector(min = model.minNumerator,
        max = model.maxNumerator, editable = false, num =
          insertItem.getParam(EventParam.NUMERATOR) ?: 3
        , setNum =
        { iface.setParam(EventParam.NUMERATOR, it) })
      ToggleButton(resource = R.drawable.hidden,
        tag = "HiddenToggle",
        selected = insertItem.getParam(EventParam.HIDDEN) ?: false,
        toggle = {
          iface.setParam(
            EventParam.HIDDEN,
            !(insertItem.getParam<Boolean>(EventParam.HIDDEN) ?: false)
          )
        }
      )
    }
  }
}
