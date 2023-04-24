package com.philblandford.kscore.sound.transform

import assertEqual
import com.philblandford.kscore.engine.dsl.dslChord
import com.philblandford.kscore.engine.duration.*
import com.philblandford.kscore.engine.eventadder.subadders.ChordDecoration
import com.philblandford.kscore.engine.types.*
import com.philblandford.kscore.sound.transform.Transformer.transform
import org.junit.Test

class TransformerTest {

  @Test
  fun testTransformStaccato() {
    val chord = getChordA(crotchet(), ArticulationType.STACCATO)
    val transformed = Transformer.transform(chord, 0, null)
    assertEqual(crotchet() * STACCATO_LENGTH, transformed.toList().first().second.realDuration())
  }

  @Test
  fun testTransformTrill() {
    val chord = chord(getChordO(crotchet(), OrnamentType.TRILL))!!.toEvent()
    val transformed = Transformer.transform(chord, 0, null)
    assertEqual(
      Array(4) { ORNAMENT_MAX_NOTE }.toList(),
      transformed.map { it.second.realDuration() }.take(4).toList()
    )
  }

  @Test
  fun testTransformTrillPlusStaccato() {
    val chord = chord(getChordO(crotchet(), OrnamentType.TRILL))!!.toEvent()
    val transformedNoStaccato = Transformer.transform(chord, 0, null)
    val chordStaccato = chord.addParam(
      EventParam.ARTICULATION,
      ChordDecoration(false, listOf(ArticulationType.STACCATO))
    )
    val transformedStaccato = Transformer.transform(chordStaccato, 0, null)

    assertEqual(
      transformedNoStaccato.last().second.realDuration() * STACCATO_LENGTH,
      transformedStaccato.last().second.realDuration()
    )
  }

  @Test
  fun testTransformTremolo() {
    val chord = getChordT(crotchet(), semiquaver())
    val transformed = Transformer.transform(chord.toEvent(), 0, null)
    assertEqual(Array(4){ semiquaver()}.toList(),
      transformed.map { it.second.realDuration() }.toList())
  }

  @Test
  fun testTransformLongTrill() {
    val chord = dslChord(crotchet())
    val transformed = Transformer.transform(chord, 0, Event(EventType.LONG_TRILL))
    assertEqual(
      Array(4) { ORNAMENT_MAX_NOTE }.toList(),
      transformed.map { it.second.realDuration() }.take(4).toList()
    )
  }


  @Test
  fun testTransformArpeggio() {
    val chord = getChordA(crotchet())
    val transformed = transform(chord.toEvent(), 0, null)
    assertEqual(Array(3) { crotchet() - (ORNAMENT_MAX_NOTE * it) }.toList(),
      transformed.map { it.second.realDuration() }.toList().take(3)
    )
  }

  private fun getChordA(duration: Duration, articulationType: ArticulationType): Event {
    val chord = dslChord(duration)
    return chord.addParam(EventParam.ARTICULATION, ChordDecoration(false, listOf(articulationType)))
  }


}