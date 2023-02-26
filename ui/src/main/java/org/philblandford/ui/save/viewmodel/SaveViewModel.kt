package org.philblandford.ui.save.viewmodel

import org.philblandford.ascore2.features.save.GetTitle
import org.philblandford.ascore2.features.save.SaveScore
import org.philblandford.ascore2.util.ok
import org.philblandford.ui.base.viewmodel.BaseViewModel
import org.philblandford.ui.base.viewmodel.VMInterface
import org.philblandford.ui.base.viewmodel.VMModel
import org.philblandford.ui.base.viewmodel.VMSideEffect

data class SaveModel(val scoreTitle:String) : VMModel()

interface SaveInterface : VMInterface {
  fun saveInternal(name: String)
}

class SaveViewModel(
  private val getTitle: GetTitle,
  private val saveScore: SaveScore
) :
  BaseViewModel<SaveModel, SaveInterface, VMSideEffect>(), SaveInterface {
  override suspend fun initState(): Result<SaveModel> {
    return SaveModel(getTitle()).ok()
  }

  override fun getInterface() = this

  override fun saveInternal(name: String) {
    receiveAction { model ->
      saveScore(name).map { model }
    }
  }
}