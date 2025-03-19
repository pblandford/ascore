package org.philblandford.ui.edit.items.text.compose

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.philblandford.kscore.engine.types.EventParam
import org.philblandford.ui.base.compose.VMView
import org.philblandford.ui.edit.compose.EditFrame
import org.philblandford.ui.edit.entities.ButtonActions
import org.philblandford.ui.edit.items.text.viewmodel.LyricEditInterface
import org.philblandford.ui.edit.items.text.viewmodel.LyricEditViewModel
import org.philblandford.ui.edit.model.EditModel
import org.philblandford.ui.util.Gap
import org.philblandford.ui.util.UpDownRow
import timber.log.Timber

@Composable
fun LyricEdit(scale: Float) {
    Timber.e("RECO TextEdit $scale")
    VMView(LyricEditViewModel::class.java) { model, iface, _ ->
        EditFrame(iface, scale = scale, actions = listOf(ButtonActions.DELETE)) {
            LyricEditInternal(model, iface as LyricEditInterface)
        }
    }
}

@Composable
private fun LyricEditInternal(model: EditModel, iface: LyricEditInterface) {

    var text by remember {
        mutableStateOf(
            model.editItem.event.getParam<String>(EventParam.TEXT) ?: ""
        )
    }

    Row(Modifier.fillMaxWidth().padding(10.dp)) {
        OutlinedTextField(
            text,
            modifier = Modifier.width(200.dp),
            onValueChange = {
                text = it
                iface.updateParam(EventParam.TEXT, text)
            },
            colors = TextFieldDefaults.colors(
                focusedContainerColor = MaterialTheme.colorScheme.onSurface,
                focusedTextColor = MaterialTheme.colorScheme.surface,
                unfocusedContainerColor = MaterialTheme.colorScheme.onSurface,
                unfocusedTextColor = MaterialTheme.colorScheme.surface,
                cursorColor = MaterialTheme.colorScheme.surface
            ),
        )
        Gap(10.dp)

        iface.upDownFlow.collectAsState(false).value.let { up ->
            UpDownRow(up, { iface.toggleUpDown() })
        }
    }
}
