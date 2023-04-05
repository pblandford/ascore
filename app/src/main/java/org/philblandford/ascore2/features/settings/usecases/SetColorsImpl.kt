package org.philblandford.ascore2.features.settings.usecases

import androidx.compose.material.Colors
import org.philblandford.ascore2.features.settings.repository.SettingsRepository

class SetColorsImpl(private val settingsRepository: SettingsRepository) : SetColors {
  override fun invoke(func: Colors.() -> Colors) {
    settingsRepository.setColors(func)
  }
}