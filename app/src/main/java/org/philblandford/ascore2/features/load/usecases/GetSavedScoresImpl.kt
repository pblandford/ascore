package org.philblandford.ascore2.features.load.usecases

import ResourceManager

class GetSavedScoresImpl(private val resourceManager: ResourceManager) : GetSavedScores {
  override operator fun invoke():List<String> {
    return resourceManager.getSavedFileNames()
  }
}