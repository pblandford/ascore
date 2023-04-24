package org.philblandford.ascore2.features.settings.usecases

import androidx.compose.material3.ColorScheme
import kotlinx.coroutines.flow.StateFlow

interface GetColors {
  operator fun invoke():StateFlow<ColorScheme>
}