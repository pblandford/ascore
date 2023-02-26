package org.philblandford.ui.util

import android.content.res.Configuration
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.style.TextAlign
import org.philblandford.ui.common.block


@Composable
fun InstrumentSelector(
  groups: List<String>,
  group: String,
  setGroup: (Int) -> Unit,
  instruments: List<String>,
  instrument: String,
  setInstrument: (Int) -> Unit,
  up: Boolean? = null,
  setUp: (Boolean) -> Unit = {},
) {
  if (LocalConfiguration.current.orientation == Configuration.ORIENTATION_PORTRAIT) {
    InstrumentSelectorPortrait(
      groups,
      group,
      setGroup,
      instruments,
      instrument,
      setInstrument,
      up,
      setUp
    )
  } else {
    InstrumentSelectorLandscape(
      groups,
      group,
      setGroup,
      instruments,
      instrument,
      setInstrument,
      up,
      setUp
    )
  }
}

@Composable
fun InstrumentSelectorPortrait(
  groups: List<String>,
  group: String,
  setGroup: (Int) -> Unit,
  instruments: List<String>,
  instrument: String,
  setInstrument: (Int) -> Unit,
  up: Boolean? = null,
  setUp: (Boolean) -> Unit = {},
) {
  Row {
    Column(
      Modifier
        .width(block(6))
        .align(Alignment.CenterVertically)
    ) {
      GroupSpinner(groups, group, setGroup)
      Gap(block(0.5))
      InstrumentSpinner(instruments, instrument, setInstrument)
    }
    up?.let { up ->
      UpDownColumn(
        isUp = { up },
        set = { setUp(it) },
        modifier = Modifier.align(Alignment.CenterVertically)
      )
    }
  }
}

@Composable
fun InstrumentSelectorLandscape(
  groups: List<String>,
  group: String,
  setGroup: (Int) -> Unit,
  instruments: List<String>,
  instrument: String,
  setInstrument: (Int) -> Unit,
  up: Boolean? = null,
  setUp: (Boolean) -> Unit = {},
) {
  Row {
    GroupSpinner(groups, group, setGroup)
    Gap(block(0.5))
    InstrumentSpinner(instruments, instrument, setInstrument)
    Gap(block(0.5))
    up?.let { up ->
      UpDownRow(
        isUp = { up },
        set = { setUp(it) },
        modifier = Modifier.align(Alignment.CenterVertically)
      )
    }
  }
}

@Composable
private fun GroupSpinner(groups: List<String>, group: String, setGroup: (Int) -> Unit) {
  TextSpinner(strings = groups,
    selected = { group }, tag = "Group", modifier = Modifier.width(block(5)),
    textAlign = TextAlign.Start,
    onSelect = { setGroup(it) })
}

@Composable
private fun InstrumentSpinner(
  instruments: List<String>,
  instrument: String,
  setInstrument: (Int) -> Unit
) {
  TextSpinner(strings = instruments,
    selected = { instrument }, tag = "Instrument", modifier = Modifier.width(block(5)),
    textAlign = TextAlign.Start,
    onSelect = { setInstrument(it) })
}

