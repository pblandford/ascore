package org.philblandford.ui.util

import android.content.res.Configuration
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.material.Checkbox
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.philblandford.ascore.android.ui.style.disabledColor
import com.philblandford.kscore.api.Ks
import com.philblandford.kscore.engine.duration.Duration
import com.philblandford.kscore.engine.duration.dot
import com.philblandford.kscore.engine.duration.numDots
import com.philblandford.kscore.engine.duration.undot
import com.philblandford.kscore.engine.types.*
import com.philblandford.kscore.log.ksLogt
import com.philblandford.kscore.log.ksLogv
import org.philblandford.ui.common.block
import org.philblandford.ui.R
import kotlin.math.pow


@Composable
fun DurationSelector(
  getDuration: () -> Duration, setDuration: (Duration) -> Unit,
  size: Dp = block(),
  tag: () -> String = { "" }
) {
  val resId = durationIds.find { it.second == getDuration() }?.first ?: R.drawable.crotchet_rest

  Box(Modifier.border(styledBorder())) {
    Row() {
      SquareButton(
        R.drawable.arrow_left, tag = "ButtonLeft",
        size = size
      ) {
        val selection = durationToSelection(getDuration())
        ksLogv("ButtonLeft $selection")

        if (selection > 0) {
          setDuration(selectionToDuration(selection - 1))
        }
      }
      SquareButton(resId, tag = "Display ${getDuration()}", size = size) {}
      SquareButton(R.drawable.arrow_right, tag = "ButtonRight", size = size) {
        val selection = durationToSelection(getDuration())
        if (selection < durationIds.size - 1) {
          setDuration(selectionToDuration(selection + 1))
        }
      }
    }
  }
}

private fun selectionToDuration(selection: Int): Duration {
  return durationIds.get(selection).second
}

private fun durationToSelection(duration: Duration): Int {
  return durationIds.indexOfFirst { it.second == duration }
}

@Composable
fun DotToggle(
  numDots: Int, setDots: (Int) -> Unit
) {
  SquareButton(resource = if (numDots < 2) R.drawable.onedot else R.drawable.twodot,
    selected = numDots > 0,
    tag = if (numDots < 2) "Onedot" else "Twodot",
    onLongPress = { setDots(2) }) {
    if (numDots > 0) setDots(0) else setDots(1)
  }
}



@Composable
fun ClefSpinner(
  selectedClef: () -> ClefType,
  border: Boolean = false,
  tag: String = "",
  setClef: (ClefType) -> Unit
) {
  val clefList = clefIds.toList()
  ImageGridDropdown(images = clefList.map { it.first },
    rows = 5,
    columns = 2,
    border = border,
    tag = { "$tag ${clefList[it].second}" },
    selected = { clefList.indexOfFirst { it.second == selectedClef() } },
    onSelect = {
      setClef(clefList[it].second)
    })
}

@Composable
fun ClefSelector(selected: () -> ClefType?, onSelect: (ClefType) -> Unit) {
  IdSelector(ids = clefIds, rows = 2, selected = selected, onSelect = onSelect)
}

@Composable
fun <T> IdSelector(
  ids: List<Pair<Int, T>>,
  rows: Int = 1,
  selected: () -> T?,
  onSelect: (T) -> Unit
) {
  GridSelection(images = ids.map { it.first },
    rows = rows, columns = ids.size / rows,
    tag = {
      ids[it].second.toString()
    },
    itemBorder = true,
    selected = { ids.indexOfFirst { it.second == selected() } },
    onSelect = {
      onSelect(ids[it].second)
    })
}



fun selectionToKs(selection: Int): Int {
  return if (selection < 8) {
    selection
  } else {
    -(selection - 7)
  }
}

fun ksToSelection(ks: Int): Int {
  return if (ks >= 0) {
    ks
  } else {
    -(ks - 7)
  }
}

