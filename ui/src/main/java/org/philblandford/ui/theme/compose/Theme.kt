package org.philblandford.ui.theme.compose

import androidx.compose.material3.ColorScheme
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.graphics.Color
import org.philblandford.ui.theme.shapes
import org.philblandford.ui.theme.typographyContrast
import org.philblandford.ui.theme.typographyDark

@Composable
fun AscoreTheme(
  colorScheme: ColorScheme = MaterialTheme.colorScheme,
  content: @Composable() () -> Unit
) {
    MaterialTheme(
      colorScheme = colorScheme,
      typography = typographyDark,
      shapes = shapes,

    ) {
      CompositionLocalProvider (
        LocalContentColor provides MaterialTheme.colorScheme.onSurface) {
        content()
    }
  }
}

@Composable
fun PopupTheme(
  content: @Composable() () -> Unit
) {
  val colors = with(MaterialTheme.colorScheme) {
    copy(surface = Color.White, onSurface = Color.Black)
  }

  MaterialTheme(
    colorScheme = colors,
    typography = typographyContrast,
    shapes = shapes,
    content = content
  )
}

