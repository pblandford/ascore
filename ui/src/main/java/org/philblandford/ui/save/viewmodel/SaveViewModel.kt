package org.philblandford.ui.save.viewmodel

import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import org.philblandford.ascore2.features.save.GetFileName
import org.philblandford.ascore2.features.save.GetTitle
import org.philblandford.ascore2.features.save.SaveScore
import org.philblandford.ascore2.features.score.ScoreUpdate
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
  private val getFileName: GetFileName,
  private val saveScore: SaveScore,
  private val update: ScoreUpdate
) :
  BaseViewModel<SaveModel, SaveInterface, VMSideEffect>(), SaveInterface {

  init {
    viewModelScope.launch {
      update().collectLatest {
        update { SaveModel(getFileName() ?: getTitle()) }
      }
    }
  }
  override suspend fun initState(): Result<SaveModel> {
    return SaveModel(getFileName() ?: getTitle()).ok()
  }

  override fun getInterface() = this

  override fun saveInternal(name: String) {
    receiveAction { model ->
      saveScore(name).map { model }
    }
  }
}