package org.philblandford.ui.save.compose

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.Button
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import org.philblandford.ui.R
import org.philblandford.ui.base.compose.VMView
import org.philblandford.ui.save.viewmodel.SaveInterface
import org.philblandford.ui.save.viewmodel.SaveModel
import org.philblandford.ui.save.viewmodel.SaveViewModel
import org.philblandford.ui.util.Gap
import org.philblandford.ui.util.LabelText
import org.philblandford.ui.util.OutlinedTextField2
import timber.log.Timber

@Composable
fun SaveScore(dismiss: () -> Unit) {
  VMView(SaveViewModel::class.java) { state, iface, _ ->
    SaveFileInternal(state, iface, dismiss)
  }
}

@Composable
private fun SaveFileInternal(model: SaveModel, iface: SaveInterface, dismiss: () -> Unit) {

  val title = remember { mutableStateOf(model.scoreTitle) }
  Column(Modifier.fillMaxWidth(0.9f),
    horizontalAlignment = Alignment.CenterHorizontally) {
    LabelText(stringResource(R.string.save_score_title))
    Gap(0.5f)
    OutlinedTextField2(title.value, {
      Timber.e("TITLE ${title.value}")
      title.value = it })
    Gap(0.5f)
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
      Button(onClick = {
        iface.saveInternal(title.value)
        dismiss()
      }) {
        Text(stringResource(R.string.save_score_save))
      }
      Button(onClick = { dismiss() }) {
        Text(stringResource(R.string.cancel))
      }
    }
  }
}

@Composable
@Preview
private fun Preview() {
  SaveFileInternal(SaveModel(""), object : SaveInterface {
    override fun reset() {
      TODO("Not yet implemented")
    }

    override fun saveInternal(name: String) {
      TODO("Not yet implemented")
    }
  }) {

  }
}