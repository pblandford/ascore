package org.philblandford.ui.edit.compose

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import org.philblandford.ui.base.compose.VMView
import org.philblandford.ui.edit.entities.ButtonActions
import org.philblandford.ui.edit.model.EditModel
import org.philblandford.ui.edit.viewmodel.EditInterface
import org.philblandford.ui.edit.viewmodel.EditViewModel
import org.philblandford.ui.util.IdRow

@Composable
fun <T> RowEdit(
  ids: List<Pair<Int, T>>,
  scale: Float,
  rows: Int = 2,
  actions: List<ButtonActions> = ButtonActions.values().toList(),
  extraContent: @Composable (EditModel, EditInterface) -> Unit = { _, _ ->}
) {

  VMView(EditViewModel::class.java) { model, iface, _ ->
    Row {
      EditFrame(iface, actions, scale) {
        IdRow(
          Modifier
            .background(MaterialTheme.colors.surface),
          ids,
          rows = rows,
          selected = -1, onSelect = {
            iface.setType(ids[it].second)
          })
      }
      extraContent(model, iface)
    }
  }
}