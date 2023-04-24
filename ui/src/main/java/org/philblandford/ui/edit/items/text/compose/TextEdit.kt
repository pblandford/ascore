package org.philblandford.ui.edit.items.text.compose

import android.view.WindowManager
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material.MaterialTheme
import androidx.compose.material.OutlinedTextField
import androidx.compose.material.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.philblandford.kscore.engine.types.EventParam
import com.philblandford.kscore.log.ksLogt
import org.philblandford.ui.LocalActivity
import org.philblandford.ui.base.compose.VMView
import org.philblandford.ui.edit.compose.EditFrame
import org.philblandford.ui.edit.items.text.viewmodel.TextEditInterface
import org.philblandford.ui.edit.items.text.viewmodel.TextEditViewModel
import org.philblandford.ui.edit.model.EditModel
import org.philblandford.ui.edit.viewmodel.EditInterface
import org.philblandford.ui.util.*
import timber.log.Timber

@Composable
fun TextEdit(scale: Float) {
  Timber.e("RECO TextEdit $scale")
  VMView(TextEditViewModel::class.java) { model, iface, _ ->
    EditFrame(iface, scale = scale) {
      TextEditInternal(model, (iface as TextEditInterface))
    }
  }
}

@Composable
private fun TextEditInternal(model: EditModel, iface: TextEditInterface) {

  val activity = LocalActivity.current

  LaunchedEffect(Unit) {
    activity?.window?.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_NOTHING)
  }

  DisposableEffect(Unit) {
    onDispose {
      activity?.window?.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN)
    }
  }

  var text by remember {
    mutableStateOf(
      model.editItem.event.getParam<String>(EventParam.TEXT) ?: ""
    )
  }

  Column {

    OutlinedTextField(
      text,
      onValueChange = {
        text = it
        iface.updateParam(EventParam.TEXT, text)
      },
      colors = TextFieldDefaults.textFieldColors(
        backgroundColor = MaterialTheme.colors.onSurface,
        textColor = MaterialTheme.colors.surface,

        cursorColor = MaterialTheme.colors.surface
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
private fun Item(content: @Composable() () -> Unit) {
  Box(Modifier.padding(10.dp)) {
    content()
  }
}