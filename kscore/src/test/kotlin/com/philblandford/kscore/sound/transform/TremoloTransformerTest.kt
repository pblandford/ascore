package com.philblandford.kscore.sound.transform

import assertEqual
import com.philblandford.kscore.engine.types.EventParam
import com.philblandford.kscore.engine.dsl.dslChord
import com.philblandford.kscore.engine.duration.*
import com.philblandford.kscore.engine.newadder.subadders.ChordDecoration
import org.junit.Test

class TremoloTransformerTest {

  @Test
  fun testTremolo() {
    val chord = getChordT(crotchet(), semiquaver())
    val transformed = TremoloTransformer.transform(chord)
    assertEqual(Array(4){ semiquaver()}.toList(),
      transformed!!.values.map { it.realDuration }.toList())
  }

  @Test
  fun testTremoloQuaver() {
    val chord = getChordT(crotchet(), quaver())
    val transformed = TremoloTransformer.transform(chord)
    assertEqual(Array(2){ quaver() }.toList(),
      transformed!!.values.map { it.realDuration }.toList())
  }

  @Test
  fun testTremoloTooBig() {
    val chord = getChordT(semiquaver(), quaver())
    val transformed = TremoloTransformer.transform(chord)
    assert(transformed == null)
  }

  @Test
  fun testTremoloOverSemibreve() {
    val chord = getChordT(semibreve(), semiquaver())
    val transformed = TremoloTransformer.transform(chord)
    assertEqual(Array(16){ semiquaver()}.toList(),
      transformed!!.values.map { it.realDuration }.toList())
    repeat(16) {
      assertEqual(semiquaver() * it, transformed.keys.toList()[it])
    }
  }
}

internal fun getChordT(duration: Duration, beats:Duration): Chord {
  return chord(dslChord(duration).addParam(
    EventParam.TREMOLO_BEATS,
    ChordDecoration(false, listOf(beats))
  ))!!
}