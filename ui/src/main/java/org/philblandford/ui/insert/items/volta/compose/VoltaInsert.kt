package org.philblandford.ui.insert.items.volta.compose

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.philblandford.kscore.engine.types.EventParam
import org.philblandford.ascore2.features.ui.model.InsertItem
import org.philblandford.ui.insert.common.compose.InsertVMView
import org.philblandford.ui.insert.common.viewmodel.DefaultInsertViewModel
import org.philblandford.ui.insert.common.viewmodel.InsertViewModel
import org.philblandford.ui.insert.model.DefaultInsertInterface
import org.philblandford.ui.insert.model.InsertInterface
import org.philblandford.ui.insert.model.InsertModel
import org.philblandford.ui.util.NumberPicker
import org.philblandford.ui.util.NumberSelector

@Composable
fun VoltaInsert() {
  InsertVMView<InsertModel, DefaultInsertInterface, DefaultInsertViewModel> { _, item, iface ->
    VoltaInsertInternal(item, iface)
  }
}

@Composable
private fun VoltaInsertInternal(insertItem: InsertItem, iface:DefaultInsertInterface) {
  Box(Modifier.fillMaxWidth().padding(10.dp)) {
    Box(Modifier.align(Alignment.CenterStart)) {
      NumberSelector(min = 1, max = 10, num = insertItem.getParam(EventParam.NUMBER) as? Int ?: 1,
        setNum = { iface.setParam(EventParam.NUMBER, it) })
    }
  }
}