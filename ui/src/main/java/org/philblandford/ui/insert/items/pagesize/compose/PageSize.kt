package org.philblandford.ui.insert.items.pagesize.compose

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import org.philblandford.ui.base.compose.VMView
import org.philblandford.ui.common.block
import org.philblandford.ui.insert.common.compose.InsertVMView
import org.philblandford.ui.insert.items.pagesize.model.PageSizeModel
import org.philblandford.ui.insert.items.pagesize.viewmodel.PageSizeInterface
import org.philblandford.ui.insert.items.pagesize.viewmodel.PageSizeViewModel
import org.philblandford.ui.util.Gap
import org.philblandford.ui.util.NumberSelector

@Composable
fun PageSize() {
  InsertVMView<PageSizeModel, PageSizeInterface, PageSizeViewModel>() { model, _, iface ->
    PageSizeInternal(model, iface)
  }
}

@Composable
private fun PageSizeInternal(model: PageSizeModel, iface: PageSizeInterface) {
  Row(
    Modifier
      .fillMaxWidth()
      .padding(10.dp)) {
    Number(model, iface)
    Gap(0.5f)
    Presets(model, iface)
  }
}


@Composable
private fun Number(model: PageSizeModel, iface: PageSizeInterface) {
  NumberSelector(
    min = model.minSize, max = model.maxSize,
    num = model.currentSize, setNum = {
      iface.setPageSize(it)
    }, step = model.step
  )
}

@Composable
private fun Presets(model: PageSizeModel, iface: PageSizeInterface) {
  Row {
    com.philblandford.kscore.engine.types.PageSize.values().dropLast(2).forEach { pageSize ->
      Text(pageSize.toString(),
        Modifier
          .size(block())
          .clickable(onClick = {
            iface.setPreset(pageSize)
          })
      )
    }
  }
}
