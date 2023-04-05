package org.philblandford.ui.theme

import android.annotation.SuppressLint
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Colors
import androidx.compose.material.MaterialTheme
import androidx.compose.material.darkColors
import androidx.compose.material.lightColors
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.philblandford.ascore.android.ui.style.*
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import org.koin.androidx.compose.inject
import org.philblandford.ascore2.features.settings.usecases.GetColors
import org.philblandford.ui.common.block
import org.philblandford.ui.util.ThemeBox

enum class Theme {
  BLUE,
  RED,
  GREEN,
  BLACK,
  VIOLET,
  GRAY,
  COOL
}

private val BlueColors = darkColors(
  primary = darkBlue,
  primaryVariant = darkBlue2,
  secondary = lightBlue2,
  secondaryVariant = powderBlue,
  background = darkBlue,
  surface = darkBlue,
  onPrimary = Color.White,
  onSecondary = Color.Black,
  onBackground = Color.White,
  onSurface = Color.White,

  )

private val RedColors = darkColors(
  primary = darkRed,
  primaryVariant = darkRed2,
  secondary = lightRed,
  background = Color.White,
  surface = Color.White,
  onPrimary = Color.White,
  onSecondary = Color.Black,
  onBackground = Color.Black,
  onSurface = Color.Black
)

private val BlackColors = darkColors(
  primary = Color.Black,
  primaryVariant = Color.Gray,
  secondary = veryLightGray,
  background = Color.White,
  surface = Color.White,
  onPrimary = Color.White,
  onSecondary = Color.Black,
  onBackground = Color.Black,
  onSurface = Color.Black
)

@SuppressLint("ConflictingOnColor")
private val GreenColors = darkColors(
  primary = darkGreen,
  primaryVariant = lightGreen2,
  secondary = lightGreen2,
  background = Color.White,
  surface = darkGreen,
  onPrimary = Color.White,
  onSecondary = Color.Black,
  onBackground = Color.Black,
  onSurface = Color.Black
)

private val VioletColors = darkColors(
  primary = violet,
  primaryVariant = violet2,
  secondary = darkViolet,
  background = Color.White,
  surface = Color.White,
  onPrimary = Color.White,
  onSecondary = Color.Black,
  onBackground = Color.Black,
  onSurface = Color.Black
)

private val ContrastColors = lightColors(
  primary = veryLightGray,
  primaryVariant = veryLightGray2,
  secondary = veryLightGray2,
  background = Color.White,
  surface = Color.White,
  onPrimary = Color.Black,
  onSecondary = Color.Black,
  onBackground = Color.Black,
  onSurface = Color.Black
)

private val GrayColors = lightColors(
  primary = lightGray,
  primaryVariant = veryLightGray,
  secondary = lightGray,
  background = Color.White,
  surface = veryLightGray2,
  onPrimary = Color.Black,
  onSecondary = Color.Black,
  onBackground = Color.Black,
  onSurface = Color.Black
)

val CoolColors = lightColors(
  surface = Color(0xffe6e6e6),
  onSurface = Color(0xff4d3900)
)

private fun getColors(theme: Theme): Colors {
  return when (theme) {
    Theme.BLUE -> BlueColors
    Theme.RED -> RedColors
    Theme.GREEN -> GreenColors
    Theme.BLACK -> BlackColors
    Theme.VIOLET -> VioletColors
    Theme.GRAY -> GrayColors
    Theme.COOL -> CoolColors
  }
}


@Composable
fun AscoreTheme(
  theme: Theme = Theme.COOL,
  content: @Composable() () -> Unit
) {


  val coroutineScope = rememberCoroutineScope()
  val getColors: GetColors by inject()

  val colorState = remember {
    mutableStateOf(getColors().value)
  }

  LaunchedEffect(Unit) {
    coroutineScope.launch {
      getColors().collectLatest { colors ->
        colorState.value = colors
      }
    }
  }
  MaterialTheme(
    colors = colorState.value,
    typography = typographyDark,
    shapes = shapes,
    content = content
  )
}

@Composable
fun PopupTheme(
  content: @Composable() () -> Unit
) {
  val colors = with(MaterialTheme.colors) {
    copy(surface = Color.White, onSurface = Color.Black)
  }

  MaterialTheme(
    colors = colors,
    typography = typographyContrast,
    shapes = shapes,
    content = content
  )
}

@Composable
fun SeriousTheme(
  content: @Composable() () -> Unit
) {
  val colors = GrayColors

  MaterialTheme(
    colors = colors,
    typography = typographyContrast,
    shapes = shapes,
    content = content
  )
}

@Composable
fun PopupBorder(content: @Composable() () -> Unit) {
  ThemeBox(
    Modifier.wrapContentSize(),
    border = BorderStroke(2.dp, MaterialTheme.colors.primary),
    shape = RoundedCornerShape(5.dp)
  ) {
    ThemeBox(
      Modifier
        .wrapContentSize()
        .testTag("SettingsPopup"),
      shape = RoundedCornerShape(5.dp),
      padding = block(0.5)
    ) {
      content()
    }
  }
}