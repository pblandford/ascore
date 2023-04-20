package org.philblandford.ascore2.features.settings.usecases

import com.philblandford.kscore.api.KScore
import com.philblandford.kscore.engine.core.area.factory.TextType
import org.philblandford.ascore2.features.settings.repository.SettingsRepository

class SetFontImpl(private val settingsRepository: SettingsRepository) : SetFont {

    override fun invoke(textType: TextType, font: String) {
        settingsRepository.setFont(textType, font)
    }
}