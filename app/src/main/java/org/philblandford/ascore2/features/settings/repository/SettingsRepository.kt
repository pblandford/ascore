package org.philblandford.ascore2.features.settings.repository

import androidx.compose.material.Colors
import androidx.compose.material.MaterialTheme
import androidx.compose.material.lightColors
import androidx.compose.ui.graphics.Color
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import org.philblandford.ascore2.features.settings.SettingsDataSource

private val CoolColors = lightColors(
  surface = Color(0xffe6e6e6),
  onSurface = Color(0xff4d3900),
  secondary = Color(0xffbbbbbb)
)

private data class ColorStore(val surface:ULong, val onSurface:ULong, val secondary:ULong)

class SettingsRepository(private val settingsDataSource: SettingsDataSource) {

  private val colorsKey = "Colors"
  private val coroutineScope = CoroutineScope(Dispatchers.Default)
  private val colors =
    MutableStateFlow(CoolColors)

  init {
    settingsDataSource.getObject(colorsKey, ColorStore::class.java)?.let { stored ->
      setColors {
        lightColors(surface = Color(stored.surface), onSurface = Color(stored.onSurface),
        secondary = Color(stored.secondary), primary = Color(stored.onSurface))
      }
    }
  }

  fun setColors(func: Colors.() -> Colors) {
    coroutineScope.launch {
      val newColors = colors.value.func()
      colors.emit(newColors)
      settingsDataSource.storeObject(colorsKey, ColorStore(newColors.surface.value,
      newColors.onSurface.value, newColors.secondary.value))
    }
  }

  fun getColors(): StateFlow<Colors> = colors
}