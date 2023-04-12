package org.philblandford.ui.edit.items.wedge.compose

import androidx.compose.foundation.layout.Column
import androidx.compose.runtime.Composable
import com.philblandford.kscore.engine.types.EventParam
import org.philblandford.ui.R
import org.philblandford.ui.edit.compose.RowEdit
import org.philblandford.ui.edit.entities.ButtonActions
import org.philblandford.ui.util.SquareButton
import org.philblandford.ui.util.UpDownRow
import org.philblandford.ui.util.clefIds
import org.philblandford.ui.util.wedgeIds

@Composable
fun WedgeEdit(scale: Float) {
  RowEdit(wedgeIds, scale, 1) { model, iface ->
    UpDownRow(model.editItem.event.getParam<Boolean>(EventParam.IS_UP) ?: false, {
      iface.updateParam(EventParam.IS_UP, it)
    })
  }
}