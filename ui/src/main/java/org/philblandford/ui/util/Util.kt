@file:Suppress("INVISIBLE_REFERENCE", "INVISIBLE_MEMBER")

package org.philblandford.ui.util

import android.annotation.SuppressLint
import android.content.res.Configuration
import androidx.compose.foundation.*
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.*
import androidx.compose.ui.platform.*
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.*
import androidx.compose.ui.text.input.*
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.*
import androidx.compose.ui.window.Popup
import org.philblandford.ui.theme.PopupTheme
import com.philblandford.ascore.android.ui.style.backgroundGray
import com.philblandford.ascore.android.ui.style.disabledDark
import com.philblandford.kscore.log.ksLogt
import com.philblandford.kscore.log.ksLogv
import org.philblandford.ui.common.block
import org.philblandford.ui.R
import kotlin.math.log
import kotlin.math.min
import kotlin.math.pow

@Composable
fun styledBorder(): BorderStroke {
  return BorderStroke(1.dp, Color.White)
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun SquareButton(
  resource: Int,
  modifier: Modifier = Modifier,
  selected: Boolean = false,
  onLongPress: () -> Unit = {},
  border: Boolean = false,
  size: Dp = block(),
  size2d: Pair<Dp, Dp>? = null,
  backgroundColor: Color? = null,
  foregroundColor: Color? = null,
  tag: String = "",
  onClick: () -> Unit = {}
) {
  SquareImage(
    resource,
    modifier.combinedClickable(onClick = onClick, onLongClick = onLongPress),
    selected,
    border,
    size,
    size2d,
    backgroundColor,
    foregroundColor,
    tag,
  )
}

@Composable
fun SquareImage(
  resource: Int,
  modifier: Modifier = Modifier,
  selected: Boolean = false,
  border: Boolean = false,
  size: Dp = block(),
  size2d: Pair<Dp, Dp>? = null,
  backgroundColor: Color? = null,
  foregroundColor: Color? = null,
  tag: String = ""
) {
  var sizeMod = size2d?.let { modifier.size(it.first, it.second) } ?: modifier.size(size)
  if (border) sizeMod = sizeMod.border(1.dp, Color.White)
  Box(
    sizeMod.background(
      backgroundColor
        ?: if (selected) MaterialTheme.colors.secondary else Color.Transparent,
      shape = RoundedCornerShape(5)
    ),
  ) {
    val realTag = if (selected) "$tag on" else tag
    Image(
      painterResource(resource), "",
      Modifier
        .testTag(realTag)
        .size(size)
        .padding(2.dp),
      colorFilter = ColorFilter.tint(foregroundColor ?: MaterialTheme.colors.onSurface)
    )
  }
}

@Composable
fun NumberSpinner(
  numbers: List<Int>, selected: () -> Int, onSelect: (Int) -> Unit,
  color: Color = MaterialTheme.colors.onSurface,
  tag: String = ""
) {
  SpinnerBase(numbers.map { it.toString() }, tag = "Number", child = {
    NumberImage(number = selected(), color = color)
  }) {
    it.toIntOrNull()?.let {
      onSelect(it)
    }
  }
}

@Composable
fun NumberImage(number: Int, color: Color = MaterialTheme.colors.onBackground) {
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
          tag = num.toString(),
        )
      }
    }
  }
}

@Composable
fun ImageGridDropdown(
  images: List<Int>, rows: Int, columns: Int,
  modifier: Modifier = Modifier,
  size: Dp = block(),
  tag: (Int) -> String = { "" },
  border: Boolean = false,
  selected: () -> Int, onSelect: (Int) -> Unit
) {
  val showDropdown = remember { mutableStateOf(false) }

  Box(modifier) {
    SquareButton(
      resource = images[selected()],
      size = size,
      border = border,
      tag = "Button ${tag(selected())}",
      onClick = { showDropdown.value = !showDropdown.value })
    if (showDropdown.value) {

      DropdownMenu(
        expanded = showDropdown.value,
        modifier = Modifier.background(Color.White),
        onDismissRequest = { showDropdown.value = false }) {

        PopupTheme {
          GridSelection(
            images = images, rows = rows, border = border,
            columns = columns, size = size, tag = {
              tag(it)
            }
          ) {
            onSelect(it)
            showDropdown.value = false
          }
        }
      }
    }
  }
}

