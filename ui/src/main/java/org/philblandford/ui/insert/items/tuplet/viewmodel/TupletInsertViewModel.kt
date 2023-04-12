package org.philblandford.ui.insert.items.tuplet.viewmodel

import org.philblandford.ascore2.util.ok
import org.philblandford.ui.insert.common.viewmodel.InsertViewModel
import org.philblandford.ui.insert.items.tuplet.model.TupletInsertModel
import org.philblandford.ui.insert.model.InsertInterface


interface TupletInsertInterface : InsertInterface<TupletInsertModel>

class TupletInsertViewModel: InsertViewModel<TupletInsertModel, TupletInsertInterface>(), TupletInsertInterface {
  override suspend fun initState(): Result<TupletInsertModel> {
    return TupletInsertModel().ok()
  }

  override fun getInterface() = this

}