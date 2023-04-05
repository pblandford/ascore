package org.philblandford.ascore2.features.load.usecases

import FileInfo
import ResourceManager

class DeleteScoreImpl(private val resourceManager: ResourceManager) : DeleteScore {
  override fun invoke(fileInfo: FileInfo) {
    resourceManager.deleteScore(fileInfo.name, fileInfo.fileSource)
  }
}