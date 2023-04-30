package org.philblandford.ui.create.compose.compact

import androidx.compose.foundation.layout.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.philblandford.kscore.api.NewScoreDescriptor
import org.philblandford.ui.R
import org.philblandford.ui.create.compose.WizardFrame
import org.philblandford.ui.create.viewmodel.CreateInterface
import org.philblandford.ui.create.viewmodel.CreateModel
import org.philblandford.ui.theme.compose.PopupTheme

@Composable
internal fun CreateTimeSignature(
  model: CreateModel,
  next: () -> Unit,
  cancel: () -> Unit,
  iface: CreateInterface
) {

  WizardFrame(R.string.create_score_time_signature, next, cancel) {


    }
}

@Composable
@Preview
private fun Preview() {
  PopupTheme {
    Box(Modifier.fillMaxSize()) {
      CreateTimeSignature(CreateModel(NewScoreDescriptor()), {}, {}, StubCreateInterface())
    }
  }
}


