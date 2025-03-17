package org.philblandford.ascore2.features.drawing

import com.philblandford.kscore.api.KScore
import com.philblandford.kscore.engine.types.EventAddress
import timber.log.Timber

class DrawPageImpl(private val kScore: KScore) : DrawPage {
  override operator fun invoke(page:Int, playbackMarker: EventAddress?, vararg args:Any) {
    kScore.prepareDraw(*args)
    kScore.drawPage(page, playbackMarker)
  }
}