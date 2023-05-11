package org.philblandford.ui.edit.items.instrumentedit.compose

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Popup
import com.philblandford.kscore.api.Instrument
import com.philblandford.kscore.api.InstrumentGroup
import com.philblandford.kscore.api.Rectangle
import com.philblandford.kscore.api.defaultInstrument
import com.philblandford.kscore.api.instrument
import com.philblandford.kscore.engine.types.EventParam
import com.philblandford.kscore.engine.types.ea
import kotlinx.coroutines.flow.MutableStateFlow
import org.philblandford.ascore2.features.ui.model.EditItem
import org.philblandford.ui.R
import org.philblandford.ui.base.compose.VMView
import org.philblandford.ui.create.compose.ClefRow
import org.philblandford.ui.edit.items.instrumentedit.viewmodel.InstrumentEditInterface
import org.philblandford.ui.edit.items.instrumentedit.viewmodel.InstrumentEditViewModel
import org.philblandford.ui.edit.model.EditModel
import org.philblandford.ui.stubs.StubInstrumentEditInterface
import org.philblandford.ui.theme.compose.AscoreTheme
import org.philblandford.ui.util.Gap
import org.philblandford.ui.util.InstrumentList
import org.philblandford.ui.util.NumberSelector
import timber.log.Timber

@Composable
fun InstrumentEdit() {
  VMView(InstrumentEditViewModel::class.java) { model, iface, _ ->
    InstrumentEditInternal(model, iface as InstrumentEditInterface)
  }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun InstrumentEditInternal(editModel: EditModel, iface: InstrumentEditInterface) {
  var instrumentLabel by remember {
    mutableStateOf(
      editModel.editItem.event.getParam<String>(
        EventParam.LABEL
      ) ?: ""
    )
  }
  var abbreviation by remember {
    mutableStateOf(
      editModel.editItem.event.getParam<String>(EventParam.ABBREVIATION) ?: ""
    )
  }
  val instrument by iface.selectedInstrument().collectAsState(null)
  var showPopup by remember { mutableStateOf(false) }

  LaunchedEffect(instrument) {
    instrumentLabel = instrument?.label ?: ""
    abbreviation = instrument?.abbreviation ?: ""
  }

  Column(
    Modifier
      .width(300.dp)
      .padding(10.dp)
  ) {
    OutlinedTextField(
      instrumentLabel, { label ->
        instrumentLabel = label;
        instrument?.let {
          iface.setInstrument(it.copy(label = label))
        }
      },
      Modifier.fillMaxWidth(),
      label = { Text(stringResource(R.string.label)) },
      colors = TextFieldDefaults.outlinedTextFieldColors(unfocusedLabelColor = MaterialTheme.colorScheme.onSurface)
    )
    Gap(0.5f)
    OutlinedTextField(
      abbreviation, { abr ->
        abbreviation = abr;
        instrument?.let {
          iface.setInstrument(it.copy(abbreviation = abr))
        }
      }, Modifier.fillMaxWidth(),
      label = { Text(stringResource(R.string.abbreviation)) },
      colors = TextFieldDefaults.outlinedTextFieldColors(unfocusedLabelColor = MaterialTheme.colorScheme.onSurface)
    )
    Gap(0.5f)
    Text("${stringResource(R.string.instrument)}: ${instrument?.name}",
      Modifier.clickable { showPopup = true }, fontSize = 16.sp)
    Gap(0.5f)

    if (showPopup) {
      Popup {
        InstrumentList(
          Modifier
            .height(200.dp)
            .background(MaterialTheme.colorScheme.surface)
            .fillMaxWidth(), iface.instruments(), instrument
        ) { iface.setInstrument(it); showPopup = false }
      }
    }
    if (instrument?.percussion != true) {
      instrument?.let { instrument ->
        Row(verticalAlignment = Alignment.CenterVertically) {
          Text("${stringResource(R.string.transposition)}: ", fontSize = 16.sp)
          NumberSelector(
            -12,
            12,
            instrument.transposition,
            { iface.setInstrument(instrument.copy(transposition = it)) })
        }
        Gap(0.3f)
        ClefRow(instrument) { iface.setInstrument(it) }
      }
    }
  }
}

@Composable
@Preview
private fun Preview() {

  var instrument by remember { mutableStateOf(defaultInstrument()) }
  var event by remember { mutableStateOf(instrument.toEvent()) }
  val iface = object : InstrumentEditInterface by StubInstrumentEditInterface() {
    override fun instruments(): List<InstrumentGroup> {
      return (0..10).map { group ->
        val instruments = (0..10).map {
          defaultInstrument().copy(
            label = "instrument $it",
            name = "instrument $it"
          )
        }
        InstrumentGroup("group $group", instruments)
      }
    }

    override fun setInstrument(i: Instrument) {
      instrument = i
      event = instrument.toEvent()
    }

    override fun selectedInstrument() = MutableStateFlow(instrument)
  }

  AscoreTheme {
    InstrumentEditInternal(
      EditModel(EditItem(event, ea(1), 1, Rectangle(0, 0, 0, 0))),
      iface
    )
  }
}