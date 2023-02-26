package org.philblandford.ui.insert.items.tempo.compose

import androidx.compose.foundation.layout.Row
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import com.philblandford.kscore.engine.duration.Duration
import com.philblandford.kscore.engine.duration.dot
import com.philblandford.kscore.engine.duration.numDots
import com.philblandford.kscore.engine.duration.undot
import com.philblandford.kscore.engine.types.EventParam
import org.philblandford.ascore2.features.ui.model.InsertItem
import org.philblandford.ui.insert.common.compose.InsertVMView
import org.philblandford.ui.insert.common.viewmodel.DefaultInsertViewModel
import org.philblandford.ui.insert.model.InsertInterface
import org.philblandford.ui.insert.model.InsertModel
import org.philblandford.ui.util.TempoSelector

@Composable
fun TempoInsert() {
  InsertVMView<InsertModel, InsertInterface<InsertModel>, DefaultInsertViewModel>(
  ) { _, insertItem, iface->
    TempoInsertInternal(insertItem, iface)
  }
}


@Composable
fun TempoInsertInternal(insertItem: InsertItem, iface:InsertInterface<InsertModel>) {
  val isDotted = insertItem.getParam<Duration>(EventParam.DURATION).numDots() > 0
  val duration:Duration = insertItem.getParam(EventParam.DURATION)
  Row(verticalAlignment = Alignment.CenterVertically) {
    TempoSelector(getDuration = { duration },
      setDuration = { iface.setParam(EventParam.DURATION, it) },
      { isDotted },
      {
        iface.setParam(EventParam.DURATION, if (isDotted) duration.undot() else duration.dot(1))
      },
      getBpm = { insertItem.getParam(EventParam.BPM) },
      setBpm = { iface.setParam(EventParam.BPM, it) })
  }
}
