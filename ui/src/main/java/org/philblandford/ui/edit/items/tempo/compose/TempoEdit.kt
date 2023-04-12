package org.philblandford.ui.edit.items.tempo.compose

import androidx.compose.runtime.Composable
import com.philblandford.kscore.engine.duration.Duration
import com.philblandford.kscore.engine.duration.crotchet
import com.philblandford.kscore.engine.tempo.Tempo
import com.philblandford.kscore.engine.types.EventParam
import org.philblandford.ui.base.compose.VMView
import org.philblandford.ui.edit.compose.EditFrame
import org.philblandford.ui.edit.model.EditModel
import org.philblandford.ui.edit.viewmodel.EditInterface
import org.philblandford.ui.edit.viewmodel.EditViewModel
import org.philblandford.ui.util.TempoSelector

@Composable
fun TempoEdit(scale: Float) {
  VMView(EditViewModel::class.java) { model, iface, _ ->
    TempoEditInternal(model, iface, scale)
  }
}

@Composable
private fun TempoEditInternal(model: EditModel, iface: EditInterface, scale: Float) {
  EditFrame(iface, scale = scale) {
    val tempo = Tempo(
      model.editItem.event.getParam<Duration>(EventParam.DURATION) ?: crotchet(),
      model.editItem.event.getParam<Int>(EventParam.BPM) ?: 120
    )
    TempoSelector(tempo) { newTempo ->
       if (newTempo.bpm != tempo.bpm) {
         iface.updateParam(EventParam.BPM, newTempo.bpm)
       }
      if (newTempo.duration != tempo.duration) {
        iface.updateParam(EventParam.DURATION, newTempo.duration)
      }
    }
  }
}