@Composable
fun GridSelection(
  images: List<Int>,
  rows: Int,
  columns: Int,
  modifier: Modifier = Modifier,
  size: Dp = block(),
  border: Boolean = false,
  itemBorder: Boolean = false,
  tag: (Int) -> String = { "" },
  selected: () -> Int? = { null },
  onSelect: (Int) -> Unit
) {
  val borderMod = if (border) modifier.border(2.dp, MaterialTheme.colors.onSurface) else modifier
  ThemeBox(
    modifier = borderMod.size(size * columns, size * rows)
  ) {
    Column {
      (0 until rows).forEach { row ->
        Row {
          (0 until columns).forEach { column ->
            val idx = (row * columns) + column
            SquareButton(resource = images[idx], size = size,
              tag = tag(idx),
              border = itemBorder,
              selected = selected() == idx,
              onClick = { onSelect(idx) })
          }
        }
      }
    }
  }
}

@Composable
fun BorderBox(modifier: Modifier = Modifier, children: @Composable() () -> Unit) {
  Box(modifier.border(2.dp, MaterialTheme.colors.onSurface)) {
    children()
  }
}

@Composable
fun ToggleButton(
  resource: Int,
  selected: Boolean, toggle: () -> Unit,
  border: Boolean = false,
  tag:String = ""
) {
  SquareButton(resource = resource, border = border, selected = selected, tag = tag, onClick = {
    toggle()
  })
}

@Composable
fun ThemeBox(
  modifier: Modifier = Modifier,
  shape: Shape = RectangleShape,
  backgroundColor: Color = MaterialTheme.colors.surface,
  border: BorderStroke? = null,
  padding: Dp = border?.width ?: 0.dp,
  children: @Composable() () -> Unit = {}
) {
  var mod2 = modifier
    .background(backgroundColor, shape)
    .padding(padding)
  border?.let { mod2 = mod2.border(border) }
  Box(mod2) { children() }
}

@Composable
fun NumberPicker(
  min: Int, max: Int, getNum: () -> Int, setNum: (Int) -> Unit,
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
            if (getNum() > min) {
              setNum(left(getNum()))
            }
          })
        .wrapContentWidth()
        .padding(horizontal = block() / 2)
    )
    Box(Modifier.wrapContentWidth()) {
      if (editable) {
        TextField(
          value = getNum().toString(),
          onValueChange = { str: String ->
            setNum(str.toInt())
          },
          label = {},
          textStyle = MaterialTheme.typography.body1.copy(textAlign = TextAlign.Center),
          keyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.Number)
        )
      } else {
        Text(
          getNum().toString(),
          textAlign = TextAlign.Center,
          modifier = Modifier
            .wrapContentWidth()
            .padding(horizontal = block() / 2)
        )
      }
    }
    Text("+",
      Modifier
        .testTag(tag()?.let { "IncrementButton ${tag()}" } ?: "IncrementButton")
        .clickable(
          onClick = {
            if (getNum() < max) {
              setNum(right(getNum()))
            }
          })
        .padding(horizontal = block() / 2)
    )
  }
}



