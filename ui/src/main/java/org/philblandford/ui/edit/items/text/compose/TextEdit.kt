package org.philblandford.ui.edit.items.text.compose

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
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
import org.philblandford.ui.util.NumberSelector
import org.philblandford.ui.util.TextSpinner
import org.philblandford.ui.util.nullIfEmpty

@Composable
fun TextEdit(scale: Float) {
  VMView(TextEditViewModel::class.java) { model, iface, _ ->
    EditFrame(iface, scale = scale) {
      TextEditInternal(model, (iface as TextEditInterface))
    }
  }
}

@Composable
private fun TextEditInternal(model: EditModel, iface: TextEditInterface) {

  var text by remember {
    mutableStateOf(
      model.editItem.event.getParam<String>(EventParam.TEXT) ?: ""
    )
  }

  Column(Modifier.widthIn(max = 250.dp)) {

    OutlinedTextField(
      text,
      onValueChange = {
        text = it
        iface.updateParam(EventParam.TEXT, text)
      },
      colors = TextFieldDefaults.colors(
        focusedContainerColor = MaterialTheme.colorScheme.onSurface,
        focusedTextColor = MaterialTheme.colorScheme.surface,
        cursorColor = MaterialTheme.colorScheme.surface
      ),
    )

    Item {
      TextSpinner(strings = iface.getFontStrings(), selected = {
        model.editItem.event.getParam<String>(EventParam.FONT)?.nullIfEmpty() ?: iface.defaultFont
      }, onSelect = {
        iface.updateParam(EventParam.FONT, iface.getFontStrings()[it])
      }, tag = "Font"
      )
    }

    Item {
      NumberSelector(
        min = 10,
        max = 500,
        step = 10,
        num = model.editItem.event.getParam<Int>(EventParam.TEXT_SIZE) ?: iface.defaultTextSize(),
        setNum = {
          iface.updateParam(EventParam.TEXT_SIZE, it)
        },
        editable = false
      )
    }
  }
}

@Composable
private fun Item(content: @Composable () -> Unit) {
  Box(Modifier.padding(10.dp)) {
    content()
  }
}