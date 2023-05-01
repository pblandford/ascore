package org.philblandford.ui.createfromtemplate.viewmodel

import FileInfo
import com.philblandford.kscore.engine.types.FileSource
import org.philblandford.ascore2.features.load.usecases.DeleteScore
import org.philblandford.ascore2.features.load.usecases.GetSavedScores
import org.philblandford.ascore2.features.score.CreateScoreFromTemplate
import org.philblandford.ascore2.util.ok
import org.philblandford.ui.base.viewmodel.BaseViewModel
import org.philblandford.ui.base.viewmodel.VMInterface
import org.philblandford.ui.base.viewmodel.VMSideEffect
import org.philblandford.ui.createfromtemplate.model.CreateFromTemplateModel

interface CreateFromTemplateInterface : VMInterface {
  fun create(template: String)
  fun delete(template: FileInfo)
}

class CreateFromTemplateViewModel(
  private val getSavedScores: GetSavedScores,
  private val createScoreFromTemplate: CreateScoreFromTemplate,
  private val deleteScore: DeleteScore
) : BaseViewModel<CreateFromTemplateModel, CreateFromTemplateInterface, VMSideEffect>(),
  CreateFromTemplateInterface {
  override suspend fun initState(): Result<CreateFromTemplateModel> {
    return CreateFromTemplateModel(getScores()).ok()
  }

  override fun getInterface(): CreateFromTemplateInterface = this

  override fun create(template: String) {
    createScoreFromTemplate(template)
  }

  override fun delete(template: FileInfo) {
    deleteScore(template)
    update { copy(templates = getScores()) }
  }

  private fun getScores():List<FileInfo> {
    return getSavedScores()[FileSource.TEMPLATE] ?: listOf()

  }
}