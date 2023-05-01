package org.philblandford.ui.save.compose

import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import com.philblandford.kscore.engine.types.FileSource
import kotlinx.coroutines.flow.Flow
import org.philblandford.ui.R
import org.philblandford.ui.base.compose.VMView
import org.philblandford.ui.base.viewmodel.VMSideEffect
import org.philblandford.ui.save.viewmodel.SaveInterface
import org.philblandford.ui.save.viewmodel.SaveModel
import org.philblandford.ui.save.viewmodel.SaveViewModel
import org.philblandford.ui.theme.DialogTheme
import org.philblandford.ui.util.Gap
import org.philblandford.ui.util.LabelText
import org.philblandford.ui.util.OutlinedTextField2
import timber.log.Timber

@Composable
fun SaveScore(dismiss: () -> Unit) {
  VMView(SaveViewModel::class.java) { state, iface, _ ->
    DialogTheme {
      SaveFileInternal(it, state, iface, dismiss)
    }
  }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SaveFileInternal(modifier: Modifier, model: SaveModel, iface: SaveInterface, dismiss: () -> Unit) {

  val title = remember { mutableStateOf(model.scoreTitle) }
  Column(
    modifier
      .fillMaxWidth(0.9f)
      .wrapContentHeight(),
    horizontalAlignment = Alignment.CenterHorizontally) {
    LabelText(stringResource(R.string.save_score_title))
    Gap(0.5f)
    OutlinedTextField(title.value, {
      Timber.e("TITLE ${title.value}")
      title.value = it }, Modifier.wrapContentHeight())
    Gap(0.5f)
    SourceSelect(model.source, iface::setFileSource)
    Gap(0.5f)
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
      Button(onClick = {
        iface.saveInternal(title.value)
        dismiss()
      }, enabled = title.value.isNotEmpty()) {
        Text(stringResource(R.string.save_score_save))
      }
      Button(onClick = { dismiss() }) {
        Text(stringResource(R.string.cancel))
      }
    }
  }
}

@Composable
private fun SourceSelect(current:FileSource, set:(FileSource)->Unit) {

  Column(Modifier.fillMaxWidth(), horizontalAlignment = Alignment.Start) {
    Row(verticalAlignment = Alignment.CenterVertically){
      RadioButton(
        current == FileSource.SAVE, onClick = { set(FileSource.SAVE) },
        colors = RadioButtonDefaults.colors(selectedColor = MaterialTheme.colorScheme.onSurface)
      )
      Text(stringResource(R.string.files_in_storage))
    }
    Row(verticalAlignment = Alignment.CenterVertically){
      RadioButton(
        current == FileSource.EXTERNAL, onClick = { set(FileSource.EXTERNAL) },
        colors = RadioButtonDefaults.colors(selectedColor = MaterialTheme.colorScheme.onSurface)
      )
      Text(stringResource(R.string.files_in_external_storage))
    }
    Row(verticalAlignment = Alignment.CenterVertically) {
      RadioButton(
        current == FileSource.TEMPLATE, onClick = { set(FileSource.TEMPLATE) },
        colors = RadioButtonDefaults.colors(selectedColor = MaterialTheme.colorScheme.onSurface)
      )
      Text(stringResource(R.string.set_template))
    }
  }
}

@Composable
@Preview
private fun Preview() {
  DialogTheme {

    SaveFileInternal(it, SaveModel("", FileSource.SAVE), object : SaveInterface {
      override fun reset() {
        TODO("Not yet implemented")
      }

      override fun saveInternal(name: String) {
        TODO("Not yet implemented")
      }

      override fun getSideEffects(): Flow<VMSideEffect> {
        TODO("Not yet implemented")
      }

      override fun setFileSource(source: FileSource) {
        TODO("Not yet implemented")
      }
    }) {

    }
  }
}