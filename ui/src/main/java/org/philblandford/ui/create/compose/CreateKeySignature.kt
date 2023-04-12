package org.philblandford.ui.create.compose

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment.Companion.CenterHorizontally
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.philblandford.kscore.api.NewScoreDescriptor
import org.philblandford.ui.R
import org.philblandford.ui.create.viewmodel.CreateInterface
import org.philblandford.ui.create.viewmodel.CreateModel
import org.philblandford.ui.theme.PopupTheme
import org.philblandford.ui.util.KeySelector

@Composable
internal fun CreateKeyTimeSignature(
  model: CreateModel,
  next: () -> Unit,
  cancel:() -> Unit,
  iface:CreateInterface
) {

  WizardFrame(R.string.create_score_key_time_signature, next, cancel) {
    Column(Modifier.fillMaxSize(), verticalArrangement = Arrangement.SpaceEvenly) {
      KeySelector(
        model.newScoreDescriptor.keySignature, iface::setKeySignature,
        modifier = Modifier.align(CenterHorizontally)
      )
      TimeSelector(Modifier.align(CenterHorizontally), model.newScoreDescriptor.timeSignature) {
        iface.setTimeSignature { it }
      }
    }
  }
}

@Composable
@Preview
private fun Preview() {
  PopupTheme {
    Box(Modifier.fillMaxSize()) {
      CreateKeyTimeSignature(CreateModel(NewScoreDescriptor(), listOf()), {}, {}, StubCreateInterface())
    }
  }
}


