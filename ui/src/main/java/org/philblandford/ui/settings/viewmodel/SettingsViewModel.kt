package org.philblandford.ui.settings.viewmodel

import androidx.compose.ui.graphics.Color
import com.godaddy.android.colorpicker.HsvColor
import org.philblandford.ascore2.features.settings.usecases.GetColors
import org.philblandford.ascore2.features.settings.usecases.SetColors
import org.philblandford.ascore2.util.ok
import org.philblandford.ui.base.viewmodel.BaseViewModel
import org.philblandford.ui.base.viewmodel.VMInterface
import org.philblandford.ui.base.viewmodel.VMSideEffect
import org.philblandford.ui.settings.model.SettingsModel

interface SettingsInterface : VMInterface {
  fun setBackgroundColor(color: HsvColor)
  fun setForegroundColor(color: HsvColor)
}

class SettingsViewModel(private val getColors: GetColors,
private val setColors: SetColors) : BaseViewModel<SettingsModel, SettingsInterface, VMSideEffect>(), SettingsInterface {
  override suspend fun initState(): Result<SettingsModel> {
    return SettingsModel(getColors().value).ok()
  }

  override fun getInterface(): SettingsInterface = this

  override fun setBackgroundColor(color: HsvColor) {
    val secondary = color.copy(saturation = color.saturation * 0.6f)
    val secondaryVariant = color.copy(saturation = 1f)
    setColors{ copy(surface = color.toColor(), secondary = secondary.toColor(),
      secondaryVariant = secondaryVariant.toColor())}
  }

  override fun setForegroundColor(color: HsvColor) {
    setColors{ copy(onSurface = color.toColor(), primary = color.toColor()) }
  }
}