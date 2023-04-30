package org.philblandford.ui.load.viewmodels

import FileInfo
import androidx.lifecycle.viewModelScope
import com.philblandford.kscore.engine.types.FileSource
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.philblandford.ascore2.features.load.usecases.DeleteScore
import org.philblandford.ascore2.features.load.usecases.GetSavedScores
import org.philblandford.ascore2.features.load.usecases.LoadScore
import org.philblandford.ascore2.util.ok
import org.philblandford.ui.base.viewmodel.BaseViewModel
import org.philblandford.ui.base.viewmodel.VMInterface
import org.philblandford.ui.base.viewmodel.VMModel
import org.philblandford.ui.base.viewmodel.VMSideEffect
import timber.log.Timber

data class ProgressDescr(val title:String, val subtitle:String, val progress:Float)

data class LoadModel(
  val fileNames: Map<FileSource, List<FileInfo>>,
  val loadingScore:String? = null,
  val progress:ProgressDescr? = null
) : VMModel()

interface LoadInterface : VMInterface {
  fun load(fileInfo: FileInfo)
  fun delete(fileInfo: FileInfo)
}

sealed class LoadSideEffect : VMSideEffect() {
  object Done : LoadSideEffect()
}


class LoadViewModel(
  private val loadScore: LoadScore,
  private val getSavedScores: GetSavedScores,
  private val deleteScore: DeleteScore
) :
  BaseViewModel<LoadModel, LoadInterface, VMSideEffect>(), LoadInterface {
  override suspend fun initState(): Result<LoadModel> {
    return LoadModel(getSavedScores()).ok()
  }

  override fun getInterface() = this

  override fun load(fileInfo: FileInfo) {
    update { copy(loadingScore = fileInfo.name) }
    CoroutineScope(Dispatchers.IO).launch {
      loadScore(fileInfo.name, fileInfo.fileSource) { s, t, p ->
        update { copy(loadingScore = fileInfo.name, progress = ProgressDescr(s, t, p)) }
        false
      }
      launchEffect(LoadSideEffect.Done)
    }
  }

  override fun delete(fileInfo: FileInfo) {
    deleteScore(fileInfo)
    update { copy(fileNames = getSavedScores()) }
  }
}