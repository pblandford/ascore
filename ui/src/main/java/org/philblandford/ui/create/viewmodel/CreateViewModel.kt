package org.philblandford.ui.create.viewmodel

import com.philblandford.kscore.api.NewScoreDescriptor
import com.philblandford.kscore.engine.types.MetaType
import org.philblandford.ascore2.util.ok
import org.philblandford.ui.base.viewmodel.BaseViewModel
import org.philblandford.ui.base.viewmodel.VMInterface
import org.philblandford.ui.base.viewmodel.VMModel
import org.philblandford.ui.base.viewmodel.VMSideEffect

data class CreateModel(
  val newScoreDescriptor: NewScoreDescriptor
) : VMModel()

interface CreateInterface : VMInterface {
  fun setTitle(title:String)
}

class CreateViewModel : BaseViewModel<CreateModel, CreateInterface, VMSideEffect>(),
CreateInterface {
  override suspend fun initState(): Result<CreateModel> {
    return CreateModel(NewScoreDescriptor()).ok()
  }

  override fun getInterface() = this

  override fun setTitle(title: String) {
    updateScore { copy(meta = meta.setText(MetaType.TITLE, title)) }
  }

  private fun updateScore(func:NewScoreDescriptor.()->NewScoreDescriptor) {
    update { copy(newScoreDescriptor = newScoreDescriptor.func()) }
  }
}