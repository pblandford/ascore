package org.philblandford.ui.edit.compose

import androidx.compose.runtime.Composable
import org.philblandford.ui.base.compose.VMView
import org.philblandford.ui.edit.entities.ButtonActions
import org.philblandford.ui.edit.viewmodel.EditViewModel

@Composable
fun DefaultEdit(scale:Float, actions:List<ButtonActions> = ButtonActions.values().toList()) {
  VMView(EditViewModel::class.java) { model, iface, _ ->
    EditFrame(iface, scale = scale, actions = actions) {

    }
  }
}