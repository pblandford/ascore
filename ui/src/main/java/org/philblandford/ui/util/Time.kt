package org.philblandford.ui.util

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material.Checkbox
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.accompanist.pager.ExperimentalPagerApi
import com.google.accompanist.pager.VerticalPager
import com.google.accompanist.pager.rememberPagerState
import com.philblandford.ascore.android.ui.style.disabledColor
import com.philblandford.kscore.engine.time.TimeSignature
import com.philblandford.kscore.engine.types.TimeSignatureType
import org.philblandford.ui.common.block
import timber.log.Timber
import kotlin.math.log
import kotlin.math.pow


@Composable
fun TimeSignatureSelector(
  timeSignature: TimeSignature,
  set:(TimeSignature)->Unit,
  disableNumbersIfNotCustom: Boolean = true
) {
  val enabled = !disableNumbersIfNotCustom || timeSignature.type == TimeSignatureType.CUSTOM

  Row(verticalAlignment = Alignment.CenterVertically) {
    Stoppable(enabled, disabledColor = Color.Transparent) {
      CustomTimeSelector(timeSignature, set)
    }
    Spacer(Modifier.width(block()))
    TimeSignatureTypeSelector(timeSignature.type, { set(timeSignature.copy(type = it))})
  }
}

@Composable
fun CustomTimeSelector(
  timeSignature: TimeSignature,
  set: (TimeSignature) -> Unit,
  enabled: Boolean = true
) {
  val color = if (enabled) MaterialTheme.colors.onSurface else disabledColor

  Row {
      TimeNumberPicker(Modifier.size(50.dp, 30.dp), (1..32).toList(), timeSignature.numerator)
      { set(timeSignature.copy(numerator = it, type = TimeSignatureType.CUSTOM)) }
      Text(
        "/",
        Modifier.width(block()),
        fontSize = 25.sp,
        color = color,
        textAlign = TextAlign.Center
      )
      val range = (1..5).map { 2.0.pow(it.toDouble()).toInt() }
      TimeNumberPicker(Modifier.size(50.dp, 30.dp), range, timeSignature.denominator)
      { set(timeSignature.copy(denominator = it, type = TimeSignatureType.CUSTOM)) }

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
          numbers = (1..5).map { 2f.pow(it).toInt() }.toList(),
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

@Composable
fun TimeNumberPickerOld(modifier: Modifier, range: List<Int>, value: Int, set: (Int) -> Unit) {

  NumberPicker(value, modifier, range, image = { num, mod ->
    val numDigits = log(num.toDouble(), 10.0).toInt() + 1

    Row(mod) {
      val digits =
        (0 until numDigits).map { (num / 10.0.pow(it.toDouble()).toInt()) % 10 }.reversed()
      digits.forEach {
        numberIds.getOrNull(it)?.let { id ->
          Image(painterResource(id), "", Modifier.size(50.dp))
        }
      }
    }
  }) { set(it) }
}

@OptIn(ExperimentalPagerApi::class)
@Composable
fun TimeNumberPicker(
  modifier: Modifier, range: List<Int>, initValue: Int,
  set: (Int) -> Unit
) {
  val pagerState = rememberPagerState()

  LaunchedEffect(initValue) {
    val page = range.indexOf(initValue).coerceAtLeast(0)
    pagerState.scrollToPage(page)
  }

  LaunchedEffect(pagerState.currentPage) {
    range.getOrNull(pagerState.currentPage)?.let {
      if (it != initValue)
        set(it)
    }
  }

  VerticalPager(range.size, modifier, pagerState) { idx ->

    range.getOrNull(idx)?.let { num ->

      val numDigits = log(num.toDouble(), 10.0).toInt() + 1

      Row {
        val digits =
          (0 until numDigits).map { (num / 10.0.pow(it.toDouble()).toInt()) % 10 }.reversed()
        digits.forEach {
          numberIds.getOrNull(it)?.let { id ->
            Image(
              painterResource(id), "",
              Modifier
                .fillMaxHeight()
                .padding(3.dp),
              contentScale = ContentScale.FillHeight,
              colorFilter = ColorFilter.tint(MaterialTheme.colors.onSurface)
            )
          }
        }
      }
    }
  }
}

@Composable
@Preview
private fun PreviewTimeNumberPicker() {
  var num by remember { mutableStateOf(4) }
  Box(Modifier.fillMaxSize()) {
    TimeNumberPicker(
      Modifier
        .size(100.dp, 50.dp)
        .align(Alignment.Center), (1..32).toList(), num
    ) { num = it }
  }
}