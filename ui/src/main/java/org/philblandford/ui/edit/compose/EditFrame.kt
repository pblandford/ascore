package org.philblandford.ui.edit.compose

import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.philblandford.kscore.engine.types.EventParam
import org.philblandford.ui.R
import org.philblandford.ui.common.block
import org.philblandford.ui.edit.entities.ButtonActions
import org.philblandford.ui.edit.viewmodel.EditInterface
import org.philblandford.ui.util.Gap
import org.philblandford.ui.util.SquareButton

@Composable
fun EditFrame(
  editInterface: EditInterface,
  actions: List<ButtonActions> = ButtonActions.values().toList(),
  scale: Float,
  content: @Composable () -> Unit,
) {
  Column(
    Modifier
      .border(1.dp, MaterialTheme.colorScheme.onSurface)
      .padding(5.dp),
    horizontalAlignment = Alignment.CenterHorizontally
  ) {
    content()
    DefaultActions(actions, editInterface, scale)
  }
}

@Composable
private fun DefaultActions(
  actions: List<ButtonActions>, editInterface: EditInterface,
  scale: Float, modifier: Modifier = Modifier
) {
  val buttonSize = block()
  Row(modifier, verticalAlignment = Alignment.CenterVertically) {
    if (actions.contains(ButtonActions.MOVE)) {
      DragButton(size = buttonSize, scale = scale) { x, y ->
        editInterface.move(
          x, y,
          EventParam.HARD_START
        )
      }
      Gap(10.dp)
    }
    if (actions.contains(ButtonActions.CLEAR)) {
      SquareButton(R.drawable.cross, size = buttonSize * 0.8f) {
        editInterface.clear()
      }
      Gap(10.dp)
    }
    if (actions.contains(ButtonActions.DELETE)) {
      SquareButton(R.drawable.eraser, size = buttonSize) {
        editInterface.delete()
      }
    }
  }
}

@Composable
internal fun DragButton(
  modifier: Modifier = Modifier,
  size: Dp = block(),
  scale: Float,
  move: (Int, Int) -> Unit
) {
  var dragTotal by remember { mutableStateOf(Offset(0f, 0f)) }
  var dragCount by remember { mutableStateOf(0)}
  SquareButton(R.drawable.hold, size = size, modifier = modifier.pointerInput(Unit) {
    detectDragGestures(
      onDragStart = { dragTotal = Offset(0f,0f) },
      onDrag = { x, dragAmount ->
        dragTotal += dragAmount
        if (dragCount % 5 == 0) {
          move((dragTotal.x / scale).toInt(), (dragTotal.y / scale).toInt())
          dragTotal = Offset(0f,0f)
        }
        dragCount++
      }
      )
  })
}