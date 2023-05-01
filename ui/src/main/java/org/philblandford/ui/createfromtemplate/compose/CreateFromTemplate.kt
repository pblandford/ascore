package org.philblandford.ui.createfromtemplate.compose

import FileInfo
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import org.philblandford.ui.R
import org.philblandford.ui.base.compose.VMView
import org.philblandford.ui.createfromtemplate.model.CreateFromTemplateModel
import org.philblandford.ui.createfromtemplate.viewmodel.CreateFromTemplateInterface
import org.philblandford.ui.createfromtemplate.viewmodel.CreateFromTemplateViewModel
import org.philblandford.ui.load.compose.DeleteConfirmDialog
import org.philblandford.ui.load.compose.ScoreDetailCard
import org.philblandford.ui.theme.DialogTheme
import org.philblandford.ui.util.Gap
import org.philblandford.ui.util.LabelText

@Composable
fun CreateFromTemplate(dismiss: () -> Unit) {
  VMView(CreateFromTemplateViewModel::class.java) { state, iface, _ ->
    CreateFromTemplateInternal(state, iface, dismiss)
  }
}

@Composable
private fun CreateFromTemplateInternal(
  state: CreateFromTemplateModel,
  iface: CreateFromTemplateInterface,
  dismiss: () -> Unit
) {
  DialogTheme { modifier ->

    var showConfirm by remember { mutableStateOf<FileInfo?>(null) }

    showConfirm?.let { fileInfo ->
      DeleteConfirmDialog(fileInfo, action = {
        iface.delete(fileInfo)
        showConfirm = null
      }) {
        showConfirm = null
      }
    }


    Column(
      modifier
        .fillMaxWidth()
        .fillMaxHeight(0.7f),
      horizontalAlignment = Alignment.CenterHorizontally
    ) {
      LabelText(stringResource(R.string.new_score_template))
      Gap(0.5f)
      LazyColumn {
        items(state.templates) {
          ScoreDetailCard(it, {
            iface.create(it.name)
            dismiss()
          }) {
            showConfirm = it
          }
          Gap(0.2f)
        }
      }
    }
  }
}