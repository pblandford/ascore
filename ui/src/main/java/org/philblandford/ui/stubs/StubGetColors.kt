package org.philblandford.ui.stubs

import androidx.compose.material3.ColorScheme
import androidx.compose.material3.lightColorScheme
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import org.philblandford.ascore2.features.settings.usecases.GetColors

class StubGetColors : GetColors {
  private val colors = MutableStateFlow(lightColorScheme())

  override fun invoke(): StateFlow<ColorScheme> {
    return colors
  }
}