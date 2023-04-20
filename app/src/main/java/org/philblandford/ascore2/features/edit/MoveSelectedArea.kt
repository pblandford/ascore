package org.philblandford.ascore2.features.edit

import com.philblandford.kscore.engine.types.EventParam

interface MoveSelectedArea {
  operator fun invoke(x:Int, y:Int, param:EventParam = EventParam.HARD_START)
}