package org.philblandford.ui.input.compose.selectors

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.philblandford.kscore.engine.types.GraceType
import org.philblandford.ui.R
import org.philblandford.ui.util.ButtonState.Companion.selected
import org.philblandford.ui.util.SquareButton
import org.philblandford.ui.util.styledBorder


data class GraceSelectorModel(
  val graceType: GraceType,
  val graceShift:Boolean
)

interface GraceSelectorInterface {
  fun setGrace(graceType: GraceType)
  fun toggleGraceShift()
}

@Composable
fun GraceButtons(
  model: GraceSelectorModel,
  iface: GraceSelectorInterface
) {

  Box(Modifier.border(styledBorder())) {
    Row {
      SquareButton(
        resource = R.drawable.appoggiatura_icon,
        tag = "Appoggiatura",
        onClick = {
          val grace = if (model.graceType == GraceType.APPOGGIATURA) GraceType.NONE else GraceType.APPOGGIATURA
          iface.setGrace(grace)
        }, state = selected(model.graceType == GraceType.APPOGGIATURA)
      )
      SquareButton(
        resource = R.drawable.acciaccatura,
        tag = "Acciaccatura",
        onClick = {
          val grace = if (model.graceType == GraceType.ACCIACCATURA) GraceType.NONE else GraceType.ACCIACCATURA
          iface.setGrace(grace)
        }, state = selected(model.graceType == GraceType.ACCIACCATURA)
      )
      SquareButton(
        resource = R.drawable.add_bar_right,
        tag = "GraceShift",
        onClick = {
          iface.toggleGraceShift()
        }, state = selected(model.graceShift)
      )
    }
  }
}