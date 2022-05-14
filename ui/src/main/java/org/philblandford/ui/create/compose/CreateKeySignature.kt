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
import org.philblandford.ui.util.KeySelector

@Composable
internal fun CreateKeySignature(
  model: CreateModel,
  next: () -> Unit,
  cancel:() -> Unit,
  iface:CreateInterface
) {

  CreateFrame(R.string.create_score_key_signature, next, cancel) {

    KeySelector(model.newScoreDescriptor.keySignature, {})
  }
}

@Composable
@Preview
private fun Preview() {
  PopupTheme {
    Box(Modifier.fillMaxSize()) {
      CreateKeySignature(CreateModel(NewScoreDescriptor()), {}, {}, stubCreateIface)
    }
  }
}


