package com.philblandford.kscore.engine.scorefunction

import com.philblandford.kscore.engine.types.*

import com.philblandford.kscore.engine.duration.Duration
import com.philblandford.kscore.engine.duration.dZero
import com.philblandford.kscore.engine.eventadder.subadders.ChordDecoration
import org.junit.Test

class TremoloTest : ScoreTest() {

  @Test
  fun testAddTremolo() {
    SMV()
    SAE(EventType.TREMOLO, eav(1), paramMapOf(EventParam.TREMOLO_BEATS to Duration(1,16)))
    SVP(EventType.DURATION, EventParam.TREMOLO_BEATS, ChordDecoration(items = listOf(Duration(1,16))), eav(1))
  }

  @Test
  fun testDeleteTremolo() {
    SMV()
    SAE(EventType.TREMOLO, eav(1), paramMapOf(EventParam.TREMOLO_BEATS to Duration(1,16)))
    SDE(EventType.TREMOLO, eav(1))
    SVNP(EventType.DURATION, EventParam.TREMOLO_BEATS, eav(1))
  }

  @Test
  fun testSetZeroBeatsDeletes() {
    SMV()
    SAE(EventType.TREMOLO, eav(1), paramMapOf(EventParam.TREMOLO_BEATS to Duration(1,16)))
    SSP(EventType.TREMOLO, EventParam.TREMOLO_BEATS, dZero(), eav(1))
    SVNP(EventType.DURATION, EventParam.TREMOLO_BEATS, eav(1))
  }

  @Test
  fun testAddTremoloVoice2() {
    SMV(eventAddress = eav(1, voice = 2))
    SAE(EventType.TREMOLO, eav(1, voice = 2), paramMapOf(EventParam.TREMOLO_BEATS to Duration(1,16)))
    SVP(EventType.DURATION, EventParam.TREMOLO_BEATS, ChordDecoration(items = listOf(Duration(1,16))), eav(1, voice = 2))
  }

  @Test
  fun testAddTremoloVoice2NoteNotThere() {
    SMV(eventAddress = eav(1))
    SAE(EventType.TREMOLO, eav(1, voice = 2), paramMapOf(EventParam.TREMOLO_BEATS to Duration(1,16)))
    SVNP(EventType.DURATION, EventParam.TREMOLO_BEATS, eav(1))
  }

  @Test
  fun testAddTremoloNoBeatsVoice2NoteNotThere() {
    SMV(eventAddress = eav(1))
    SAE(EventType.TREMOLO, eav(1, voice = 2), paramMapOf(EventParam.TREMOLO_BEATS to 0))
    SVNP(EventType.DURATION, EventParam.TREMOLO_BEATS, eav(1))
  }

}