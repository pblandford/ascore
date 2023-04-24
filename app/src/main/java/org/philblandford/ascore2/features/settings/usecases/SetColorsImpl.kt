package org.philblandford.ascore2.features.settings.usecases

import androidx.compose.material3.ColorScheme
import org.philblandford.ascore2.features.settings.repository.SettingsRepository

class SetColorsImpl(private val settingsRepository: SettingsRepository) : SetColors {
  override fun invoke(func: ColorScheme.() -> ColorScheme) {
    settingsRepository.setColors(func)
  }
}