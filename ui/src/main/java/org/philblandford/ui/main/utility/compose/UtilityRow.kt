package org.philblandford.ui.main.utility.compose

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.constraintlayout.compose.ConstraintLayout
import org.philblandford.ascore2.features.ui.model.LayoutID
import org.philblandford.ui.R
import org.philblandford.ui.base.compose.VMView
import org.philblandford.ui.common.Gap
import org.philblandford.ui.common.block
import org.philblandford.ui.main.utility.viewmodel.UtilityInterface
import org.philblandford.ui.main.utility.viewmodel.UtilityModel
import org.philblandford.ui.main.utility.viewmodel.UtilityViewModel
import org.philblandford.ui.util.SquareButton

@Composable
fun UtilityRow(panelShowing: Boolean, togglePanel: () -> Unit) {
  VMView(UtilityViewModel::class.java) { model, iface, _ ->
    UtilityRowInternal(model, iface, panelShowing, togglePanel)
  }
}

@Composable
fun UtilityRowInternal(
  model: UtilityModel,
  iface: UtilityInterface,
  panelShowing: Boolean,
  togglePanel: () -> Unit
) {

  ConstraintLayout(
    Modifier
      .height(block())
      .fillMaxWidth()
      .background(MaterialTheme.colors.surface)
      .border(1.dp, MaterialTheme.colors.onSurface)
  ) {
    val (left, right) = createRefs()
    Row(Modifier.constrainAs(left) { start.linkTo(parent.start) }) {
      DeleteButton(model.deleteSelected, iface::delete, iface::deleteLong)
      Gap(0.3f)
      VoiceButton(model.voice, iface::toggleVoice)
      Gap(0.3f)
      ZoomInOutButtons(iface::zoomIn, iface::zoomOut)
      Gap(0.3f)
      ClearButton(iface::clear)
      Gap(0.3f)
      UndoButton(iface::undo, iface::redo)
      Gap(0.3f)
    }
    Row(Modifier.constrainAs(right) { end.linkTo(parent.end) }) {
      TogglePanelButton(panelShowing, togglePanel)
      ToggleInsertButton(model.panelType) {
        if (!panelShowing) {
          togglePanel()
        } else {
          iface.togglePanelType()
        }
      }
    }

  }

}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun DeleteButton(
  selected:Boolean, delete: () -> Unit,
  deleteLong: () -> Unit
) {
  val colors = with(MaterialTheme.colors) {
    if (selected) {
      onSurface to surface
    } else {
      surface to onSurface
    }
  }

  Box(Modifier.width(block(2))) {
    Image(
      painterResource(R.drawable.eraser),
      "",
      Modifier.width(block(2)).background(colors.first)
        .align(Alignment.Center)
        .combinedClickable(
          onClick = delete, onLongClick = deleteLong
        ),
      colorFilter = ColorFilter.tint(colors.second)
    )
  }
}

@Composable
private fun VoiceButton(voice:Int, toggle: () -> Unit) {
  val voiceRes = if (voice == 1) R.drawable.voice1 else R.drawable.voice2
  SquareButton(voiceRes) { toggle() }
}

@Composable
private fun ZoomInOutButtons(zoomIn: () -> Unit, zoomOut: () -> Unit) {
  Row() {
    SquareButton(R.drawable.zoom_in) { zoomIn() }
    SquareButton(R.drawable.zoom_out) { zoomOut() }
  }
}

@Composable
private fun ClearButton(clear: () -> Unit) {
  SquareButton(R.drawable.ic_clear_normal) { clear() }
}

@Composable
private fun UndoButton(undo: () -> Unit, redo: () -> Unit) {
  SquareButton(R.drawable.undo, onLongPress = { redo() }) { undo() }
}

@Composable
private fun TogglePanelButton(visible: Boolean, toggle: () -> Unit) {
  val res = if (visible) R.drawable.minimize else R.drawable.maximize
  SquareButton(res) { toggle() }
}

@Composable
private fun ToggleInsertButton(panelType: LayoutID, toggle: () -> Unit) {
  val res = if (panelType == LayoutID.INSERT) R.drawable.keyboard_icon else R.drawable.plus
  SquareButton(res) { toggle() }
}