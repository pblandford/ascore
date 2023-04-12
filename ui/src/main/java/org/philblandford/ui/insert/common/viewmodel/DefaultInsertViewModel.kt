package org.philblandford.ui.insert.common.viewmodel

import org.philblandford.ascore2.util.ok
import org.philblandford.ui.insert.model.DefaultInsertInterface
import org.philblandford.ui.insert.model.InsertModel


open class DefaultInsertViewModel: InsertViewModel<InsertModel, DefaultInsertInterface>(), DefaultInsertInterface {

  override suspend fun initState(): Result<InsertModel> {
    return InsertModel().ok()
  }

  override fun getInterface() = this

}