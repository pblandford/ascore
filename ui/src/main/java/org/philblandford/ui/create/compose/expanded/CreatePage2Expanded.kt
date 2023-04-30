package org.philblandford.ui.create.compose.expanded

import androidx.compose.foundation.layout.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Alignment.Companion.CenterHorizontally
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.philblandford.kscore.api.NewScoreDescriptor
import org.philblandford.ui.R
import org.philblandford.ui.create.compose.TimeSelector
import org.philblandford.ui.create.compose.WizardFrame
import org.philblandford.ui.create.compose.compact.BarsRow
import org.philblandford.ui.create.compose.compact.Label
import org.philblandford.ui.create.compose.compact.PageSizeRow
import org.philblandford.ui.create.compose.compact.StubCreateInterface
import org.philblandford.ui.create.compose.compact.UpbeatRow
import org.philblandford.ui.create.viewmodel.CreateInterface
import org.philblandford.ui.create.viewmodel.CreateModel
import org.philblandford.ui.theme.compose.PopupTheme
import org.philblandford.ui.util.Gap
import org.philblandford.ui.util.KeySelector
import org.philblandford.ui.util.TempoSelector

@Composable
internal fun CreatePage2Expanded(
  model: CreateModel,
  next: () -> Unit,
  cancel: () -> Unit,
  iface: CreateInterface
) {

  WizardFrame(R.string.create_score_key_signature, next, cancel) {
    with(model.newScoreDescriptor) {
      Column(Modifier.fillMaxSize(), verticalArrangement = Arrangement.SpaceEvenly) {
        KeySelector(keySignature, iface::setKeySignature, modifier = Modifier.align(CenterHorizontally))
        Label(R.string.create_score_time_signature)
        TimeSelector(Modifier.align(CenterHorizontally), timeSignature, column = false) {
          iface.setTimeSignature { it }
        }
        Label(R.string.create_score_tempo)
        TempoSelector(tempo) { iface.setTempo { it } }
        Gap(0.5f)
        Label(R.string.upbeatbar)
        UpbeatRow(upbeatEnabled, iface::setUpbeatEnabled, upBeat) { iface.setUpbeatBar { it } }
        Gap(0.5f)
        Row(
          Modifier.wrapContentWidth(),
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.Start
        ) {
          Column {
            Label(R.string.page_size)
            PageSizeRow(pageSize) { iface.setPageSize(it) }
          }
          Gap(1f)
          Column {
            Label(R.string.num_bars)
            BarsRow(numBars) { iface.setNumBars(it) }
          }
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
      CreatePage2Expanded(
        CreateModel(NewScoreDescriptor(), listOf()),
        {},
        {},
        StubCreateInterface()
      )
    }
  }
}


