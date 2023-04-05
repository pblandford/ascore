package org.philblandford.ui.insert.items.transposeby.viewmodel

import com.philblandford.kscore.engine.types.EventType
import com.philblandford.kscore.engine.types.eZero
import org.philblandford.ascore2.features.insert.InsertEvent
import org.philblandford.ascore2.util.ok
import org.philblandford.ui.insert.common.viewmodel.DefaultInsertViewModel
import org.philblandford.ui.insert.common.viewmodel.InsertViewModel
import org.philblandford.ui.insert.model.DefaultInsertInterface
import org.philblandford.ui.insert.model.InsertInterface
import org.philblandford.ui.insert.model.InsertModel

interface TransposeInterface : DefaultInsertInterface {
  fun go()
}

class TransposeViewModel(private val insertEvent: InsertEvent) : InsertViewModel<InsertModel, TransposeInterface>(), TransposeInterface {

  override suspend fun initState(): Result<InsertModel> {
    return InsertModel().ok()
  }

  override fun getInterface() = this

  override fun go() {
    getInsertState().value.insertItem?.let { item ->
      insertEvent(EventType.TRANSPOSE, eZero(), item.params)
    }
  }
}