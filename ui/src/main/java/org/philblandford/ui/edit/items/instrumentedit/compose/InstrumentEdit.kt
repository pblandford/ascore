package org.philblandford.ui.edit.items.instrumentedit.compose

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.OutlinedTextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.philblandford.kscore.api.Instrument
import com.philblandford.kscore.api.InstrumentGroup
import com.philblandford.kscore.api.Rectangle
import com.philblandford.kscore.api.defaultInstrument
import com.philblandford.kscore.engine.types.EventParam
import com.philblandford.kscore.engine.types.ea
import org.philblandford.ascore2.features.ui.model.EditItem
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

@Composable
fun InstrumentEdit() {
  VMView(InstrumentEditViewModel::class.java) { model, iface, _ ->
    InstrumentEditInternal(model, iface as InstrumentEditInterface)
  }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun InstrumentEditInternal(editModel: EditModel, iface:InstrumentEditInterface) {
  var instrumentLabel by remember{ mutableStateOf(editModel.editItem.event.getParam<String>(EventParam.LABEL) ?: "") }
  var abbreviation by remember{ mutableStateOf(editModel.editItem.event.getParam<String>(EventParam.ABBREVIATION) ?: "") }

  LaunchedEffect(iface.selectedInstrument()?.name) {
    instrumentLabel = iface.selectedInstrument()?.label ?: ""
  }

  Column(Modifier.width(300.dp)) {
    OutlinedTextField(instrumentLabel, { label -> instrumentLabel = label;
      iface.selectedInstrument()?.let {
        iface.setInstrument(it.copy(label = label))
      }
    }, Modifier.fillMaxWidth())
    OutlinedTextField(abbreviation, { abr -> abbreviation = abr;
      iface.selectedInstrument()?.let {
        iface.setInstrument(it.copy(abbreviation = abr))
      }
    }, Modifier.fillMaxWidth())
    InstrumentList(
      Modifier
        .height(200.dp)
        .fillMaxWidth(), iface.instruments(), iface.selectedInstrument()) { iface.setInstrument(it) }
    iface.selectedInstrument()?.let { instrument ->
      NumberSelector(-12, 12, instrument.transposition, { iface.setInstrument(instrument.copy(transposition = it)) })
      Gap(0.3f)
      ClefRow(instrument) { iface.setInstrument(it) }
    }
  }
}

@Composable
@Preview
private fun Preview() {

  var instrument by remember{ mutableStateOf(defaultInstrument()) }
  var event by remember { mutableStateOf(instrument.toEvent()) }
  val iface = object : InstrumentEditInterface by StubInstrumentEditInterface() {
    override fun instruments(): List<InstrumentGroup> {
      return (0..10).map { group ->
        val instruments = (0..10).map { defaultInstrument().copy(label = "instrument $it", name = "instrument $it") }
        InstrumentGroup("group $group", instruments)
      }
    }

    override fun setInstrument(i: Instrument) {
      instrument = i
      event = instrument.toEvent()
    }

    override fun selectedInstrument(): Instrument? {
      return instrument
    }
  }

  AscoreTheme {
    InstrumentEditInternal(EditModel(EditItem(event, ea(1),1, Rectangle(0,0,0,0))),
      iface)
  }
}