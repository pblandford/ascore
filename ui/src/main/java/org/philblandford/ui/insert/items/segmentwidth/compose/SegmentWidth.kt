package org.philblandford.ui.insert.items.segmentwidth.compose

import android.R
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import org.philblandford.ui.base.compose.VMView
import org.philblandford.ui.common.block
import org.philblandford.ui.insert.common.compose.InsertVMView
import org.philblandford.ui.insert.items.segmentwidth.model.SegmentWidthModel
import org.philblandford.ui.insert.items.segmentwidth.viewmodel.SegmentWidthInterface
import org.philblandford.ui.insert.items.segmentwidth.viewmodel.SegmentWidthViewModel
import org.philblandford.ui.util.Gap
import org.philblandford.ui.util.NumberSelector
import org.philblandford.ui.util.SquareButton

@Composable
fun SegmentWidth() {
  InsertVMView<SegmentWidthModel, SegmentWidthInterface, SegmentWidthViewModel> { model, _, iface ->
    SegmentWidthInternal(model, iface)
  }
}

@Composable
private fun SegmentWidthInternal(model: SegmentWidthModel, iface:SegmentWidthInterface) {
  Box(Modifier.fillMaxWidth().padding(10.dp)) {
    Row(Modifier.align(Alignment.CenterStart)) {
      NumberSelector(
        min = model.min,
        max = model.max,
        model.current,
        setNum = {
          iface.setSegmentWidth(it)
        },
        step = 25
      )
      Gap(block())
      SquareButton(resource = R.drawable.ic_menu_close_clear_cancel, onClick = {
        iface.clearSegmentWidth()
      }, tag = "ClearButton")
    }
  }
}