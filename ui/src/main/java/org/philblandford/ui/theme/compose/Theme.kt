package org.philblandford.ui.theme

import androidx.compose.material3.ColorScheme
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.graphics.Color
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import org.koin.androidx.compose.inject
import org.philblandford.ascore2.features.settings.usecases.GetColors
import org.philblandford.ui.stubs.StubGetColors

@Composable
fun AscoreTheme(
  colorScheme: ColorScheme = MaterialTheme.colorScheme,
  content: @Composable() () -> Unit
) {
  CompositionLocalProvider (
    LocalContentColor provides MaterialTheme.colorScheme.onSurface) {

    MaterialTheme(
      colorScheme = colorScheme,
      typography = typographyDark,
      shapes = shapes,
      content = content
    )
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

