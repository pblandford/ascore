package org.philblandford.ui.settings.compose

import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.scrollable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.constraintlayout.compose.Dimension
import com.philblandford.kscore.api.Instrument
import com.philblandford.kscore.api.InstrumentGroup
import com.philblandford.kscore.log.ksLogt
import org.philblandford.ui.R
import org.philblandford.ui.base.compose.VMView
import org.philblandford.ui.common.block
import org.philblandford.ui.settings.model.InstrumentManageModel
import org.philblandford.ui.settings.viewmodel.InstrumentManageInterface
import org.philblandford.ui.settings.viewmodel.InstrumentManageViewModel
import org.philblandford.ui.theme.DialogTheme
import org.philblandford.ui.util.Gap
import org.philblandford.ui.util.TextSpinner
import timber.log.Timber

@Composable
fun InstrumentManage() {
  VMView(InstrumentManageViewModel::class.java) { model, iface, _ ->
    InstrumentManageInternal(model, iface)
  }
}

@Composable
private fun InstrumentManageInternal(
  model: InstrumentManageModel,
  iface: InstrumentManageInterface
) {
  DialogTheme { modifier ->

    Box(modifier) {
      InstrumentEditPopup(model, iface)

      ConstraintLayout(
        Modifier
          .fillMaxSize()
          .padding(horizontal = 10.dp)
      ) {
        val (column, button) = createRefs()
        Box(
          Modifier
            .constrainAs(column) {
              width = Dimension.fillToConstraints
              this.height = Dimension.fillToConstraints
              top.linkTo(parent.top)
              bottom.linkTo(button.top)
              start.linkTo(parent.start)
              end.linkTo(parent.end)
            }) {
          LazyColumn(Modifier.scrollable(rememberScrollState(), Orientation.Vertical)) {
            items(model.groups) {
                InstrumentGroupItem(it, iface)
            }
          }
        }

        Button(
          onClick = { iface.clear() },
          modifier = Modifier.constrainAs(button) {
            centerHorizontallyTo(parent)
            bottom.linkTo(parent.bottom)
          }
        ) {
          Text(stringResource(R.string.clear_assignments))
        }
      }
    }
  }
}

@Composable
private fun InstrumentGroupItem(
  instrumentGroup: InstrumentGroup, iface: InstrumentManageInterface
) {
  val showInstruments = remember { mutableStateOf(false) }
  Text(instrumentGroup.name, Modifier.clickable(onClick = {
    showInstruments.value = !showInstruments.value
  }))
  if (showInstruments.value) {
    InstrumentList(instrumentGroup.instruments, iface)
  }
}

@Composable
private fun InstrumentList(
  instruments: List<Instrument>, iface: InstrumentManageInterface
) {
  Column(Modifier.offset(block())) {
    instruments.forEach { instrument ->
      Text(instrument.name, Modifier.clickable(onClick = {
        ksLogt("clicked $instrument")
        iface.setSelectedInstrument(instrument)
      }))
    }
  }
}

@Composable
private fun InstrumentEditPopup(
  model: InstrumentManageModel,
  iface: InstrumentManageInterface
) {
  model.selectedInstrument?.let { instrument ->
    Dialog({ iface.setSelectedInstrument(null) }) {
      DialogTheme { modifier ->
        InstrumentEditPanel(instrument, modifier, model, iface)
      }
    }
  }
}

@Composable
private fun InstrumentEditPanel(
  instrument: Instrument, modifier: Modifier,
  model: InstrumentManageModel, iface: InstrumentManageInterface
) {
  Box(modifier.border(1.dp, MaterialTheme.colorScheme.onSurface)) {
    Column(Modifier.padding(10.dp)) {
      Text(instrument.name)
      Gap(0.5f)
      Row {
        Text(
          "${stringResource(R.string.assign_instrument)}: ",
        )
        TextSpinner(
          strings = model.groups.map { it.name },
          selected = { instrument.group },
          onSelect = { iface.assignInstrument(instrument.name, model.groups[it].name) }
        )
      }
    }
  }
}