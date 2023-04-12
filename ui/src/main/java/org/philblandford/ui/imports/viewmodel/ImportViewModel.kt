package org.philblandford.ui.imports.viewmodel

import android.net.Uri
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import org.philblandford.ascore2.features.load.usecases.ImportScore
import org.philblandford.ascore2.util.ok
import org.philblandford.ui.base.viewmodel.BaseViewModel
import org.philblandford.ui.base.viewmodel.VMInterface
import org.philblandford.ui.base.viewmodel.VMSideEffect
import org.philblandford.ui.imports.model.ImportModel
import timber.log.Timber

interface ImportInterface : VMInterface {
  fun import(uri: Uri)
}

class ImportViewModel(private val importScore: ImportScore) :
  BaseViewModel<ImportModel, ImportInterface, VMSideEffect>(), ImportInterface {

  override suspend fun initState(): Result<ImportModel> {
    return ImportModel("").ok()
  }

  override fun getInterface(): ImportInterface = this

  override fun import(uri: Uri) {
    viewModelScope.launch {
      importScore(uri) {
        Timber.e("FUCK YOU $it")
        update { copy(progress = it) }
      }
    }
  }
}