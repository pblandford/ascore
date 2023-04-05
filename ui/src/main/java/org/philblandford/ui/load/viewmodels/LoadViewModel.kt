package org.philblandford.ui.load.viewmodels

import FileInfo
import com.philblandford.kscore.engine.types.FileSource
import org.philblandford.ascore2.features.load.usecases.DeleteScore
import org.philblandford.ascore2.features.load.usecases.GetSavedScores
import org.philblandford.ascore2.features.load.usecases.LoadScore
import org.philblandford.ascore2.util.ok
import org.philblandford.ui.base.viewmodel.BaseViewModel
import org.philblandford.ui.base.viewmodel.VMInterface
import org.philblandford.ui.base.viewmodel.VMModel
import org.philblandford.ui.base.viewmodel.VMSideEffect
import timber.log.Timber

data class LoadModel(
  val fileNames: Map<FileSource, List<FileInfo>>
) : VMModel()

interface LoadInterface : VMInterface {
  fun load(fileInfo: FileInfo)
  fun delete(fileInfo: FileInfo)
}


class LoadViewModel(
  private val loadScore: LoadScore,
  private val getSavedScores: GetSavedScores,
  private val deleteScore: DeleteScore
) :
  BaseViewModel<LoadModel, LoadInterface, VMSideEffect>(), LoadInterface {
  override suspend fun initState(): Result<LoadModel> {
    val scores = getSavedScores()
    Timber.e("SCORES $scores")
    return LoadModel(getSavedScores()).ok()
  }

  override fun getInterface() = this

  override fun load(fileInfo: FileInfo) {
    loadScore(fileInfo.name, fileInfo.fileSource)
  }

  override fun delete(fileInfo: FileInfo) {
    deleteScore(fileInfo)
    update { copy(fileNames = getSavedScores()) }
  }
}