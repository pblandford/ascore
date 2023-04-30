package org.philblandford.ui.insert.choose.viewmodel

import org.philblandford.ascore2.features.ui.model.InsertItem
import org.philblandford.ascore2.features.ui.usecases.SelectInsertItem
import org.philblandford.ascore2.util.ok
import org.philblandford.ui.base.viewmodel.BaseViewModel
import org.philblandford.ui.base.viewmodel.VMInterface
import org.philblandford.ui.base.viewmodel.VMModel
import org.philblandford.ui.base.viewmodel.VMSideEffect

data class InsertChooseModel(
  val page: Int,
  val items: List<InsertItem>,
  val searchItems: List<InsertItem>,
  val showNext: Boolean
) : VMModel()

enum class GroupSize {
  COMPACT,
  MEDIUM,
  EXPANDED
}

interface InsertChooseInterface : VMInterface {
  fun select(item: InsertItem)
  fun nextPage()
  fun helpText(string: String)
  fun setGroupSize(groupSize: GroupSize)
}

class InsertChooseViewModel(private val selectInsertItem: SelectInsertItem) :
  BaseViewModel<InsertChooseModel, InsertChooseInterface, VMSideEffect>(), InsertChooseInterface {
  private var grouped = groupItems(GroupSize.COMPACT)

  override val resetOnLoad = false

  override suspend fun initState(): Result<InsertChooseModel> {
    return InsertChooseModel(0, grouped[0], insertItems, true).ok()
  }

  override fun getInterface() = this

  override fun helpText(string: String) {
  }

  override fun select(item: InsertItem) {
    selectInsertItem(item)
  }

  override fun nextPage() {
    update {
      val newPage = (page + 1) % grouped.size
      val items = grouped[newPage]
      copy(page = newPage, items = items)
    }
  }

  override fun setGroupSize(groupSize: GroupSize) {
    grouped = groupItems(groupSize)
    update { copy(items = grouped[0], showNext = grouped.size > 1) }
  }

  private fun groupItems(groupSize: GroupSize): List<List<InsertItem>> {
    val perPage = when (groupSize) {
      GroupSize.COMPACT -> 12
      GroupSize.MEDIUM -> 18
      GroupSize.EXPANDED -> 36
    }
    return insertItems.chunked(perPage)
  }

}
