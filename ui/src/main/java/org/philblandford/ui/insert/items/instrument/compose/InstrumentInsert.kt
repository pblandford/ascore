package org.philblandford.ui.insert.items.instrument.compose

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.philblandford.kscore.engine.types.EventParam
import org.philblandford.ascore2.features.ui.model.InsertItem
import org.philblandford.ui.insert.common.compose.InsertVMView
import org.philblandford.ui.insert.items.instrument.model.InstrumentInsertModel
import org.philblandford.ui.insert.items.instrument.viewmodel.InstrumentInsertInterface
import org.philblandford.ui.insert.items.instrument.viewmodel.InstrumentInsertViewModel
import org.philblandford.ui.util.Gap
import org.philblandford.ui.util.InstrumentList
import org.philblandford.ui.util.UpDownColumn

@Composable
fun InstrumentInsert() {
  InsertVMView<InstrumentInsertModel, InstrumentInsertInterface, InstrumentInsertViewModel>(
  ) { model, insertItem, iface ->
    InstrumentInsertInternal(model, insertItem, iface)
  }
}

@Composable
private fun InstrumentInsertInternal(
  model: InstrumentInsertModel,
  insertItem: InsertItem, insertInterface: InstrumentInsertInterface
) {

  Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
    InstrumentList(
      Modifier
        .weight(1f)
        .height(200.dp),
      model.instrumentGroups,
      model.instrumentGroups.flatMap { it.instruments }
        .find { it.name == insertItem.getParam(EventParam.NAME) },
      insertInterface::setInstrument
    )
    Gap(0.5f)
    UpDownColumn(insertItem.getParam<Boolean>(EventParam.IS_UP) ?: false,
      { insertInterface.setParam(EventParam.IS_UP, it) })
  }
}