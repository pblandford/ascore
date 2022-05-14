package org.philblandford.ui.theme

import android.annotation.SuppressLint
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Colors
import androidx.compose.material.MaterialTheme
import androidx.compose.material.darkColors
import androidx.compose.material.lightColors
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.philblandford.ascore.android.ui.style.*
import org.philblandford.ui.common.block
import org.philblandford.ui.util.ThemeBox

enum class Theme {
  BLUE,
  RED,
  GREEN,
  BLACK,
  VIOLET,
  GRAY
}

private val BlueColors = darkColors(
  primary = darkBlue,
  primaryVariant = darkBlue2,
  secondary = lightBlue2,
  background = darkBlue,
  surface = darkBlue,
  onPrimary = Color.White,
  onSecondary = Color.Black,
  onBackground = Color.White,
  onSurface = Color.White
)

private val RedColors = darkColors(
  primary = darkRed,
  primaryVariant = darkRed2,
  secondary = lightRed,
  background = darkRed,
  surface = darkRed,
  onPrimary = Color.White,
  onSecondary = Color.Black,
  onBackground = Color.White,
  onSurface = Color.White
)

private val BlackColors = darkColors(
  primary = Color.Black,
  primaryVariant = Color.Gray,
  secondary = veryLightGray,
  background = Color.Black,
  surface = Color.Black,
  onPrimary = Color.White,
  onSecondary = Color.Black,
  onBackground = Color.White,
  onSurface = Color.White
)

@SuppressLint("ConflictingOnColor")
private val GreenColors = darkColors(
  primary = darkGreen,
  primaryVariant = lightGreen2,
  secondary = lightGreen2,
  background = darkGreen,
  surface = darkGreen,
  onPrimary = Color.White,
  onSecondary = Color.Black,
  onBackground = Color.White,
  onSurface = Color.White
)

private val VioletColors = darkColors(
  primary = violet,
  primaryVariant = violet2,
  secondary = darkViolet,
  background = violet,
  surface = violet,
  onPrimary = Color.White,
  onSecondary = Color.Black,
  onBackground = Color.White,
  onSurface = Color.White
)

private val DarkColors = darkColors(
  primary = darkBlue,
  primaryVariant = darkBlue2,
  secondary = teal200,
  background = darkBlue,
  surface = darkBlue,
  onPrimary = Color.White,
  onSecondary = Color.Black,
  onBackground = Color.White,
  onSurface = Color.White
)

private val LightColors = lightColors(
  primary = veryLightGray,
  primaryVariant = lightBlueVariant,
  secondary = veryLightGray2,
  background = Color.White,
  surface = Color.White,
  onPrimary = Color.Black,
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
  background = veryLightGray2,
  surface = veryLightGray2,
  onPrimary = Color.Black,
  onSecondary = Color.Black,
  onBackground = Color.Black,
  onSurface = Color.Black
)

private fun getColors(theme: Theme): Colors {
  return when (theme) {
    Theme.BLUE -> BlueColors
    Theme.RED -> RedColors
    Theme.GREEN -> GreenColors
    Theme.BLACK -> BlackColors
    Theme.VIOLET -> VioletColors
    Theme.GRAY -> GrayColors
  }
}


@Composable
fun AscoreTheme(
  theme: Theme = Theme.BLUE,
  content: @Composable() () -> Unit
) {
  val colors = getColors(theme)

  MaterialTheme(
    colors = colors,
    typography = typographyDark,
    shapes = shapes,
    content = content
  )
}

@Composable
fun PopupTheme(
  content: @Composable() () -> Unit
) {
  val colors = ContrastColors

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
      Modifier.wrapContentSize().testTag("SettingsPopup"),
      shape = RoundedCornerShape(5.dp),
      padding = block(0.5)
    ) {
      content()
    }
  }
}