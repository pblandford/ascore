package org.philblandford.ui.create.compose

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.OutlinedTextField
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.tooling.preview.Preview
import com.philblandford.kscore.api.Instrument
import com.philblandford.kscore.api.InstrumentGroup
import com.philblandford.kscore.api.NewScoreDescriptor
import com.philblandford.kscore.engine.time.TimeSignature
import com.philblandford.kscore.engine.types.MetaType
import org.philblandford.ui.common.block
import org.philblandford.ui.create.viewmodel.CreateInterface
import org.philblandford.ui.create.viewmodel.CreateModel
import org.philblandford.ui.R
import org.philblandford.ui.theme.PopupTheme

@Composable
internal fun CreateMetaData(
  model: CreateModel,
  next: () -> Unit,
  cancel: () -> Unit,
  iface: CreateInterface
) {

  CreateFrame(R.string.create_score_meta_data, next, cancel) {

    Box(
      Modifier
        .fillMaxWidth()
        .wrapContentHeight()
    ) {
      Column {
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


@Composable
private fun TextLine(
  value: String, cmd: (String) -> Unit, resource: Int,
  next: () -> Unit,
) {
  OutlinedTextField(
    value = value, onValueChange = { cmd(it) },
    modifier = Modifier.size(block(9), block(2)),
    label = { Text(stringResource(id = resource)) },
    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
    keyboardActions = KeyboardActions(
      onNext = { next() }
    )
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

  override fun addInstrument(instrument: Instrument) {
  }

  override fun removeInstrument(instrument: Instrument) {
  }

  override fun reorderInstruments(oldIndex: Int, newIndex: Int) {
    TODO("Not yet implemented")
  }

  override fun create() {
    TODO("Not yet implemented")
  }
}
