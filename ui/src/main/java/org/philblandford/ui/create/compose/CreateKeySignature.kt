package org.philblandford.ui.create.compose

import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment.Companion.CenterHorizontally
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
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
  cancel: () -> Unit,
  iface: CreateInterface
) {

  WizardFrame(R.string.create_score_key_signature, next, cancel) {
    Column(Modifier.fillMaxSize(), verticalArrangement = Arrangement.SpaceEvenly) {
      KeySelector(
        model.newScoreDescriptor.keySignature, iface::setKeySignature,
        modifier = Modifier.align(CenterHorizontally)
      )
      Text(stringResource(R.string.create_score_time_signature), Modifier.padding(vertical = 5.dp),
        style = MaterialTheme.typography.bodyLarge)
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
      CreateKeyTimeSignature(
        CreateModel(NewScoreDescriptor(), listOf()),
        {},
        {},
        StubCreateInterface()
      )
    }
  }
}


