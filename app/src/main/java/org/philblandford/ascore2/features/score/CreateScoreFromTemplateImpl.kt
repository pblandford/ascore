package org.philblandford.ascore2.features.score

import ResourceManager
import com.philblandford.kscore.api.KScore
import com.philblandford.kscore.engine.types.FileSource
import com.philblandford.kscore.saveload.Loader

class CreateScoreFromTemplateImpl(private val resourceManager: ResourceManager,
                                  private val loader:Loader,
private val kScore: KScore) : CreateScoreFromTemplate {
  override fun invoke(template: String) {
    resourceManager.loadScore(template, FileSource.TEMPLATE)?.let { bytes ->
      loader.createScoreFromBytes(bytes)?.let { bytes ->
        kScore.setScore(bytes)
      }
    }
  }
}