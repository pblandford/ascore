package org.philblandford.ascore2.features.startup

import com.philblandford.kscore.api.KScore
import com.philblandford.kscore.api.ProgressFunc
import org.philblandford.ascore2.android.billing.BillingManager
import org.philblandford.ascore2.features.file.AutoSave
import org.philblandford.ascore2.features.load.usecases.InstallTemplates
import timber.log.Timber

class StartupManager(private val autoSave: AutoSave, private val kScore: KScore,
private val installTemplates: InstallTemplates,
private val billingManager: BillingManager) {

  fun start(progressFunc: ProgressFunc = { _, _, _ -> false }) {
    installTemplates()
    billingManager.start()
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