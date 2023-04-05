package org.philblandford.ascore2.features.settings.usecases

import androidx.compose.material.Colors
import kotlinx.coroutines.flow.StateFlow

interface GetColors {
  operator fun invoke():StateFlow<Colors>
}