@Composable
fun ToggleColumn(
  ids: List<Int>,
  modifier: Modifier = Modifier,
  spacing: Dp = 0.dp,
  border: Boolean = true,
  tag: (Int) -> String = { "" },
  size: @Composable() (Int) -> Dp = { block() },
  selected: () -> Int?,
  onSelect: (Int?) -> Unit
) {

  Box(modifier.border(if (border) 1.dp else 0.dp, Color.White)) {
    Column {
      ids.withIndex().map { iv ->
        SquareButton(
          resource = iv.value,
          size = size(iv.index),
          selected = selected() == iv.index,
          tag = "Button ${tag(iv.index)}",
          modifier = Modifier.align(Alignment.CenterHorizontally),
          onClick = {
            if (selected() == iv.index) {
              onSelect(null)
            } else {
              onSelect(iv.index)
            }
          })
        Spacer(modifier = Modifier.width(spacing))
      }
    }
  }
}

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
  onSelect: (String) -> Unit
) {

  SpinnerBase(
    strings, modifier, itemModifier, tag, grid, gridRows, gridColumns, buttonBorder,
    textStyle,
    {
      Text(
        text = selected(),
        style = textStyle() ?: MaterialTheme.typography.body1,
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
  onSelect: (String) -> Unit
) {
  val showDropdown = remember { mutableStateOf(false) }
  val borderMod = if (buttonBorder) modifier.border(
    1.dp,
    MaterialTheme.colors.onSurface
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
  }

  if (showDropdown.value) {
    val screenHeight = LocalConfiguration.current.screenHeightDp
    val bottom = globalY.value + height
    val overlap = bottom - screenHeight
    val density = LocalDensity.current.density
    val y = if (overlap > 0) (-(overlap) * density) else 0f

    ksLogt("height ${block().value} $density $y ${globalY.value} $bottom $height $screenHeight $overlap")
    Popup(
      offset = IntOffset(localX.value, y.toInt()),
      onDismissRequest = { showDropdown.value = false }
    ) {
      PopupTheme() {
        if (grid) {
          TextGrid(
            strings = strings,
            rows = gridRows,
            columns = gridColumns,
            tag = tag,
            textStyle = textStyle() ?: MaterialTheme.typography.body1,
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
  onSelect: (String) -> Unit,
) {
  Box(
    Modifier
      .background(MaterialTheme.colors.background)
      .border(2.dp, MaterialTheme.colors.onSurface)
      .padding(5.dp)
  ) {
    val itemHeight = block(0.8)
    val itemHeightPadding = itemHeight + 6.dp
    val height = if (strings.size < 10) itemHeightPadding * strings.size else itemHeightPadding * 10
    ScrollableColumn(Modifier.height(height)) {
      strings.forEach { string ->
        val testTag = "$tag $string"
        Text(
          string,
          modifier = Modifier
            .clickable { onSelect(string) }
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
  textStyle: TextStyle = MaterialTheme.typography.body1,
  itemModifier: Modifier = Modifier,
  onSelect: (String) -> Unit,
) {
  Box(
    modifier = modifier
      .background(MaterialTheme.colors.background)
      .border(if (border) BorderStroke(2.dp, MaterialTheme.colors.onSurface) else NoBorder)
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
                  .clickable(onClick = { onSelect(str) })
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
fun LabelText(resId: Int, modifier: Modifier = Modifier, align: TextAlign? = null) {
  Text(
    stringResource(id = resId), fontSize = 15.sp, modifier = modifier,
    textAlign = align
  )
}

@Composable
fun LabelText(string: String) {
  Text(string, fontSize = 15.sp)
}


@Composable
fun Stoppable(
  enable: Boolean, tag: String = "",
  disabledColor: Color = com.philblandford.ascore.android.ui.style.disabledColor,
  children: @Composable() () -> Unit
) {
  Box {
    Box(Modifier.testTag(tag)) {
      children()
    }
    if (!enable) {
      ksLogv("$tag is disabled")
      Box(
        Modifier
          .matchParentSize()
          .pointerInput(Unit) {
            detectTapGestures(onTap = {})
          }
          .testTag("$tag disabled")
          .background(disabledColor)
      )
    }
  }
}


@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun FreeKeyboard(
  initValue: String,
  onValueChanged: (String) -> Unit,
  refresh: Boolean = true,
  tag: String = "TextTextField",
  content: @Composable() KeyboardDelegate.() -> Unit
) {
  val inputService = LocalTextInputService.current!!
  val editProcessor = remember { EditProcessor() }
  val focusRequester = remember { FocusRequester() }

  val textFieldValue =
    remember {
      mutableStateOf(
        TextFieldValue(
          initValue, TextRange(
            initValue.length,
            initValue.length
          )
        )
      )
    }

  if (refresh) {
    textFieldValue.value = TextFieldValue(initValue, TextRange(initValue.length, initValue.length))
  }

  val token = remember {
    inputService.startInput(textFieldValue.value,
      imeOptions = ImeOptions(
        imeAction = ImeAction.None,
        autoCorrect = false,
        keyboardType = KeyboardType.Text,
        capitalization = KeyboardCapitalization.None
      ),
      onImeActionPerformed = {},
      onEditCommand = { things ->
        ksLogt("THINGS")
        var text = textFieldValue.value.text
        things.forEach { ksLogt("$it $text") }

        things.forEach {
          when (it) {
            is SetComposingTextCommand -> {
              it.text.lastOrNull()?.let { text += it }
              ksLogt("setComp2 $text ${it.text}")
            }
            is CommitTextCommand -> {
              it.text.lastOrNull()?.let { lastChar ->
                text += lastChar
              }
            }
            is BackspaceCommand -> {
              text = text.dropLast(1)
            }
          }
        }

        if (text != textFieldValue.value.text) {
          textFieldValue.value = TextFieldValue(text, TextRange(text.length))
          onValueChanged(text)
        }
      })
  }
  LocalView.current.requestFocus()

  editProcessor.reset(textFieldValue.value, null)
  val keyboardController = LocalSoftwareKeyboardController.current!!

  val delegate = remember {
    KeyboardDelegate(keyboardController, focusRequester)
  }
  Box(
    Modifier
      .focusRequester(focusRequester)
      .testTag(tag)
  ) {
    delegate.content()
  }

  DisposableEffect(Unit) {
    onDispose {
      ksLogt("Ondispose")
      delegate.close()
      inputService.stopInput(token)
    }
  }
}

@OptIn(ExperimentalComposeUiApi::class)
class KeyboardDelegate  constructor(
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

@Composable
fun UpDownDependent(modifier: Modifier, isUp: () -> Boolean, set: (Boolean) -> Unit) {
  if (LocalConfiguration.current.orientation == Configuration.ORIENTATION_PORTRAIT) {
    UpDownColumn(isUp, set, modifier)
  } else {
    UpDownRow(isUp, set, modifier)
  }
}

@Composable
fun UpDownColumn(isUp: () -> Boolean, set: (Boolean) -> Unit, modifier: Modifier = Modifier) {
  Column(modifier) {
    ToggleColumn(ids = listOf(R.drawable.up, R.drawable.down), selected = {
      if (isUp()) 0 else 1
    }, onSelect = { set(it == 0) }, tag = {
      if (it == 0) "Up" else "Down"
    })
  }
}

@Composable
fun UpDownRow(isUp: () -> Boolean, set: (Boolean) -> Unit, modifier: Modifier = Modifier) {
  Column(modifier) {
    ToggleRow(ids = listOf(R.drawable.up, R.drawable.down), selected =
      if (isUp()) 0 else 1
    , onSelect = { set(it == 0) }, tag = {
      if (it == 0) "Up" else "Down"
    })
  }
}

@Composable
fun Gap(size: Dp) = Spacer(Modifier.size(size))

@Composable
fun Gap(blocks: Float) = Gap(block(blocks))

@Composable
fun LeftRight(left: () -> Unit, right: () -> Unit) {
  Row(Modifier.border(styledBorder())) {
    SquareButton(
      resource = R.drawable.left_arrow,
      onClick = { left() }, tag = "LeftArrow"
    )
    SquareButton(
      resource = R.drawable.right_arrow,
      onClick = { right() }, tag = "RightArrow"
    )
  }
}

@Composable
fun OutlinedTextField2(
  value: String,
  @SuppressLint("ModifierParameter") modifier: Modifier = Modifier.size(block(5), block(2)),
  onValueChange: (String) -> Unit,
  onDone: (String) -> Unit = {},
  tag: String = "", refresh: Boolean = false,
  id: Int = -1, keyboardType: KeyboardType = KeyboardType.Text,
) {
  val text = remember { mutableStateOf(value) }

  if (refresh) {
    text.value = value
  }

  ThemeBox(modifier) {
    OutlinedTextField(value = text.value,
      onValueChange = {
        text.value = it
        onValueChange(it)
      },
      // activeColor = MaterialTheme.colors.onSurface,
      keyboardOptions = KeyboardOptions(
        keyboardType = keyboardType,
        imeAction = ImeAction.Done
      ),
      keyboardActions = KeyboardActions { onDone(text.value) },
      modifier = Modifier
        .fillMaxSize()
        .testTag(tag),
      label = { if (id != -1) Text(stringResource(id = id)) }
    )
  }
}


@Composable
fun TextField2(
  value: String, onValueChange: (String) -> Unit,
  modifier: Modifier = Modifier.size(block(5), block(2)),
  tag: String = "", refresh: Boolean = false,
  id: Int = -1, keyboardType: KeyboardType = KeyboardType.Text,
) {
  val text = remember { mutableStateOf(value) }
  if (refresh) {
    text.value = value
  }

  ThemeBox(modifier) {
    TextField(value = text.value,
      onValueChange = {
        text.value = it
        onValueChange(it)
      },
      keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
      modifier = Modifier
        .fillMaxSize()
        .testTag(tag),
      label = { if (id != -1) Text(stringResource(id = id)) }
    )
  }
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
fun ThemeButton(id: Int, modifier: Modifier = Modifier, onClick: () -> Unit) {
  Button(onClick = onClick, modifier = modifier) {
    Text(stringResource(id = id), color = MaterialTheme.colors.onPrimary)
  }
}

@Composable
fun StyledText(
  id: Int, modifier: Modifier = Modifier,
  fontSize: TextUnit = MaterialTheme.typography.body1.fontSize
) {
  StyledText(stringResource(id), modifier, fontSize)
}

@Composable
fun StyledText(
  text: String,
  modifier: Modifier = Modifier,
  fontSize: TextUnit = MaterialTheme.typography.body1.fontSize
) {
  StyledBox(modifier) {
    Text(text, fontSize = fontSize)
  }
}


@Composable
fun StyledBox(
  modifier: Modifier = Modifier,
  content: @Composable () -> Unit
) {
  Box(
    modifier
      .border(1.dp, MaterialTheme.colors.onSurface, RoundedCornerShape(7.dp))
      .background(MaterialTheme.colors.surface)
      .padding(2.dp)
  ) {
    Box(
      Modifier
        .padding(1.dp)
        .border(1.dp, MaterialTheme.colors.onSurface, RoundedCornerShape(7.dp))
        .padding(5.dp),
    ) {
      content()
    }
  }
}

@Composable
fun FakePopup(onDismiss: () -> Unit, width: Dp = block(10), content: @Composable () -> Unit) {
  val screenWidth = LocalConfiguration.current.screenWidthDp.dp
  val screenHeight = LocalConfiguration.current.screenHeightDp.dp
  Box(
    Modifier
      .size(screenWidth, screenHeight)
      .clickable(onClick = {
        onDismiss()
      })
      .background(backgroundGray)
  ) {
    Box(
      Modifier
        .align(Alignment.Center)
        .width(width)
        .background(MaterialTheme.colors.surface, RoundedCornerShape(5.dp))
        .border(1.dp, MaterialTheme.colors.onSurface)
        .clickable(onClick = {})
    ) {
      content()
    }
  }
}

fun String.nullIfEmpty() = if (isEmpty()) null else this

fun String.getAbbreviation(): String {
  val filtered = filter { it.isLetterOrDigit() }
  val split = filtered.split(" ").toList()
  return if (split.isNotEmpty()) {
    String(split.map { it.firstOrNull() ?: ' ' }.toCharArray())
  } else ""
}

val NoBorder = BorderStroke(0.dp, Color.Transparent)

@Composable
fun ScrollableColumn(
  modifier: Modifier,
  scrollState: ScrollState = rememberScrollState(),
  alignment: Alignment.Horizontal = Alignment.Start,
  content: @Composable () -> Unit
) {
  Column(modifier.verticalScroll(scrollState), horizontalAlignment = alignment) {
    content()
  }
}

@Composable
fun ScrollableRow(
  modifier: Modifier,
  scrollState: ScrollState = rememberScrollState(),
  content: @Composable () -> Unit
) {
  Row(modifier.horizontalScroll(scrollState)) {
    content()
  }
}

@Composable
fun DefocusableTextField(
  value: String, onValueChange: (String) -> Unit,
  modifier: Modifier = Modifier,
  keyboardType: KeyboardType = KeyboardType.Text
) {
  val focusManager = LocalFocusManager.current

  OutlinedTextField(
    value = value,
    onValueChange = onValueChange,
    modifier = modifier,
    keyboardOptions = KeyboardOptions(keyboardType = keyboardType, imeAction = ImeAction.Done),
    keyboardActions = KeyboardActions(onDone = { focusManager.clearFocus() })
  )
}