package org.philblandford.ui.load.compose

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import org.philblandford.ui.R
import org.philblandford.ui.base.compose.VMView
import org.philblandford.ui.load.viewmodels.LoadInterface
import org.philblandford.ui.load.viewmodels.LoadModel
import org.philblandford.ui.load.viewmodels.LoadViewModel
import org.philblandford.ui.util.Gap
import org.philblandford.ui.util.LabelText

@Composable
fun LoadScore(dismiss: () -> Unit) {
  VMView(LoadViewModel::class.java) { state, iface, _ ->
    LoadScoreInternal(state, iface, dismiss)
  }
}

@Composable
private fun LoadScoreInternal(model: LoadModel, iface: LoadInterface, dismiss: () -> Unit) {
  Column(
    Modifier
      .fillMaxWidth(0.9f)
      .background(MaterialTheme.colors.background)
      .padding(5.dp)) {
    LabelText(stringResource(R.string.load_score_title))
    Gap(0.5f)
    if (model.fileNames.isEmpty()) {
      Box(
        Modifier
          .fillMaxWidth()
          .height(100.dp)) {
        Text(stringResource(R.string.load_score_no_files), Modifier.align(Alignment.Center),
        Color.LightGray)
      }
    } else {
      LazyColumn {
        items(model.fileNames) { fileName ->
          Text(fileName, Modifier.clickable {
            iface.load(fileName)
            dismiss()
          })
        }

      }
    }
  }
}