package org.philblandford.ascore2.features.settings.usecases

import com.philblandford.kscore.api.KScore
import com.philblandford.kscore.engine.core.area.factory.TextType
import com.philblandford.kscore.engine.types.EventParam
import org.philblandford.ascore2.features.settings.repository.SettingsRepository

class SetFontImpl(private val settingsRepository: SettingsRepository,
private val updateFontOptions: UpdateFontOptions) : SetFont {

    override fun invoke(textType: TextType, font: String) {
        settingsRepository.setFont(textType, font)
        updateFontOptions()
    }
}