package org.philblandford.ui.imports.viewmodel

import android.net.Uri
import androidx.core.net.toFile
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.philblandford.ascore2.features.load.usecases.ImportScore
import org.philblandford.ascore2.features.startup.StartupManager
import org.philblandford.ascore2.util.ok
import org.philblandford.ui.base.viewmodel.BaseViewModel
import org.philblandford.ui.base.viewmodel.VMInterface
import org.philblandford.ui.base.viewmodel.VMSideEffect
import org.philblandford.ui.imports.model.ImportModel
import timber.log.Timber

interface ImportInterface : VMInterface {
  fun import(uri: Uri)
  fun start()
}

sealed class ImportSideEffect : VMSideEffect() {
  object Complete : ImportSideEffect()
  data class Error(val exception: Exception) : ImportSideEffect()
}

class ImportViewModel(private val importScore: ImportScore,
private val startupManager: StartupManager) :
  BaseViewModel<ImportModel, ImportInterface, VMSideEffect>(), ImportInterface {

  override suspend fun initState(): Result<ImportModel> {
    return ImportModel("", "", "").ok()
  }

  override fun getInterface(): ImportInterface = this

  override fun import(uri: Uri) {
    CoroutineScope(Dispatchers.IO).launch {

      try {
        importScore(uri) { n, t, s, p ->
          update {
            copy(name = n, action = t, subAction = s, progress = p)
          }
        }
        launchEffect(ImportSideEffect.Complete)
      } catch (e: Exception) {
        launchEffect(ImportSideEffect.Error(e))
      }
    }
  }

  override fun start() {
    CoroutineScope(Dispatchers.IO).launch {
      startupManager.start() { t,s,p ->
        update {
          copy(action = t, progress = p)
        }
        false
      }
      launchEffect(ImportSideEffect.Complete)
    }
  }
}