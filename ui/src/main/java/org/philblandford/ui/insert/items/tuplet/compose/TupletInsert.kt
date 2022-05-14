package org.philblandford.ui.insert.items.tuplet.compose

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.constraintlayout.compose.ConstraintLayout
import com.philblandford.kscore.engine.types.EventParam
import org.philblandford.ui.R
import org.philblandford.ui.insert.common.compose.InsertVMView
import org.philblandford.ui.insert.common.constants.panelHeight
import org.philblandford.ui.insert.items.tuplet.model.tupletModel
import org.philblandford.ui.insert.items.tuplet.viewmodel.TupletInsertInterface
import org.philblandford.ui.insert.items.tuplet.viewmodel.TupletInsertModel
import org.philblandford.ui.insert.items.tuplet.viewmodel.TupletInsertViewModel
import org.philblandford.ui.util.NumberPicker
import org.philblandford.ui.util.ToggleButton

@Composable
fun TupletInsert() {
  InsertVMView<TupletInsertModel,
          TupletInsertInterface,
          TupletInsertViewModel>(tupletModel) { state, iface ->
    TupletInsertInternal(state, iface)
  }
}

@Composable
private fun TupletInsertInternal(model: TupletInsertModel, iface: TupletInsertInterface) {
  ConstraintLayout(
    Modifier
      .fillMaxWidth().height(panelHeight)
      .testTag("TupletInsert")) {
    val (picker, checkBox) = createRefs()
    Box(Modifier.constrainAs(picker) { centerVerticallyTo(parent) }) {
      NumberPicker(min = model.minNumerator,
        max = model.maxNumerator, editable = false, getNum = {
          model.getParam(EventParam.NUMERATOR)
        }, setNum =
        { iface.setParam(EventParam.NUMERATOR, it) })
    }
    Box(Modifier.constrainAs(checkBox) { start.linkTo(picker.end); centerVerticallyTo(parent) }) {
      ToggleButton(resource = R.drawable.hidden,
        tag = "HiddenToggle",
        selected =  model.getParam(EventParam.HIDDEN),
        toggle = {
          iface.setParam(
              EventParam.HIDDEN,
              !model.getParam<Boolean>(EventParam.HIDDEN)
            )
        }
      )
    }
  }
}