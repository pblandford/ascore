package org.philblandford.ascore2.features.edit

import com.philblandford.kscore.api.KScore
import com.philblandford.kscore.engine.types.EventParam

class MoveSelectedAreaImpl(private val kScore: KScore) : MoveSelectedArea {
  override fun invoke(x: Int, y: Int, param: EventParam) {
    kScore.moveSelectedArea(x, y, param)
  }
}