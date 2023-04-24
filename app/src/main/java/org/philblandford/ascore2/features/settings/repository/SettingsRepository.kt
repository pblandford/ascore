package org.philblandford.ascore2.features.settings.repository

import ResourceManager
import android.provider.CalendarContract.Colors
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.ui.graphics.Color
import com.philblandford.kscore.api.KScore
import com.philblandford.kscore.engine.core.area.factory.TextType
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import org.philblandford.ascore2.features.settings.SettingsDataSource

private val CoolColors =  lightColorScheme(
    surface = Color(0xffe6e6e6),
    onSurface = Color(0xff4d3900),
    secondary = Color(0xffbbbbbb)
)

private data class ColorStore(val surface: ULong, val onSurface: ULong, val secondary: ULong)
private data class FontStore(val map: Map<TextType, String>)

class SettingsRepository(private val settingsDataSource: SettingsDataSource) {

    private val colorsKey = "Colors"
    private val fontKey = "Fonts"
    private val coroutineScope = CoroutineScope(Dispatchers.Default)
    private val colors =
        MutableStateFlow(CoolColors)

    init {
        settingsDataSource.getObject(colorsKey, ColorStore::class.java)?.let { stored ->
            setColors {
                lightColorScheme(
                    surface = Color(stored.surface),
                    onSurface = Color(stored.onSurface),
                    secondary = Color(stored.secondary),
                    primary = Color(stored.onSurface),
                    onPrimary = Color(stored.surface)
                )
            }
        }
    }

    fun setColors(func: ColorScheme.() -> ColorScheme) {
        coroutineScope.launch {
            val newColors = colors.value.func()
            colors.emit(newColors)
            settingsDataSource.storeObject(
                colorsKey, ColorStore(
                    newColors.surface.value,
                    newColors.onSurface.value, newColors.secondary.value
                )
            )
        }
    }

    fun getColors(): StateFlow<ColorScheme> = colors

    fun setFont(textType: TextType, font: String) {
        val newFonts = getFonts() + (textType to font)
        settingsDataSource.storeObject(fontKey, FontStore(newFonts))
    }

    fun getFonts(): Map<TextType, String> {
       return settingsDataSource.getObject(fontKey, FontStore::class.java)?.map
           ?: TextType.values().associateWith { it.getFont() }
    }

    private fun TextType.getFont():String {
        return when (this) {
            TextType.SYSTEM -> "tempo"
            TextType.EXPRESSION -> "expression"
            TextType.TITLE -> "default"
            TextType.SUBTITLE -> "default"
            TextType.COMPOSER -> "default"
            TextType.LYRICIST -> "default"
            TextType.LYRIC -> "default"
            TextType.HARMONY -> "default"
            TextType.DEFAULT -> "default"
        }
    }
}