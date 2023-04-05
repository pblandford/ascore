package org.philblandford.ascore2.features.startup

import com.philblandford.kscore.api.KScore
import org.philblandford.ascore2.features.file.AutoSave
import timber.log.Timber

class StartupManager(private val autoSave: AutoSave, private val kScore: KScore) {

  fun start() {
    autoSave.tryAutoSave()?.let { autoSave ->
      try {
        kScore.setScore(autoSave)
      } catch (e:Exception) {
        Timber.e(e)
        kScore.createDefaultScore()
      }
    } ?: run {
      kScore.createDefaultScore()
    }
    autoSave.startAutoSave()
  }
}