package org.philblandford.ascore2.features.edit

import com.philblandford.kscore.api.KScore

class MoveSelectedAreaImpl(private val kScore: KScore) : MoveSelectedArea {
  override fun invoke(x: Int, y: Int) {
    kScore.moveSelectedArea(x, y)
  }
}