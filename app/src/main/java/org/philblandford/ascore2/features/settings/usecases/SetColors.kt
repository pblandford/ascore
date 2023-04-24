package org.philblandford.ascore2.features.settings.usecases

import androidx.compose.material3.ColorScheme


interface SetColors {
  operator fun invoke(func: ColorScheme.()->ColorScheme)
}