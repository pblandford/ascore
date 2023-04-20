package org.philblandford.ui.edit.items.timesignature.compose

import androidx.compose.runtime.Composable
import com.philblandford.kscore.engine.time.TimeSignature
import org.philblandford.ui.base.compose.VMView
import org.philblandford.ui.edit.compose.EditFrame
import org.philblandford.ui.edit.entities.ButtonActions
import org.philblandford.ui.edit.model.EditModel
import org.philblandford.ui.edit.viewmodel.EditInterface
import org.philblandford.ui.edit.viewmodel.EditViewModel
import org.philblandford.ui.util.TimeSignatureSelector

@Composable
fun TimeSignatureEdit() {
    VMView(EditViewModel::class.java) { model, iface, _ ->
        EditFrame(iface, listOf(ButtonActions.DELETE), 0f) {
            TimeSignatureEditInternal(model, iface)
        }
    }
}

@Composable
private fun TimeSignatureEditInternal(model: EditModel, iface: EditInterface) {
    val ts = TimeSignature.fromParams(model.editItem.event.params)
    TimeSignatureSelector(ts, { iface.updateParams(it.toEvent().params) })
}