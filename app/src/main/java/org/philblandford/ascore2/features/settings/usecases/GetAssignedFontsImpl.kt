package org.philblandford.ascore2.features.settings.usecases

import com.philblandford.kscore.engine.core.area.factory.TextType
import org.philblandford.ascore2.features.settings.repository.SettingsRepository

class GetAssignedFontsImpl(private val settingsRepository: SettingsRepository) : GetAssignedFonts {

    override fun invoke(): Map<TextType, String> {
        return settingsRepository.getFonts()
    }
}