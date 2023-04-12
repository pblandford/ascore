package org.philblandford.ui.create.compose

import androidx.compose.foundation.layout.*
import androidx.compose.material.Checkbox
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.philblandford.kscore.api.NewScoreDescriptor
import org.philblandford.ui.R
import org.philblandford.ui.create.viewmodel.CreateInterface
import org.philblandford.ui.create.viewmodel.CreateModel
import org.philblandford.ui.theme.PopupTheme
import org.philblandford.ui.util.CustomTimeSelector
import org.philblandford.ui.util.DimmableBox
import org.philblandford.ui.util.TempoSelector

@Composable
internal fun CreateTempo(
  model: CreateModel,
  next: () -> Unit,
  cancel: () -> Unit,
  iface: CreateInterface
) {

  WizardFrame(R.string.create_score_tempo, next, cancel) {
    Column(Modifier.fillMaxSize()) {
      with(model.newScoreDescriptor) {
        TempoSelector(tempo) { iface.setTempo { it } }
        Text(stringResource(R.string.upbeatbar), Modifier.padding(vertical = 5.dp))
        Row {
          DimmableBox(!upbeatEnabled, Modifier.wrapContentWidth()) {
            CustomTimeSelector(timeSignature, { iface.setUpbeatBar { it } }, upbeatEnabled)
          }
          Checkbox(upbeatEnabled, { iface.setUpbeatEnabled(it) })
        }
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


