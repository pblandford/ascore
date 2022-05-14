package org.philblandford.ascore2.features.sound.model

import com.philblandford.kscore.engine.types.EventAddress

data class PlaybackMarkerInfo(
  val eventAddress: EventAddress,
  val page:Int
)