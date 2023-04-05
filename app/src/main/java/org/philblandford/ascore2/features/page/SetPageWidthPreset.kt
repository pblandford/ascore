package org.philblandford.ascore2.features.page

import com.philblandford.kscore.engine.types.PageSize

interface SetPageWidthPreset {
  operator fun invoke(pageWidth:PageSize)
}