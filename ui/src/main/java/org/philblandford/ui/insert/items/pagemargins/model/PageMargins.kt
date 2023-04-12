package org.philblandford.ui.insert.items.pagemargins.model

import com.philblandford.kscore.engine.types.EventParam
import org.philblandford.ui.insert.model.InsertModel

data class MarginDescriptor(
  val param: EventParam,
  val current: Int,
  val textId: Int
)

data class PageMarginsModel(
  val left: MarginDescriptor,
  val right: MarginDescriptor,
  val top: MarginDescriptor,
  val bottom: MarginDescriptor,
  val min:Int = 0,
  val max:Int = 800,
  val step:Int = 25,
) : InsertModel()
