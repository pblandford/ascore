package org.philblandford.ascore2.features.load.usecases

import FileInfo
import ResourceManager
import com.philblandford.kscore.engine.types.FileSource

class GetSavedScoresImpl(private val resourceManager: ResourceManager) : GetSavedScores {
  override operator fun invoke():Map<FileSource,List<FileInfo>> {
    val saved = resourceManager.getSavedFiles(FileSource.SAVE).sortedBy { -it.accessTime }
    val auto = resourceManager.getSavedFiles(FileSource.AUTOSAVE).sortedBy { -it.accessTime }
    val external = resourceManager.getSavedFiles(FileSource.EXTERNAL).sortedBy { -it.accessTime }
    val template = resourceManager.getSavedFiles(FileSource.TEMPLATE).sortedBy { -it.accessTime }
    return mapOf(
      FileSource.SAVE to saved,
      FileSource.AUTOSAVE to auto,
      FileSource.EXTERNAL to external,
      FileSource.TEMPLATE to template
    )
  }
}