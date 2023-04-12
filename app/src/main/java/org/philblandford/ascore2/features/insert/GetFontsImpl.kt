package org.philblandford.ascore2.features.insert

import ResourceManager

class GetFontsImpl(private val resourceManager: ResourceManager) : GetFonts {
  override fun invoke(): List<String> {
    return resourceManager.getTextFonts()
  }
}