package org.philblandford.ui.insert.items.tuplet.viewmodel

import com.philblandford.kscore.engine.types.EventParam
import com.philblandford.kscore.engine.types.ParamMap
import org.philblandford.ascore2.features.ui.usecases.GetInsertItem
import org.philblandford.ascore2.features.ui.usecases.InsertItemMenu
import org.philblandford.ascore2.features.ui.usecases.UpdateInsertItem
import org.philblandford.ascore2.util.ok
import org.philblandford.ui.R
import org.philblandford.ui.insert.common.viewmodel.InsertViewModel
import org.philblandford.ui.insert.model.InsertInterface
import org.philblandford.ui.insert.model.InsertModel

data class TupletInsertModel(
  val minNumerator: Int = 2,
  val maxNumerator: Int = 32,
  override val params: Map<EventParam, Any?> = mapOf(
    EventParam.NUMERATOR to 3,
    EventParam.HIDDEN to false
  )
) : InsertModel(R.string.tuplet, "", params)

interface TupletInsertInterface : InsertInterface<TupletInsertModel>

class TupletInsertViewModel(
  private val updateInsertItem: UpdateInsertItem,
  private val getInsertItem: GetInsertItem,
  insertItemMenu: InsertItemMenu

) : InsertViewModel<TupletInsertModel, TupletInsertInterface>(updateInsertItem,
  getInsertItem, insertItemMenu), TupletInsertInterface {
  override suspend fun initState(): Result<TupletInsertModel> {
    return TupletInsertModel().ok()
  }

  override fun getInterface() = this

  override fun TupletInsertModel.updateParams(params: ParamMap): TupletInsertModel {
    return copy(params = params)
  }
}