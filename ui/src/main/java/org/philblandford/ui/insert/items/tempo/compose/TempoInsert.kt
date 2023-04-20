package org.philblandford.ui.insert.items.tempo.compose

import androidx.compose.foundation.layout.Row
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import com.philblandford.kscore.engine.duration.Duration
import com.philblandford.kscore.engine.duration.crotchet
import com.philblandford.kscore.engine.tempo.Tempo
import com.philblandford.kscore.engine.types.EventParam
import com.philblandford.kscore.engine.types.paramMapOf
import org.philblandford.ascore2.features.ui.model.InsertItem
import org.philblandford.ui.insert.common.compose.InsertVMView
import org.philblandford.ui.insert.common.viewmodel.DefaultInsertViewModel
import org.philblandford.ui.insert.model.InsertInterface
import org.philblandford.ui.insert.model.InsertModel
import org.philblandford.ui.util.TempoSelector

@Composable
fun TempoInsert() {
  InsertVMView<InsertModel, InsertInterface<InsertModel>, DefaultInsertViewModel>(
  ) { _, insertItem, iface ->
    TempoInsertInternal(insertItem, iface)
  }
}


@Composable
fun TempoInsertInternal(insertItem: InsertItem, iface: InsertInterface<InsertModel>) {
  val duration = insertItem.getParam<Duration>(EventParam.DURATION) ?: crotchet()
  val bpm = insertItem.getParam<Int>(EventParam.BPM) ?: 120
  val tempo = Tempo(duration, bpm)
  Row(verticalAlignment = Alignment.CenterVertically) {
    TempoSelector(tempo) { tempo ->
      iface.updateParams(
        paramMapOf(
          EventParam.DURATION to tempo.duration,
          EventParam.BPM to tempo.bpm
        )
      )
    }
  }
}
