package org.philblandford.ui.settings.viewmodel

import com.godaddy.android.colorpicker.HsvColor
import com.philblandford.kscore.engine.core.area.factory.TextType
import org.philblandford.ascore2.features.settings.usecases.GetAvailableFonts
import org.philblandford.ascore2.features.settings.usecases.GetColors
import org.philblandford.ascore2.features.settings.usecases.GetAssignedFonts
import org.philblandford.ascore2.features.settings.usecases.SetColors
import org.philblandford.ascore2.features.settings.usecases.SetFont
import org.philblandford.ascore2.util.ok
import org.philblandford.ui.base.viewmodel.BaseViewModel
import org.philblandford.ui.base.viewmodel.VMInterface
import org.philblandford.ui.base.viewmodel.VMSideEffect
import org.philblandford.ui.settings.model.SettingsModel

interface SettingsInterface : VMInterface {
    fun setBackgroundColor(color: HsvColor)
    fun setForegroundColor(color: HsvColor)
    fun setFont(textType: TextType, font: String)
}

class SettingsViewModel(
    private val getColors: GetColors,
    private val setColors: SetColors,
    private val getAssignedFonts: GetAssignedFonts,
    private val setFontUC: SetFont,
    private val getAvailableFonts: GetAvailableFonts
) : BaseViewModel<SettingsModel, SettingsInterface, VMSideEffect>(), SettingsInterface {
    override suspend fun initState(): Result<SettingsModel> {
       return updateFromSource().ok()
    }

    override fun getInterface(): SettingsInterface = this

    override fun setBackgroundColor(color: HsvColor) {
        val secondary = color.copy(saturation = color.saturation * 0.6f)
        val secondaryVariant = color.copy(saturation = 1f)
        setColors {
            copy(
                surface = color.toColor(),
                onPrimary = color.toColor(),
                secondary = secondary.toColor(),
                secondaryContainer = secondaryVariant.toColor()
            )
        }
        update{ updateFromSource() }
    }

    override fun setForegroundColor(color: HsvColor) {
        setColors { copy(onSurface = color.toColor(), primary = color.toColor()) }
        update { updateFromSource() }
    }

    override fun setFont(textType: TextType, font: String) {
        setFontUC(textType, font)
        update { updateFromSource() }
    }

    private fun updateFromSource():SettingsModel {
        return SettingsModel(
            getColors().value, getAvailableFonts(), getAssignedFonts(),
            "default"
        )
    }
 }