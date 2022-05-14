package org.philblandford.ascore2.features.instruments

interface SetVolume {
  operator fun invoke(part:Int, volume:Int)
}