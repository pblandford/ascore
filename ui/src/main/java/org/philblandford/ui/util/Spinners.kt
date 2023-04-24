package org.philblandford.ui.util

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInParent
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.times
import com.philblandford.kscore.log.ksLogt
import org.philblandford.ui.R
import org.philblandford.ui.common.block
import kotlin.math.log
import kotlin.math.min
import kotlin.math.pow


@Composable
fun TextSpinner(
  strings: List<String>,
  modifier: Modifier = Modifier,
  itemModifier: Modifier = Modifier,
  tag: String = "",
  grid: Boolean = false,
  gridRows: Int = strings.size, gridColumns: Int = 1,
  buttonBorder: Boolean = false,
  textStyle: @Composable () -> TextStyle? = { null },
  textAlign: TextAlign = TextAlign.Start,
  selected: () -> String,
  onSelect: (Int) -> Unit
) {

  SpinnerBase(
    strings, modifier, itemModifier, tag, grid, gridRows, gridColumns, buttonBorder,
    textStyle,
    {
      Text(
        text = selected(),
        style = textStyle() ?: MaterialTheme.typography.bodyLarge,
        modifier = Modifier
          .align(
            if (textAlign == TextAlign.Start) Alignment.CenterStart else Alignment.Center
          )
          .testTag("$tag ${selected()}"),
        textAlign = textAlign
      )
    }, onSelect
  )
}

@Composable
private fun SpinnerBase(
  strings: List<String>,
  modifier: Modifier = Modifier,
  itemModifier: Modifier = Modifier,
  tag: String = "",
  grid: Boolean = false,
  gridRows: Int = strings.size, gridColumns: Int = 1,
  buttonBorder: Boolean = false,
  textStyle: @Composable () -> TextStyle? = { null },
  child: @Composable BoxScope.() -> Unit,
  onSelect: (Int) -> Unit
) {
  val showDropdown = remember { mutableStateOf(false) }
  val borderMod = if (buttonBorder) modifier.border(
    1.dp,
    MaterialTheme.colorScheme.onSurface
  ) else modifier
  val localX = remember { mutableStateOf(0) }
  val globalY = remember { mutableStateOf(0) }
  val rowsShown = min(gridRows, 10)
  val height = ((rowsShown * block(1.1f))).value

  Box(
    borderMod
      .clickable {
        showDropdown.value = !showDropdown.value
      }
      .onGloballyPositioned { lc ->
        globalY.value = lc.positionInParent().y.toInt()
        localX.value = lc.positionInParent().x.toInt()
      }) {
    child()


    if (showDropdown.value) {
      val screenHeight = LocalConfiguration.current.screenHeightDp
      val bottom = globalY.value + height
      val overlap = bottom - screenHeight
      val density = LocalDensity.current.density
      val y = if (overlap > 0) (-(overlap) * density) else 0f

      ksLogt("height ${block().value} $density $y ${globalY.value} $bottom $height $screenHeight $overlap")

      DropdownMenu(
        expanded = showDropdown.value,
        onDismissRequest = { showDropdown.value = false }) {
        if (grid) {
          TextGrid(
            strings = strings,
            rows = gridRows,
            columns = gridColumns,
            tag = tag,
            textStyle = textStyle() ?: MaterialTheme.typography.bodyLarge,
            itemModifier = itemModifier
          ) {
            onSelect(it)
            showDropdown.value = false
          }
        } else {
          TextSpinnerDropdown(strings = strings, tag = tag) {
            onSelect(it)
            showDropdown.value = false
          }
        }
      }
    }

  }
}

@Composable
private fun TextSpinnerDropdown(
  strings: List<String>,
  tag: String = "",
  onSelect: (Int) -> Unit,
) {
  Box(
    Modifier
      .background(MaterialTheme.colorScheme.surface)
      .border(2.dp, MaterialTheme.colorScheme.onSurface)
      .padding(5.dp)
  ) {
    val itemHeight = block(0.8)
    val itemHeightPadding = itemHeight + 6.dp
    val height = if (strings.size < 10) itemHeightPadding * strings.size else itemHeightPadding * 10
    ScrollableColumn(Modifier.height(height)) {
      strings.withIndex().forEach { (idx, string) ->
        val testTag = "$tag $string"
        Text(
          string,
          modifier = Modifier
            .clickable { onSelect(idx) }
            .padding(3.dp)
            .testTag(testTag)
            .height(itemHeight)
        )
      }
    }
  }
}


@Composable
fun TextGrid(
  strings: List<String>,
  modifier: Modifier = Modifier,
  rows: Int = strings.size, columns: Int = 1,
  tag: String = "",
  border: Boolean = true,
  textStyle: TextStyle = MaterialTheme.typography.bodyLarge,
  itemModifier: Modifier = Modifier,
  onSelect: (Int) -> Unit,
) {
  Box(
    modifier = modifier
      .background(MaterialTheme.colorScheme.surface)
      .border(if (border) BorderStroke(2.dp, MaterialTheme.colorScheme.onSurface) else NoBorder)
  ) {

    Column {
      (0 until rows).forEach { row ->
        Row {
          (0 until columns).forEach { column ->
            val idx = (row * columns) + column
            strings.getOrNull(idx)?.let { str ->
              Text(
                str,
                modifier = itemModifier
                  .clickable(onClick = { onSelect(idx) })
                  .padding(10.dp)
                  .testTag("$tag $str"),
                style = textStyle
              )
            }
          }
        }
      }
    }
  }
}


@Composable
fun NumberSpinner(
  numbers: List<Int>, selected: Int, onSelect: (Int) -> Unit,
  color: Color = MaterialTheme.colorScheme.onSurface,
  tag: String = ""
) {
  SpinnerBase(numbers.map { it.toString() }, tag = "Number", child = {
    NumberImage(number = selected, color = color)
  }) {
    onSelect(numbers[it])
  }
}

@Composable
fun NumberImage(number: Int, color: Color = MaterialTheme.colorScheme.onBackground) {
  if (number == 0) {
    SquareImage(R.drawable.zero, modifier = Modifier.testTag("0"))
  } else {
    val numDigits = log(number.toFloat(), 10f).toInt() + 1
    Row {
      (numDigits downTo 1).map { digit ->
        val num = ((number % 10f.pow(digit)) / 10f.pow(digit - 1)).toInt()
        val numberRes = numberIds[num]
        SquareImage(
          numberRes,
          foregroundColor = color,
        )
      }
    }
  }
}

