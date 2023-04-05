package org.philblandford.ui.quickscore.viewmodel

import org.philblandford.ascore2.features.score.CreateDefaultScore
import org.philblandford.ascore2.util.ok
import org.philblandford.ui.base.viewmodel.BaseViewModel
import org.philblandford.ui.base.viewmodel.VMInterface
import org.philblandford.ui.base.viewmodel.VMModel
import org.philblandford.ui.base.viewmodel.VMSideEffect

interface QuickScoreInterface : VMInterface {
  fun create()
}

class QuickScoreViewModel(private val createDefaultScore: CreateDefaultScore) :
  BaseViewModel<VMModel, QuickScoreInterface, VMSideEffect>(), QuickScoreInterface {

  override suspend fun initState(): Result<VMModel> {
    return object : VMModel() {}.ok()
  }

  override fun getInterface(): QuickScoreInterface = this

  override fun create() {
    createDefaultScore()
  }
}