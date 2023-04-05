package org.philblandford.ascore2.features.settings.usecases

import androidx.compose.material.Colors

interface SetColors {
  operator fun invoke(func: Colors.()->Colors)
}