@Composable
fun KeySelector(
  selected: Int?,
  onSelect: (Int) -> Unit,
  rows: Int = 3,
  modifier: Modifier = Modifier
) {
  val clefs = keySignatureIds.toList()
  GridSelection(images = clefs.map { it.first },
    rows = rows, columns = clefs.size / rows,
    tag = { selectionToKs(it).toString() },
    itemBorder = true,
    modifier = modifier,
    selected = { selected?.let { ksToSelection(it) } ?: -1 },
    onSelect = {
      onSelect(selectionToKs(it))
    })
}

@Composable
fun KeySignatureGrid(selected: () -> Ks, onSelect: (Ks) -> Unit, modifier: Modifier = Modifier) {
  ImageGridDropdown(
    images = keySignatureIds.map { it.first },
    rows = 3,
    columns = 5,
    border = true,
    size = block(1.5f),
    modifier = modifier,
    tag = { "Key ${selectionToKs(it)}" },
    selected = { ksToSelection(selected()) },
    onSelect
    = { onSelect(selectionToKs(it)) }
  )
}

@Composable
fun TimeSignatureSelector(
  getNumerator: () -> Int,
  setNumerator: (Int) -> Unit,
  getDenominator: () -> Int,
  setDenominator: (Int) -> Unit,
  getType: () -> TimeSignatureType,
  setType: (TimeSignatureType) -> Unit,
  disableNumbersIfNotCustom: Boolean = true
) {
  val enabled = !disableNumbersIfNotCustom || getType() == TimeSignatureType.CUSTOM
  val color = if (enabled) MaterialTheme.colors.onSurface else disabledColor

  Row(verticalAlignment = Alignment.CenterVertically) {
    Stoppable(
      enable = enabled, disabledColor = Color.Transparent,
      tag = "NumberRow"
    ) {
      Row {
        NumberSpinner(
          numbers = (1..32).toList(),
          selected = getNumerator, onSelect = {
            setType(TimeSignatureType.CUSTOM)
            setNumerator(it)
          }, color = color, tag = "Numerator"
        )
        Text("/", Modifier.width(block()), color = color, textAlign = TextAlign.Center)
        NumberSpinner(
          numbers = (0..5).map { 2f.pow(it).toInt() }.toList(),
          selected = getDenominator, onSelect = {
            setType(TimeSignatureType.CUSTOM)
            setDenominator(it)
          }, color = color, tag = "Denominator"
        )
      }
    }
    Spacer(modifier = Modifier.width(block()))
    TimeSignatureTypeSelector(
      getType = getType,
      setType = setType
    )
  }
}

@Composable
fun UpbeatSelector(
  getNumerator: () -> Int,
  setNumerator: (Int) -> Unit,
  getDenominator: () -> Int,
  setDenominator: (Int) -> Unit,
  isEnabled: () -> Boolean,
  enable: (Boolean) -> Unit
) {
  val color = if (isEnabled()) MaterialTheme.colors.onSurface else disabledColor
  Row(verticalAlignment = Alignment.CenterVertically) {
    Checkbox(
      checked = isEnabled(),
      onCheckedChange = {
        enable(it)
      },
      modifier = Modifier
        .align(Alignment.CenterVertically)
        .testTag("UpbeatEnableButton"),
    )
    Spacer(modifier = Modifier.width(block()))
    Stoppable(enable = isEnabled(), disabledColor = Color.Transparent) {
      Row() {
        NumberSpinner(
          numbers = (1..32).toList(),
          selected = getNumerator, onSelect = {
            setNumerator(it)
          }, color = color, tag = "Upbeat Numerator"
        )
        Text(
          "/", Modifier.width(block()),
          color = color,
          textAlign = TextAlign.Center
        )
        NumberSpinner(
          numbers = (0..5).map { 2f.pow(it).toInt() }.toList(),
          selected = getDenominator, onSelect = {
            setDenominator(it)
          }, color = color, tag = "Upbeat Denominator"
        )
      }
    }


  }
}

