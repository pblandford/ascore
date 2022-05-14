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
import com.philblandford.kscore.api.NewScoreDescriptor
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
  cancel:() -> Unit,
  iface:CreateInterface
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
          { },
          R.string.subtitle,
          next
        )
        TextLine(
          model.text(MetaType.COMPOSER),
          { },
          R.string.composer,
          next
        )
        TextLine(
          model.text(MetaType.LYRICIST),
          { },
          R.string.lyricist,
          next
        )
      }
    }
  }
}

private fun CreateModel.text(metaType: MetaType):String = newScoreDescriptor.meta.getSection(metaType).text


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
    CreateMetaData(CreateModel(NewScoreDescriptor()), {}, {}, stubCreateIface)
  }
}

internal val stubCreateIface = object : CreateInterface {
  override fun reset() {
    TODO("Not yet implemented")
  }

  override fun setTitle(title: String) {
    TODO("Not yet implemented")
  }
}
