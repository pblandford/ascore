package org.philblandford.ui.settings.model

import androidx.compose.material.Colors
import com.philblandford.kscore.engine.core.area.factory.TextType
import org.philblandford.ui.base.viewmodel.VMModel


data class SettingsModel(
    val colors: Colors,
    val fonts: List<String>,
    val assignedFonts: Map<TextType, String>,
    val defaultFont: String
) : VMModel()
