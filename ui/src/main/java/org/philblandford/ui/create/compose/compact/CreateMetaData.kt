package org.philblandford.ui.create.compose.compact

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.tooling.preview.Preview
import com.philblandford.kscore.api.Instrument
import com.philblandford.kscore.api.NewScoreDescriptor
import com.philblandford.kscore.engine.tempo.Tempo
import com.philblandford.kscore.engine.time.TimeSignature
import com.philblandford.kscore.engine.types.MetaType
import com.philblandford.kscore.engine.types.PageSize
import kotlinx.coroutines.flow.Flow
import org.philblandford.ui.R
import org.philblandford.ui.base.viewmodel.VMSideEffect
import org.philblandford.ui.common.block
import org.philblandford.ui.create.compose.WizardFrame
import org.philblandford.ui.create.viewmodel.CreateInterface
import org.philblandford.ui.create.viewmodel.CreateModel
import org.philblandford.ui.main.window.LocalWindowSizeClass
import org.philblandford.ui.main.window.expanded
import org.philblandford.ui.theme.compose.PopupTheme

@Composable
internal fun CreateMetaData(
  model: CreateModel,
  next: () -> Unit,
  cancel: () -> Unit,
  iface: CreateInterface
) {

  WizardFrame(R.string.create_score_meta_data, next, cancel) {

    Box(
      Modifier
        .fillMaxSize()
    ) {
      Column(Modifier.align(Alignment.Center)) {
        TextLine(
          model.text(MetaType.TITLE),
          iface::setTitle,
          R.string.title,
          next
        )
        TextLine(
          model.text(MetaType.SUBTITLE),
          iface::setSubtitle,
          R.string.subtitle,
          next
        )
        TextLine(
          model.text(MetaType.COMPOSER),
          iface::setComposer,
          R.string.composer,
          next
        )
        TextLine(
          model.text(MetaType.LYRICIST),
          iface::setLyricist,
          R.string.lyricist,
          next
        )
      }
    }
  }
}

private fun CreateModel.text(metaType: MetaType): String =
  newScoreDescriptor.meta.getSection(metaType).text


@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TextLine(
  value: String, cmd: (String) -> Unit, resource: Int,
  next: () -> Unit,
) {
  val height = if (LocalWindowSizeClass.current.expanded()) 1.5f else 2f
  OutlinedTextField(
    value = value, onValueChange = { cmd(it) },
    modifier = Modifier.size(block(9), block(height)),
    label = { Text(stringResource(id = resource)) },
    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
    keyboardActions = KeyboardActions(
      onNext = { next() },
    ),
    colors = TextFieldDefaults.colors(),
    textStyle = MaterialTheme.typography.bodyMedium
  )
}

@Composable
@Preview
private fun Preview() {
  PopupTheme {
    CreateMetaData(CreateModel(NewScoreDescriptor(), listOf()), {}, {}, StubCreateInterface())
  }
}

internal class StubCreateInterface : CreateInterface {


  override fun reset() {
    TODO("Not yet implemented")
  }

  override fun setTitle(title: String) {
    TODO("Not yet implemented")
  }

  override fun setSubtitle(subtitle: String) {
    TODO("Not yet implemented")
  }

  override fun setComposer(composer: String) {
    TODO("Not yet implemented")
  }

  override fun setLyricist(lyricist: String) {
    TODO("Not yet implemented")
  }

  override fun setKeySignature(key: Int) {
    TODO("Not yet implemented")
  }

  override fun setTimeSignature(func: TimeSignature.() -> TimeSignature) {
  }

  override fun setUpbeatBar(func: TimeSignature.() -> TimeSignature) {

  }

  override fun setUpbeatEnabled(enabled: Boolean) {
    TODO("Not yet implemented")
  }

  override fun setTempo(func: Tempo.() -> Tempo) {
    TODO("Not yet implemented")
  }

  override fun addInstrument(instrument: Instrument) {
  }

  override fun removeInstrument(instrument: Instrument) {
  }

  override fun setPageSize(pageSize: PageSize) {
    TODO("Not yet implemented")
  }

  override fun reorderInstruments(oldIndex: Int, newIndex: Int) {
    TODO("Not yet implemented")
  }

  override fun setNumBars(bars: Int) {
    TODO("Not yet implemented")
  }

  override fun create() {
    TODO("Not yet implemented")
  }

  override fun getSideEffects(): Flow<VMSideEffect> {
    TODO("Not yet implemented")
  }

  override fun updateInstrument(idx: Int, instrument: Instrument) {
    TODO("Not yet implemented")
  }
}
