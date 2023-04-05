package org.philblandford.ui.insert.common.viewmodel

import com.philblandford.kscore.engine.types.ParamMap
import org.philblandford.ascore2.features.ui.model.InsertItem
import org.philblandford.ascore2.util.ok
import org.philblandford.ui.insert.model.DefaultInsertInterface
import org.philblandford.ui.insert.model.InsertInterface
import org.philblandford.ui.insert.model.InsertModel


open class DefaultInsertViewModel: InsertViewModel<InsertModel, DefaultInsertInterface>(), DefaultInsertInterface {

  override suspend fun initState(): Result<InsertModel> {
    return InsertModel().ok()
  }

  override fun getInterface() = this

}