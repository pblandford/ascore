package org.philblandford.ui.layout.model

import com.philblandford.kscore.engine.types.EventParam
import org.philblandford.ui.base.viewmodel.VMModel

data class LayoutOption<T>(val string: Int, val param: EventParam, val value: T)
data class LayoutOptionModel(
  val numFixedBars: Int,
  val options: List<LayoutOption<*>>
) : VMModel()