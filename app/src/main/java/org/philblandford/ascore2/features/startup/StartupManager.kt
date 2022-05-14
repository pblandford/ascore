package org.philblandford.ascore2.features.startup

import com.philblandford.kscore.api.KScore
import org.philblandford.ascore2.features.file.AutoSave

class StartupManager(private val autoSave: AutoSave, private val kScore: KScore) {

  fun start() {
    autoSave.tryAutoSave()?.let { autoSave ->
      kScore.setScore(autoSave)
    } ?: run {
      kScore.createDefaultScore()
    }
    autoSave.startAutoSave()
  }
}