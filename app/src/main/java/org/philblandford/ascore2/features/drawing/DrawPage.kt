package org.philblandford.ascore2.features.drawing

import com.philblandford.kscore.engine.types.EventAddress

interface DrawPage {
  operator fun invoke(page:Int, playbackMarker:EventAddress?, vararg args:Any)
}