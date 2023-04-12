package com.philblandford.kscore.sound.transform

import assertEqual
import com.philblandford.kscore.engine.types.EventParam
import com.philblandford.kscore.engine.dsl.dslChord
import com.philblandford.kscore.engine.duration.*
import com.philblandford.kscore.engine.map.EMK
import com.philblandford.kscore.engine.map.EventHash
import com.philblandford.kscore.engine.map.eventHashOf
import com.philblandford.kscore.engine.types.Event
import com.philblandford.kscore.engine.types.EventType
import com.philblandford.kscore.engine.types.eagv
import org.junit.Test

class GraceNoteTransformerTest {

  @Test
  fun testAcciaccatura() {
    val chord = chord(dslChord(crotchet()))!!
    val graceNotes = getGraceNotes(listOf(semiquaver()))
    val transformed = GraceNoteTransformer.transform(chord.toEvent(), graceNotes)!!.toList()
    assertEqual(GRACE_NOTE_SEMIQUAVER_DURATION, transformed.first().second.realDuration())
    assertEqual(crotchet() - GRACE_NOTE_SEMIQUAVER_DURATION, transformed.last().second.realDuration())
  }

  private fun getGraceNotes(durations: Iterable<Duration>): Map<Offset, Event> {

    var offset = dZero()
    return durations.map { d ->
      val o = offset
      offset += d
      o to chord(dslChord(d))!!.toEvent()
    }.toMap()
  }

}

