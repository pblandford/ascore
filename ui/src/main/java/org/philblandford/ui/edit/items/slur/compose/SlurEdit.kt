package org.philblandford.ui.edit.items.slur.compose

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.philblandford.kscore.engine.types.EventParam
import org.philblandford.ui.R
import org.philblandford.ui.base.compose.VMView
import org.philblandford.ui.common.Gap
import org.philblandford.ui.edit.compose.DragButton
import org.philblandford.ui.edit.compose.EditFrame
import org.philblandford.ui.edit.entities.ButtonActions
import org.philblandford.ui.edit.viewmodel.EditInterface
import org.philblandford.ui.edit.viewmodel.EditViewModel
import org.philblandford.ui.util.SquareButton

@Composable
fun SlurEdit(scale: Float) {
  VMView(EditViewModel::class.java) { _, iface, _ ->
    EditFrame(iface, listOf(ButtonActions.CLEAR, ButtonActions.DELETE), scale) {
      SlurEditInternal(scale, iface)
    }
  }
}

@Composable
private fun SlurEditInternal(scale: Float, iface: EditInterface) {
  val buttonSize = 30.dp
  Row {
    DragButton(size = buttonSize, scale = scale) { x, y ->
      iface.move(x, y, EventParam.HARD_START)
    }
    Gap(0.5f)
    DragButton(size = buttonSize, scale = scale) { x, y ->
      iface.move(x, y, EventParam.HARD_MID)
    }
    Gap(0.5f)
    DragButton(size = buttonSize, scale = scale) { x, y ->
      iface.move(x, y, EventParam.HARD_END)
    }
  }
}

