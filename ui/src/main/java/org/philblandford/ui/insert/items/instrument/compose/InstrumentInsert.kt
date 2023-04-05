package org.philblandford.ui.insert.items.instrument.compose

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.philblandford.kscore.api.Instrument
import com.philblandford.kscore.engine.types.EventParam
import org.philblandford.ascore2.features.ui.model.InsertItem
import org.philblandford.ui.insert.common.compose.InsertVMView
import org.philblandford.ui.insert.common.viewmodel.DefaultInsertViewModel
import org.philblandford.ui.insert.items.instrument.model.InstrumentInsertModel
import org.philblandford.ui.insert.items.instrument.viewmodel.InstrumentInsertInterface
import org.philblandford.ui.insert.items.instrument.viewmodel.InstrumentInsertViewModel
import org.philblandford.ui.insert.model.InsertInterface
import org.philblandford.ui.insert.model.InsertModel
import org.philblandford.ui.util.InstrumentList

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

  InstrumentList(
    Modifier
      .fillMaxWidth()
      .height(200.dp),
    model.instrumentGroups,
    model.instrumentGroups.flatMap { it.instruments }.find { it.name == insertItem.getParam(EventParam.NAME) },
    insertInterface::setInstrument
  )

}