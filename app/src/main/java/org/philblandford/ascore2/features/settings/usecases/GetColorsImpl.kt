package org.philblandford.ascore2.features.settings.usecases

import androidx.compose.material.Colors
import kotlinx.coroutines.flow.StateFlow
import org.philblandford.ascore2.features.settings.repository.SettingsRepository

class GetColorsImpl(private val settingsRepository: SettingsRepository) : GetColors {

  override fun invoke(): StateFlow<Colors> {
    return settingsRepository.getColors()
  }
}