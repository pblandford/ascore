package org.philblandford.ui.util

import androidx.compose.material3.lightColorScheme
import androidx.compose.material3.windowsizeclass.WindowSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import org.philblandford.ui.main.window.LocalWindowSizeClass
import org.philblandford.ui.main.window.tabletWindowSize
import org.philblandford.ui.stubs.StubGetColors
import org.philblandford.ui.theme.AscoreTheme

@Composable
fun AScorePreview(size:WindowSizeClass = tabletWindowSize,
content: @Composable ()-> Unit) {
  AscoreTheme(lightColorScheme()) {
    CompositionLocalProvider(
      LocalWindowSizeClass provides size
    ) {
      content()
    }
  }
}