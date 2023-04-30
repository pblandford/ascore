package org.philblandford.ui.edit.items.text.compose

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.philblandford.kscore.engine.types.EventParam
import org.philblandford.ui.base.compose.VMView
import org.philblandford.ui.edit.compose.EditFrame
import org.philblandford.ui.edit.items.text.viewmodel.TextEditInterface
import org.philblandford.ui.edit.items.text.viewmodel.TextEditViewModel
import org.philblandford.ui.edit.model.EditModel
import org.philblandford.ui.util.FreeKeyboard
import org.philblandford.ui.util.NumberSelector
import org.philblandford.ui.util.TextSpinner
import org.philblandford.ui.util.nullIfEmpty
import timber.log.Timber

@Composable
fun LyricEdit(scale:Float) {
  Timber.e("RECO TextEdit $scale")
  VMView(TextEditViewModel::class.java) { model, iface, _ ->
    EditFrame(iface, scale = scale) {
      LyricEditInternal(model, (iface as TextEditInterface))
    }
  }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun LyricEditInternal(model: EditModel, iface: TextEditInterface) {

  var text by remember { mutableStateOf(model.editItem.event.getParam<String>(EventParam.TEXT) ?: "") }

  Column {
    OutlinedTextField(
      text,
      onValueChange = {
        text = it
        iface.updateParam(EventParam.TEXT, text)
      },
      colors = TextFieldDefaults.textFieldColors(
        containerColor = MaterialTheme.colorScheme.onSurface,
        textColor = MaterialTheme.colorScheme.surface,
        cursorColor = MaterialTheme.colorScheme.surface
      ),
    )

  }
}
