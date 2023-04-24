package com.philblandford.kscore.sound.transform

import assertEqual
import com.philblandford.kscore.engine.types.*
import com.philblandford.kscore.engine.dsl.dslChord
import com.philblandford.kscore.engine.duration.*
import com.philblandford.kscore.engine.eventadder.subadders.ChordDecoration
import org.junit.Test

class OrnamentTransformerTest {

  @Test
  fun testTransformTrill() {
    val chord = chord(getChordO(crotchet(), OrnamentType.TRILL))!!
    val transformed = OrnamentTransformer.transform(chord, 0)
    assertEqual(
      Array(4) { ORNAMENT_MAX_NOTE }.toList(),
      transformed.map { it.second.realDuration }.take(4).toList()
    )
  }

  @Test
  fun testTransformTrillNoteAbove() {
    val chord = chord(getChordO(crotchet(), OrnamentType.TRILL))!!
    val transformed = OrnamentTransformer.transform(chord, 0)
    assertEqual(Pitch(NoteLetter.F, Accidental.NATURAL, 4), transformed.toList()[0].second.notes.first().pitch)
    assertEqual(Pitch(NoteLetter.G, Accidental.NATURAL, 4), transformed.toList()[1].second.notes.first().pitch)
  }

  @Test
  fun testTransformTrillNotes() {
    val chord = chord(getChordO(crotchet(), OrnamentType.TRILL))!!
    val transformed = OrnamentTransformer.transform(chord, 0)
    assertEqual(listOf(NoteLetter.F, NoteLetter.G, NoteLetter.F, NoteLetter.G, NoteLetter.F).toList(), transformed.map { it.second.notes.first().pitch.noteLetter }.toList())
  }

  @Test
  fun testTransformTurnNotes() {
    val chord = chord(getChordO(crotchet(), OrnamentType.TURN))!!
    val transformed = OrnamentTransformer.transform(chord, 0)
    assertEqual(listOf(NoteLetter.F, NoteLetter.G, NoteLetter.F, NoteLetter.E, NoteLetter.F).toList(), transformed.map { it.second.notes.first().pitch.noteLetter }.toList())
  }

  @Test
  fun testTransformMordentNotes() {
    val chord = chord(getChordO(crotchet(), OrnamentType.MORDENT))!!
    val transformed = OrnamentTransformer.transform(chord, 0)
    assertEqual(listOf(NoteLetter.F, NoteLetter.G, NoteLetter.F).toList(), transformed.map { it.second.notes.first().pitch.noteLetter }.toList())
  }

  @Test
  fun testTransforLowerMordentNotes() {
    val chord = chord(getChordO(crotchet(), OrnamentType.LOWER_MORDENT))!!
    val transformed = OrnamentTransformer.transform(chord, 0)
    assertEqual(listOf(NoteLetter.F, NoteLetter.E, NoteLetter.F).toList(), transformed.map { it.second.notes.first().pitch.noteLetter }.toList())
  }

  @Test
  fun testTransformTrillRemainderChord() {
    val chord = chord(getChordO(crotchet(), OrnamentType.TRILL))!!.addNote(
      Note(
        crotchet(), Pitch(
          NoteLetter.D
        )
      )
    )
    val transformed = OrnamentTransformer.transform(chord, 0)
    assertEqual(2, transformed.groupBy { it.first }[dZero()]?.size)
    assert(transformed.groupBy { it.first }[dZero()]?.any { it.second.realDuration == crotchet() } == true)
    assert(transformed.groupBy { it.first }[dZero()]?.any { it.second.realDuration == ORNAMENT_MAX_NOTE } == true)
  }

  @Test
  fun testTransformTrillAccidentalAbove() {
    val chord = chord(getChordO(crotchet(), OrnamentType.TRILL, Accidental.FLAT))!!
    val transformed = OrnamentTransformer.transform(chord, 0)
    assertEqual(Pitch(NoteLetter.F, Accidental.NATURAL, 4), transformed.toList()[0].second.notes.first().pitch)
    assertEqual(Pitch(NoteLetter.G, Accidental.FLAT, 4), transformed.toList()[1].second.notes.first().pitch)
  }


  @Test
  fun testTransformTrillAccidentalBelow() {
    val chord = chord(getChordO(crotchet(), OrnamentType.TURN, Accidental.FLAT, Accidental.FLAT))!!
    val transformed = OrnamentTransformer.transform(chord, 0)
    assertEqual(Pitch(NoteLetter.F, Accidental.NATURAL, 4), transformed.toList()[0].second.notes.first().pitch)
    assertEqual(Pitch(NoteLetter.G, Accidental.FLAT, 4), transformed.toList()[1].second.notes.first().pitch)
    assertEqual(Pitch(NoteLetter.F, Accidental.NATURAL, 4), transformed.toList()[2].second.notes.first().pitch)
    assertEqual(Pitch(NoteLetter.E, Accidental.FLAT, 4), transformed.toList()[3].second.notes.first().pitch)
  }


}

internal fun getChordO(duration: Duration, ornamentType: OrnamentType,
                       aAbove:Accidental? = null, aBelow:Accidental? = null): Event {
  return dslChord(duration).addParam(
    EventParam.ORNAMENT,
    ChordDecoration(false, listOf(Ornament(ornamentType, aAbove, aBelow)))
  )
}