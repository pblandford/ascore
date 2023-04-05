package org.philblandford.ascore2.features.load.usecases

import ResourceManager
import com.philblandford.kscore.api.KScore
import com.philblandford.kscore.engine.types.FileSource
import com.philblandford.kscore.saveload.Loader
import com.philblandford.kscore.saveload.Saver

class LoadScoreImpl(
  private val resourceManager: ResourceManager,
  private val loader: Loader,
  private val kScore: KScore
) : LoadScore {
  override operator fun invoke(name: String, fileSource: FileSource) {
    resourceManager.loadScore(name, fileSource)?.let { bytes ->
      loader.createScoreFromBytes(bytes)?.let { score ->
        kScore.setScore(score)
      }
    }
  }
}