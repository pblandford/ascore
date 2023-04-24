package org.philblandford.ui.util

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import org.philblandford.ui.common.block


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NumberSelector(
  min: Int, max: Int, num:Int, setNum: (Int) -> Unit,
  step: Int = 1,
  left: (Int) -> Int = { it - step },
  right: (Int) -> Int = { it + step },
  editable: Boolean = false,
  tag: () -> String? = { null }
) {
  Row {
    Text(
      "-",
      Modifier
        .testTag(tag()?.let { "DecrementButton ${tag()}" } ?: "DecrementButton")
        .clickable(
          onClick = {
            if (num > min) {
              setNum(left(num))
            }
          })
        .wrapContentWidth()
        .padding(horizontal = block() / 3)
    )
    Box(Modifier.wrapContentWidth()) {
      if (editable) {
        TextField(
          value = num.toString(),
          onValueChange = { str: String ->
            setNum(str.toInt())
          },
          label = {},
          textStyle = MaterialTheme.typography.bodyLarge.copy(textAlign = TextAlign.Center),
          keyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.Number)
        )
      } else {
        Text(
          num.toString(),
          textAlign = TextAlign.Center,
          modifier = Modifier
            .wrapContentWidth()
            .padding(horizontal = block() / 3)
        )
      }
    }
    Text("+",
      Modifier
        .testTag(tag()?.let { "IncrementButton ${tag()}" } ?: "IncrementButton")
        .clickable(
          onClick = {
            if (num < max) {
              setNum(right(num))
            }
          })
        .padding(horizontal = block() / 3)
    )
  }
}
