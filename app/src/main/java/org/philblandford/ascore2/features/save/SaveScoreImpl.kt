package org.philblandford.ascore2.features.save

import ResourceManager
import com.philblandford.kscore.api.KScore
import com.philblandford.kscore.engine.types.FileSource
import com.philblandford.kscore.saveload.Saver
import org.philblandford.ascore2.util.action

class SaveScoreImpl(private val kScore:KScore,
private val saver: Saver,
private val resourceManager: ResourceManager
                    ) : SaveScore {
  override operator fun invoke(name: String): Result<Unit> {
    return kScore.action {
      val bytes = saver.createSaveScore(this)
      resourceManager.saveScore(name, bytes, FileSource.SAVE)
    }
  }
}