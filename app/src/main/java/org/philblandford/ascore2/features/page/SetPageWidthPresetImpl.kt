package org.philblandford.ascore2.features.page

import com.philblandford.kscore.api.KScore
import com.philblandford.kscore.engine.types.PageSize

class SetPageWidthPresetImpl(private val kScore: KScore) : SetPageWidthPreset {
  override fun invoke(pageWidth: PageSize) {
    kScore.setPageWidth(pageWidth)
  }
}