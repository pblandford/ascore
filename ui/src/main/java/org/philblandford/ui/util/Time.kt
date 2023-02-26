package org.philblandford.ui.util

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.width
import androidx.compose.material.Checkbox
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.philblandford.ascore.android.ui.style.disabledColor
import com.philblandford.kscore.engine.types.TimeSignatureType
import org.philblandford.ui.common.block
import kotlin.math.pow


@Composable
fun TimeSignatureSelector(
  numerator: Int,
  setNumerator: (Int) -> Unit,
  denominator: Int,
  setDenominator: (Int) -> Unit,
  type: TimeSignatureType,
  setType: (TimeSignatureType) -> Unit,
  disableNumbersIfNotCustom: Boolean = true
) {
  val enabled = !disableNumbersIfNotCustom || type == TimeSignatureType.CUSTOM

  Row(verticalAlignment = Alignment.CenterVertically) {
    Stoppable(enabled, disabledColor = Color.Transparent) {
      CustomTimeSelector(numerator, denominator, setNumerator, setDenominator)
    }
    Spacer(Modifier.width(block()))
    TimeSignatureTypeSelector(type, setType)
  }
}

@Composable
fun CustomTimeSelector(
  numerator: Int,
  denominator: Int,
  setNumerator: (Int) -> Unit,
  setDenominator: (Int) -> Unit,
  enabled: Boolean = true
) {
  val color = if (enabled) MaterialTheme.colors.onSurface else disabledColor

  Row {
    NumberSpinner(
      numbers = (1..32).toList(),
      selected = numerator, onSelect = {
        setNumerator(it)
      }, color = color, tag = "Numerator"
    )
    Text("/", Modifier.width(block()), color = color, textAlign = TextAlign.Center)
    NumberSpinner(
      numbers = (0..5).map { 2f.pow(it).toInt() }.toList(),
      selected = denominator, onSelect = {
        setDenominator(it)
      }, color = color, tag = "Denominator"
    )
  }
}

@Composable
fun UpbeatSelector(
  numerator: Int,
  setNumerator: (Int) -> Unit,
  denominator: Int,
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
          selected = numerator, onSelect = {
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
          selected = denominator, onSelect = {
            setDenominator(it)
          }, color = color, tag = "Upbeat Denominator"
        )
      }
    }


  }
}


@Composable
private fun TimeSignatureTypeSelector(
  type: TimeSignatureType,
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
    if (type == TimeSignatureType.CUSTOM) {
      null
    } else {
      TimeSignatureType.values().indexOf(type)
    },
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
