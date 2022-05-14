package org.philblandford.ui.insert.choose.viewmodel

import org.philblandford.ascore2.util.ok
import org.philblandford.ui.base.viewmodel.BaseViewModel
import org.philblandford.ui.base.viewmodel.VMInterface
import org.philblandford.ui.base.viewmodel.VMModel
import org.philblandford.ui.base.viewmodel.VMSideEffect
import org.philblandford.ascore2.features.ui.model.InsertItem
import org.philblandford.ascore2.features.ui.usecases.SelectInsertItem
import org.philblandford.ui.insert.viewmodel.insertItems

data class InsertChooseModel(
  val page: Int,
  val items: List<InsertItem>,
  val searchItems: List<InsertItem>,
  val showNext: Boolean
) : VMModel()

interface InsertChooseInterface : VMInterface {
  fun select(item: InsertItem)
  fun nextPage()
  fun helpText(string: String)
}

class InsertChooseViewModel(private val selectInsertItem: SelectInsertItem) :
  BaseViewModel<InsertChooseModel, InsertChooseInterface, VMSideEffect>(), InsertChooseInterface {
  private val grouped = groupItems()

  override suspend fun initState(): Result<InsertChooseModel> {
    return InsertChooseModel(0, grouped[0], listOf(), true).ok()
  }

  override fun getInterface() = this

  override fun helpText(string: String) {
    TODO("Not yet implemented")
  }

  override fun select(item: InsertItem) {
    selectInsertItem(item)
  }

  override fun nextPage() {
    update {
      val newPage = (page + 1) % grouped.size
      copy(page = newPage, items = grouped[newPage])
    }
  }

  private fun groupItems(): List<List<InsertItem>> {
    val perPage = if (true) {
      if (false) {
        24
      } else {
        14
      }
    } else {
      if (false) {
        36
      } else {
        28
      }
    }
    return insertItems.chunked(perPage)
  }

}