@Composable
fun TempoSelector(
  getDuration: () -> Duration, setDuration: (Duration) -> Unit,
  getDot: () -> Boolean, setDot: (Boolean) -> Unit,
  getBpm: () -> Int, setBpm: (Int) -> Unit, size: Dp = block()
) {

  Row(verticalAlignment = Alignment.CenterVertically) {
    Box(Modifier.align(Alignment.CenterVertically)) {
      DurationSelector(
        { getDuration().undot() },
        {
          ksLogv("Duration selected $it")
          setDuration(it.dot(getDuration().numDots()))
        }, size = size
      )
    }
    Spacer(modifier = Modifier.width(block(0.25f)))
    SquareButton(
      R.drawable.onedot, tag = "Dot", modifier = Modifier.align(Alignment.CenterVertically),
      selected = getDot(), size = size
    ) {
      setDot(!getDot())
    }
    Spacer(modifier = Modifier.width(size / 2))
    Text("=", Modifier.align(Alignment.CenterVertically))
    Spacer(modifier = Modifier.width(size / 2))
    val bpm: String = run {
      val num = getBpm()
      if (num > 0) num.toString() else ""
    }
    Box(
      Modifier
        .width(size * 3)
        .align(Alignment.CenterVertically)
        .border(
          if (getBpm() < 1) BorderStroke(1.dp, Color.Red) else BorderStroke(
            0.dp,
            Color.Transparent
          )
        )
    ) {
      DefocusableTextField(
        value = bpm,
        onValueChange = { str ->
          val int = str.toIntOrNull() ?: 0
          setBpm(int)
        },
        modifier = Modifier.size(block(2.5), block(2)),
        keyboardType = KeyboardType.Number
      )
    }
  }
}

@Composable
private fun TimeSignatureTypeSelector(
  getType: () -> TimeSignatureType,
  setType: (TimeSignatureType) -> Unit,
  modifier: Modifier = Modifier,
) {
  val commonSize = block()
  val cutCommonSize = block(1.2f)

  ToggleRow(
    ids = timeSignatureIds.map { it.first },
    spacing = 10.dp,
    border = false,
    modifier = modifier,
    size = { i -> if (i == 0) commonSize else cutCommonSize },
    selected =
      if (getType() == TimeSignatureType.CUSTOM) {
        null
      } else {
        TimeSignatureType.values().indexOf(getType())
      }
    ,
    tag = { timeSignatureIds[it].second.toString() },
    onSelect = {
      val res = if (it == null) {
        TimeSignatureType.CUSTOM
      } else {
        TimeSignatureType.values()[it]
      }
      setType(res)
    })


}

@Composable
fun InstrumentSelector(
  groups: List<String>,
  group: String,
  setGroup: (String) -> Unit,
  instruments: List<String>,
  instrument: String,
  setInstrument: (String) -> Unit,
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
  setGroup: (String) -> Unit,
  instruments: List<String>,
  instrument: String,
  setInstrument: (String) -> Unit,
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
  setGroup: (String) -> Unit,
  instruments: List<String>,
  instrument: String,
  setInstrument: (String) -> Unit,
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
private fun GroupSpinner(groups: List<String>, group: String, setGroup: (String) -> Unit) {
  TextSpinner(strings = groups,
    selected = { group }, tag = "Group", modifier = Modifier.width(block(5)),
    textAlign = TextAlign.Start,
    onSelect = { setGroup(it) })
}

@Composable
private fun InstrumentSpinner(
  instruments: List<String>,
  instrument: String,
  setInstrument: (String) -> Unit
) {
  TextSpinner(strings = instruments,
    selected = { instrument }, tag = "Instrument", modifier = Modifier.width(block(5)),
    textAlign = TextAlign.Start,
    onSelect = { setInstrument(it) })
}

