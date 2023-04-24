package org.philblandford.ui.settings.model

import androidx.compose.material3.ColorScheme
import com.philblandford.kscore.engine.core.area.factory.TextType
import org.philblandford.ui.base.viewmodel.VMModel


data class SettingsModel(
    val colors: ColorScheme,
    val fonts: List<String>,
    val assignedFonts: Map<TextType, String>,
    val defaultFont: String
) : VMModel()
