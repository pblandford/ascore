package org.philblandford.ascore2.features.startup

import com.philblandford.kscore.api.KScore
import com.philblandford.kscore.api.ProgressFunc
import org.philblandford.ascore2.features.file.AutoSave
import timber.log.Timber

class StartupManager(private val autoSave: AutoSave, private val kScore: KScore) {

  fun start(progressFunc: ProgressFunc = { _, _, _ -> false }) {
    autoSave.tryAutoSave()?.let { autoSave ->
      try {
        kScore.setScore(autoSave, progressFunc)
      } catch (e: Exception) {
        Timber.e(e)
        kScore.createDefaultScore()
      }
    } ?: run {
      kScore.createDefaultScore()
    }
  }
}