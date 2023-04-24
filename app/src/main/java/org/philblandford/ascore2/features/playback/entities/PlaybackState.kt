package org.philblandford.ascore2.features.playback.entities

data class MixerInstrument(
  val shortName: String,
  val longName: String,
  val level: Int,
  val muted: Boolean = false,
  val solo: Boolean = false
)

data class PlaybackState(val shuffle:Boolean, val harmonies:Boolean, val loop:Boolean,
val mixerInstruments:List<MixerInstrument>)
