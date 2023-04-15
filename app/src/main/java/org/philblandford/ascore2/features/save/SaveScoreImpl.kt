package org.philblandford.ascore2.features.save

import ResourceManager
import com.philblandford.kscore.api.KScore
import com.philblandford.kscore.engine.core.score.addEvent
import com.philblandford.kscore.engine.types.*
import com.philblandford.kscore.saveload.Saver
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.runBlocking
import org.philblandford.ascore2.util.action
import org.philblandford.ascore2.util.ok

class SaveScoreImpl(
  private val kScore: KScore,
  private val saver: Saver,
  private val resourceManager: ResourceManager
) : SaveScore {
  override operator fun invoke(name: String, fileSource: FileSource): Result<Unit> {

    kScore.addEvent(EventType.FILENAME, eZero(), paramMapOf(EventParam.TEXT to name))

    return kScore.action {
      this.addEvent(Event(EventType.FILENAME, paramMapOf(EventParam.TEXT to name)), eZero())
        ?.let { withFilename ->
          val bytes = saver.createSaveScore(withFilename)
          resourceManager.saveScore(name, bytes, fileSource)
        }
    }

  }
}