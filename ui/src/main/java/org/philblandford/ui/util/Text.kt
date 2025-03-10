package org.philblandford.ui.util

import android.annotation.SuppressLint
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.sp
import org.philblandford.ui.common.block
import timber.log.Timber


@Composable
fun LabelText(resId: Int, modifier: Modifier = Modifier, align: TextAlign? = null) {
  Text(
    stringResource(id = resId), fontSize = 15.sp, modifier = modifier,
    textAlign = align
  )
}

@Composable
fun LabelText(string: String) {
  Text(string, fontSize = 20.sp, fontWeight = FontWeight.Bold)
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OutlinedTextField2(
  value: String,
  onValueChange: (String) -> Unit,
  @SuppressLint("ModifierParameter") modifier: Modifier = Modifier.size(block(5), block(2)),
  onDone: (String) -> Unit = {},
  id: Int = -1, keyboardType: KeyboardType = KeyboardType.Text,
) {
  Timber.e("OTF TITLE ${value}")
    OutlinedTextField(value = value,
      onValueChange = {
        onValueChange(it)
      },
      keyboardOptions = KeyboardOptions(
        keyboardType = keyboardType,
        imeAction = ImeAction.Done
      ),
      textStyle = LocalTextStyle.current.copy(color = Color.Black),
      colors = TextFieldDefaults.colors(focusedTextColor = Color.Black),
      keyboardActions = KeyboardActions { onDone(value) },
      modifier = modifier,
      label = { if (id != -1) Text(stringResource(id = id)) }
    )
}




@OptIn(ExperimentalFoundationApi::class)
@Composable
fun BaseTextField2(
  value: String, onValueChange: (String) -> Unit,
  modifier: Modifier = Modifier.size(block(5), block(2)),
  tag: String = "", refresh: Boolean = false,
  id: Int = -1, keyboardType: KeyboardType = KeyboardType.Text,
) {
  val text = remember { mutableStateOf(TextFieldValue(value)) }
  if (refresh) {
    text.value = TextFieldValue(value)
  }

  ThemeBox(modifier) {
    BasicTextField(
      value = text.value,
      onValueChange = {
        text.value = it
        onValueChange(it.text)
      },
      keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
      textStyle = LocalTextStyle.current,
      modifier = Modifier
        .fillMaxSize()
        .testTag(tag)
    )
  }
}


@Composable
fun StyledText(
  id: Int, modifier: Modifier = Modifier,
  fontSize: TextUnit = MaterialTheme.typography.bodyLarge.fontSize
) {
  StyledText(stringResource(id), modifier, fontSize)
}

@Composable
fun StyledText(
  text: String,
  modifier: Modifier = Modifier,
  fontSize: TextUnit = MaterialTheme.typography.bodyLarge.fontSize
) {
  StyledBox(modifier) {
    Text(text, fontSize = fontSize)
  }
}
