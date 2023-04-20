package org.philblandford.ascore2.features.edit

import com.philblandford.kscore.api.KScore

class AdjustSelectedLineStartImpl(private val kScore: KScore) : AdjustSelectedLineStart {
  override fun invoke(back: Boolean) {
    kScore.getSelectedArea()?.let { area ->
      if (area.event.isLine()) {

      }
    }
  }
}