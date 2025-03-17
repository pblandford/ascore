package org.philblandford.ui.insert.items.repeatbeat.compose

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import org.philblandford.ui.insert.common.compose.InsertVMView
import org.philblandford.ui.insert.items.repeatbeat.model.RepeatBeatModel
import org.philblandford.ui.insert.items.repeatbeat.viewmodel.RepeatBeatInterface
import org.philblandford.ui.insert.items.repeatbeat.viewmodel.RepeatBeatViewModel
import org.philblandford.ui.util.DurationSelector

@Composable
fun RepeatBeatInsert() {
    InsertVMView<RepeatBeatModel, RepeatBeatInterface, RepeatBeatViewModel> { model, _, iface ->
        Box(Modifier.padding(10.dp)) {
            DurationSelector(model.duration, iface::setDuration)
        }
    }
}