package org.philblandford.ui.edit.compose

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.philblandford.kscore.engine.types.EventParam
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
  typeParam:EventParam = EventParam.TYPE,
  actions: List<ButtonActions> = ButtonActions.values().toList(),
  extraContent: @Composable (EditModel, EditInterface) -> Unit = { _, _ ->}
) {

  VMView(EditViewModel::class.java) { model, iface, _ ->

    LaunchedEffect(typeParam) {
      iface.setTypeParam(typeParam)
    }
    Row {
      EditFrame(iface, actions, scale) {
        IdRow(
          Modifier
            .background(MaterialTheme.colorScheme.surface),
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