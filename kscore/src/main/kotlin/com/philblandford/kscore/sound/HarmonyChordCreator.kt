package com.philblandford.kscore.sound

import com.philblandford.kscore.engine.duration.Chord
import com.philblandford.kscore.engine.duration.Duration
import com.philblandford.kscore.engine.duration.Note
import com.philblandford.kscore.engine.duration.minim
import com.philblandford.kscore.engine.pitch.Harmony

fun createHarmonyChord(
  harmony: Harmony,
  octave: Int = 3,
  duration: Duration = minim()
): Chord? {

  val notes = harmony.pitches(octave).map {
    Note(duration, it)
  }
  val withRoot =
    listOfNotNull(harmony.root?.copy(octave = octave - 1)?.let { Note(duration, it) }).plus(notes)
  return Chord(duration, withRoot)
}