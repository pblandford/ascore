package org.philblandford.ui.util

import android.view.KeyEvent.ACTION_UP
import android.view.KeyEvent.KEYCODE_DEL
import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.*
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.input.key.onKeyEvent
import androidx.compose.ui.platform.*
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.*
import com.philblandford.kscore.log.ksLogt
import timber.log.Timber

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun FreeKeyboard(
  initValue: String,
  onValueChanged: (String) -> Unit,
  onEnter: () -> Unit,
  updateCounter:MutableState<Int> = remember {
    mutableStateOf(0)
  },
  content: @Composable() KeyboardDelegate.() -> Unit
) {
  val inputService = LocalTextInputService.current!!
  val editProcessor = remember { EditProcessor() }
  val focusRequester = remember { FocusRequester() }

  val token = remember(updateCounter.value) {
    val textFieldValue =
      TextFieldValue(
      initValue, TextRange(
        initValue.length,
        initValue.length
      ), TextRange(0,initValue.length)
    )
    val token = inputService.startInput(
      textFieldValue,
      imeOptions = ImeOptions(
        imeAction = ImeAction.Next,
        autoCorrect = false,
        keyboardType = KeyboardType.Text,
        capitalization = KeyboardCapitalization.None
      ),
      onImeActionPerformed = {
        Timber.e("LYR action $it")
        onEnter()
      },
      onEditCommand = { commands ->
        val oldValue = editProcessor.toTextFieldValue()
        editProcessor.apply(commands)
        if (oldValue.text != editProcessor.toTextFieldValue().text) {
          onValueChanged(editProcessor.toTextFieldValue().text)
        }
      })
    editProcessor.reset(textFieldValue, token)
    token.showSoftwareKeyboard()
    token.updateState(null, textFieldValue)
    token
  }

  LocalView.current.requestFocus()

  val keyboardController = LocalSoftwareKeyboardController.current!!

  val delegate = remember {
    KeyboardDelegate(keyboardController, focusRequester)
  }
  Box(
    Modifier
      .onKeyEvent { event ->
        if (event.nativeKeyEvent.action == ACTION_UP && event.nativeKeyEvent.keyCode == KEYCODE_DEL &&
          editProcessor.toTextFieldValue().text.isNotEmpty()
        ) {
          val current = editProcessor.toTextFieldValue()
          val newPos = (current.selection.start - 1).coerceAtLeast(0)
          val newValue = TextFieldValue(
            current.text.removeRange(newPos, newPos + 1),
            TextRange(newPos, newPos)
          )
          token.updateState(null, newValue)
          editProcessor.reset(newValue, token)
          onValueChanged(newValue.text)
          true
        } else false
      }
      .focusRequester(focusRequester)
      .focusable()
  ) {
    LaunchedEffect(Unit) {
      focusRequester.requestFocus()
    }
    delegate.content()
  }

  DisposableEffect(Unit) {
    onDispose {
      ksLogt("LYR Ondispose")
      delegate.close()
      inputService.stopInput(token)
    }
  }
}

@OptIn(ExperimentalComposeUiApi::class)
class KeyboardDelegate constructor(
  private val keyboardController: SoftwareKeyboardController,
  private val focusRequester: FocusRequester
) {
  private var haveFocus = false

  fun show() {
    ksLogt("Show")
    focusRequester.requestFocus()
    keyboardController.show()
    haveFocus = true
  }

  fun hide() {
    focusRequester.freeFocus()
    keyboardController.show()
    haveFocus = false
  }

  fun close() {
    keyboardController.hide()
  }

  fun toggle() {
    ksLogt("toggle $haveFocus")
    if ((haveFocus)) {
      hide()
    } else {
      show()
    }
  }
}