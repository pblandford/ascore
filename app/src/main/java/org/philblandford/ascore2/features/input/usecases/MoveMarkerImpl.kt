package org.philblandford.ascore2.features.input.usecases

import com.philblandford.kscore.api.KScore

class MoveMarkerImpl(private val kScore: KScore) : MoveMarker {
  override operator fun invoke(left:Boolean) {
    kScore.moveMarker(left)
  }
}