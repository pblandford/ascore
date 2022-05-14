package org.philblandford.ascore2.features.sound.usecases

interface SoundNote {
  operator fun invoke(midiVal:Int)
}