package org.philblandford.ui.insert.common.viewmodel

import com.philblandford.kscore.engine.types.ParamMap
import org.philblandford.ascore2.features.ui.usecases.InsertItemMenu
import org.philblandford.ascore2.features.ui.usecases.GetInsertItem
import org.philblandford.ascore2.features.ui.usecases.UpdateInsertItem
import org.philblandford.ascore2.util.ok
import org.philblandford.ui.insert.model.InsertInterface
import org.philblandford.ui.insert.model.InsertModel


class DefaultInsertViewModel(
  updateInsertItem: UpdateInsertItem,
  getInsertItem: GetInsertItem,
  private val insertItemMenu: InsertItemMenu
) : InsertViewModel<InsertModel, InsertInterface<InsertModel>>(updateInsertItem, getInsertItem,
insertItemMenu), InsertInterface<InsertModel> {


  override suspend fun initState(): Result<InsertModel> {
    return InsertModel(-1, "", mapOf()).ok()
  }

  override fun getInterface() = this

  override fun InsertModel.updateParams(params: ParamMap): InsertModel {
    return InsertModel(title, helpTag, params)
  }
}