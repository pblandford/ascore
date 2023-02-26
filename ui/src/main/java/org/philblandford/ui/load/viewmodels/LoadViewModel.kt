package org.philblandford.ui.load.viewmodels

import org.philblandford.ascore2.features.load.usecases.GetSavedScores
import org.philblandford.ascore2.features.load.usecases.LoadScore
import org.philblandford.ascore2.util.ok
import org.philblandford.ui.base.viewmodel.BaseViewModel
import org.philblandford.ui.base.viewmodel.VMInterface
import org.philblandford.ui.base.viewmodel.VMModel
import org.philblandford.ui.base.viewmodel.VMSideEffect
import timber.log.Timber

data class LoadModel(
  val fileNames: List<String>
) : VMModel()

interface LoadInterface : VMInterface {
  fun load(name: String)
}


class LoadViewModel(private val loadScore: LoadScore, private val getSavedScores: GetSavedScores) :
  BaseViewModel<LoadModel, LoadInterface, VMSideEffect>(), LoadInterface {
  override suspend fun initState(): Result<LoadModel> {
    val scores = getSavedScores()
    Timber.e("SCORES $scores")
    return LoadModel(getSavedScores()).ok()
  }

  override fun getInterface() = this

  override fun load(name: String) {
    loadScore(name)
  }
}