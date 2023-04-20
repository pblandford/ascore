package org.philblandford.ascore2.features.settings.usecases

import ResourceManager

class GetAvailableFontsImpl(private val resourceManager: ResourceManager) : GetAvailableFonts {
    override fun invoke(): List<String> {
        return resourceManager.getTextFonts()
    }
}