package org.philblandford.ui.edit.compose

import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import org.philblandford.ui.R
import org.philblandford.ui.edit.entities.ButtonActions
import org.philblandford.ui.edit.viewmodel.EditInterface
import org.philblandford.ui.util.Gap
import org.philblandford.ui.util.SquareButton

@Composable
fun EditFrame(
  editInterface: EditInterface,
  actions: List<ButtonActions> = ButtonActions.values().toList(),
  scale:Float,
  content: @Composable () -> Unit,
  ) {

  Column(Modifier.border(1.dp, MaterialTheme.colors.onSurface).padding(5.dp),
  horizontalAlignment = Alignment.CenterHorizontally) {
    content()
    DefaultActions(actions, editInterface, scale)
  }
}

@Composable
private fun DefaultActions(actions: List<ButtonActions>, editInterface: EditInterface,
scale: Float, modifier: Modifier = Modifier) {
  val buttonSize = 30.dp
  Row(modifier, verticalAlignment = Alignment.CenterVertically) {
    if (actions.contains(ButtonActions.MOVE)) {
      SquareButton(R.drawable.hold, size = buttonSize, modifier = Modifier.pointerInput(Unit) {
        detectDragGestures { _, dragAmount ->
          editInterface.move((dragAmount.x / scale).toInt(), (dragAmount.y / scale).toInt())
        }
      })
    }
    Gap(10.dp)
    if (actions.contains(ButtonActions.CLEAR)) {
      SquareButton(R.drawable.cross, size = buttonSize * 0.8f) {
        editInterface.clear()
      }
    }
    Gap(10.dp)
    if (actions.contains(ButtonActions.DELETE)) {
      SquareButton(R.drawable.eraser, size = buttonSize) {
        editInterface.delete()
      }
    }
  }
}