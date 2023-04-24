package com.philblandford.kscore.engine.core.score.scorequery

import com.philblandford.kscore.engine.types.*
import assertEqual
import com.philblandford.kscore.engine.core.score.*
import com.philblandford.kscore.engine.dsl.*
import com.philblandford.kscore.engine.duration.*
import com.philblandford.kscore.engine.map.EMK
import org.junit.After
import org.junit.Before
import org.junit.Test
import com.philblandford.kscore.engine.scorefunction.ScoreTest
import com.philblandford.kscore.engine.time.TimeSignature
import grace

class ScoreQueryTest : ScoreTest() {

  @Before
  fun setUp() {
    SCORELEVEL_CACHE_ENABLE = false
  }

  @After
  fun tearDown() {
    SCORELEVEL_CACHE_ENABLE = true
  }

  @Test
  fun testGetBars() {
    SCD(bars = 1)
    assertEqual(1, EG().numBars)
  }

  @Test
  fun testGetBars2() {
    SCD(bars = 2)
    assertEqual(2, EG().numBars)
  }

  @Test
  fun testGetBeamsForVoice() {
    val score = scoreQuavers()
    sc.setNewScoreNoRepresentation(score)
    val events = EG().getEvents(EventType.BEAM, eav(1))
    assertEqual(2, events?.size)
  }


  @Test
  fun testGetEmptyVoiceMaps() {
    SCD()
    SMV(
      eventAddress = eav(
        1,
        voice = 2
      )
    )
    val maps = EG().getEmptyVoiceMaps(ea(1), ea(1))
    assertEqual(eav(1), maps.toList().first())
  }

  @Test
  fun testGetEmptyVoiceMapsAfterNote() {
    SCD()
    SMV()
    val maps = EG().getEmptyVoiceMaps(ea(1), ea(1))
    assert(maps.count() == 0)
  }

  @Test
  fun testGetEmptyVoiceMapsAfterNoteDeletedVoice2() {
    SCD()
    SMV(
      eventAddress = eav(
        1,
        voice = 2
      )
    )
    SDE(
      EventType.DURATION,
      eav(1, voice = 2)
    )

    val maps = EG().getEmptyVoiceMaps(ea(1), ea(1))
    assertEqual(1, maps.count())
  }


  @Test
  fun testGetPart() {
    SCD()
    SVP(EventType.PART, EventParam.LABEL, "Violin", ea(0))
    SVP(EventType.PART, EventParam.ABBREVIATION, "Vln", ea(0))
  }

  @Test
  fun testGetLastSegmentInDuration() {
    SCDG()
    SMV()
    SMV(eventAddress = eav(1, crotchet()))
    SMV(duration = minim(), eventAddress = eas(1, dZero(), StaveId(1, 2)))
    assertEqual(
      eas(1, crotchet(), StaveId(1, 2)),
      EG().getLastSegmentInDuration(eas(1, dZero(), StaveId(1, 2)))
    )
  }

  @Test
  fun testGetLastSegmentInDurationEmptyBarBelow() {
    SCDG()
    SMV()
    assertEqual(
      eas(1, minim(), StaveId(1, 2)),
      EG().getLastSegmentInDuration(eas(1, dZero(), StaveId(1, 2)))
    )
  }

  @Test
  fun testGetRangeSameStartEnd() {
    assertEqual(1, EG().getEvents(EventType.TEMPO, ez(1), ez(1))?.size)
  }

  @Test
  fun testGetStaveRange() {
    SCD(instruments = listOf("Piano", "Violin"))
    assertEqual(
      listOf(StaveId(1, 1), StaveId(1, 2), StaveId(2, 1)).toList(),
      EG().getStaveRange(StaveId(1, 1), StaveId(2, 1)).toList()
    )
  }

  @Test
  fun testGetStaveRangeStaveZero() {
    SCD(
      instruments = listOf(
        "Piano",
        "Violin"
      )
    )
    assertEqual(listOf(sZero()).toList(), EG().getStaveRange(sZero(), sZero()).toList())
  }

  @Test
  fun testGetRangeGrace() {
    SMV()
    grace()
    grace()
    assertEqual(
      listOf(eagv(1), eagv(1, graceOffset = semiquaver())).toList(),
      EG().getEvents(EventType.DURATION, eagv(1), eagv(1, graceOffset = semiquaver()))
        ?.map { it.key.eventAddress }?.toList()
    )
  }

  @Test
  fun testGetRangeGracePartial() {
    SMV()
    grace()
    grace()
    grace()
    assertEqual(
      listOf(eagv(1), eagv(1, graceOffset = semiquaver())).toList(),
      EG().getEvents(EventType.DURATION, eagv(1), eagv(1, graceOffset = semiquaver()))
        ?.map { it.key.eventAddress }?.toList()
    )
  }

  @Test
  fun testGetRangeGracePartialAfterStart() {
    SMV()
    grace()
    grace()
    grace()
    assertEqual(
      listOf(eagv(1, graceOffset = semiquaver()), eagv(1, graceOffset = quaver())).toList(),
      EG().getEvents(
        EventType.DURATION,
        eagv(1, graceOffset = semiquaver()),
        eagv(1, graceOffset = quaver())
      )
        ?.map { it.key.eventAddress }?.toList()
    )
  }

  @Test
  fun testGetRangeNotes() {
    SMV()
    assertEqual(
      listOf(eav(1).copy(id=1)).toList(),
      EG().getEvents(
        EventType.NOTE,
        ea(1),
        ea(2)
      )
        ?.map { it.key.eventAddress }?.toList()
    )
  }

  @Test
  fun testGetNoteDuration() {
    SMV()
    assertEqual(crotchet(), EG().getNoteDuration(eav(1).copy(id = 1)))
  }

  @Test
  fun testGetNoteDurationTied() {
    SMV(duration = breve())
    assertEqual(breve(), EG().getNoteDuration(eav(1).copy(id = 1)))
  }

  @Test
  fun testGetNoteDurationTiedMultiple() {
    SMV(duration = longa())
    assertEqual(longa(), EG().getNoteDuration(eav(1).copy(id = 1)))
  }

  @Test
  fun testGetEventEnd() {
    SMV()
    assertEqual(eav(1, crotchet()), EG().getEventEnd(eav(1)))
  }

  @Test
  fun testGetEventEndEmptyBar() {
    assertEqual(eav(2), EG().getEventEnd(eav(1)))
  }

  @Test
  fun testGetSystemEvents() {
    val events = EG().getSystemEvents().toList()
    assert(events.find { it.first.eventType == EventType.KEY_SIGNATURE } != null)
    assert(events.find { it.first.eventType == EventType.TIME_SIGNATURE } != null)
    assert(events.find { it.first.eventType == EventType.TEMPO } != null)
  }

  @Test
  fun testGetSystemEventsIncludesBreak() {
    SAE(
      EventType.BREAK,
      ez(3),
      paramMapOf(EventParam.TYPE to BreakType.SYSTEM)
    )
    val events = EG().getSystemEvents()
    assert(events[EMK(EventType.BREAK, ez(3))] != null)
  }

  @Test
  fun testIsEmptyBar() {
    assert(EG().isEmptyBar(ea(1)))
  }

  @Test
  fun testIsEmptyBarFalse() {
    SMV()
    assertEqual(false, EG().isEmptyBar(ea(1)))
  }

  @Test
  fun testGetBeams() {
    repeat(4) {
      SMV(
        duration = quaver(),
        eventAddress = eav(1, quaver() * it)
      )
    }
    assertEqual(1, EG().getBeams().size)
  }

  @Test
  fun testGetBeamsTuplet() {
    SAE(
      EventType.TUPLET,
      params = paramMapOf(EventParam.NUMERATOR to 3, EventParam.DENOMINATOR to 8)
    )
    repeat(3) {
      SMV(
        duration = quaver(),
        eventAddress = eav(1, Offset(1, 12) * it)
      )
    }
    assertEqual(1, EG().getBeams().size)
  }

  @Test
  fun testGetSelectedPartName() {
    sc.setSelectedPart(1)
    assertEqual("Violin", EG().selectedPartName())
  }

  @Test
  fun testGetSelectedPartNameNull() {
    assert(EG().selectedPartName() == null)
  }

  @Test
  fun testGetKeySignature() {
    assertEqual(0, EG().getKeySignature(ez(1)))
  }

  @Test
  fun testGetKeySignatureMiddle() {
    SAE(
      EventType.KEY_SIGNATURE,
      ez(4),
      paramMapOf(EventParam.SHARPS to 3)
    )
    assertEqual(0, EG().getKeySignature(ez(1)))
    assertEqual(3, EG().getKeySignature(ez(4)))
    assertEqual(3, EG().getKeySignature(ez(5)))
  }

  @Test
  fun testGetKeySignatureTransposing() {
    SCD(instruments = listOf("Trumpet"))
    assertEqual(2, EG().getKeySignature(ea(1)))
  }

  @Test
  fun testGetKeySignatureTransposingConcertOption() {
    SCD(instruments = listOf("Trumpet"))
    assertEqual(0, EG().getKeySignature(ea(1), true))
  }

  @Test
  fun testGetKeySignatureTransposingConcertOptionTransposeOptionSet() {
    SCD(instruments = listOf("Trumpet"))
    SSO(
      EventParam.OPTION_SHOW_TRANSPOSE_CONCERT,
      true
    )
    assertEqual(0, EG().getKeySignature(ea(1), true))
  }

  @Test
  fun testGetParam() {
    SAE(EventType.CLEF, ea(2), paramMapOf(EventParam.TYPE to ClefType.BASS))
    val type = EG().getParam<ClefType>(EventType.CLEF, EventParam.TYPE, ea(2))
    assertEqual(ClefType.BASS, type)
  }

  @Test
  fun testGetParamVoiceIgnored() {
    SAE(EventType.CLEF, ea(2), paramMapOf(EventParam.TYPE to ClefType.BASS))
    val type = EG().getParam<ClefType>(EventType.CLEF, EventParam.TYPE, eav(2))
    assertEqual(ClefType.BASS, type)
  }

  @Test
  fun testGetStaveEvents() {
    SAE(EventType.CLEF, ea(2), paramMapOf(EventParam.TYPE to ClefType.BASS))
    val events = EG().getEventsForStave(StaveId(1, 1), listOf(EventType.CLEF), ea(2), ea(3))
    assertEqual(ClefType.BASS, events.toList().first().second.subType)
  }

  @Test
  fun testGetStaveEventsLineStart() {
    SAE(
      EventType.WEDGE,
      ea(2),
      paramMapOf(EventParam.DURATION to semibreve() * 3, EventParam.IS_UP to true)
    )
    val events = EG().getEventsForStave(StaveId(1, 1), listOf(EventType.WEDGE), ea(2), ea(3))
    assertEqual(1, events.toList().size)
    assert(!events.toList().first().second.isTrue(EventParam.END))
  }

  @Test
  fun testGetStaveEventsLineEnd() {
    SAE(
      EventType.WEDGE,
      ea(2),
      paramMapOf(EventParam.DURATION to semibreve() * 3, EventParam.IS_UP to true)
    )
    val events = EG().getEventsForStave(StaveId(1, 1), listOf(EventType.WEDGE), ea(3), ea(6))
    assertEqual(2, events.toList().size)
    assert(events.toList().first().second.isTrue(EventParam.END))
  }

  @Test
  fun testGetStaveEventsLineMiddle() {
    SAE(
      EventType.WEDGE,
      ea(2),
      paramMapOf(EventParam.DURATION to semibreve() * 10, EventParam.IS_UP to true)
    )
    val events = EG().getEventsForStave(StaveId(1, 1), listOf(EventType.WEDGE), ea(3), ea(6))
    assertEqual(1, events.toList().size)
    assert(!events.toList().first().second.isTrue(EventParam.END))
  }

  @Test
  fun testGetStaveEventsLineMiddleId1() {
    SAE(
      EventType.WEDGE,
      ea(2),
      paramMapOf(EventParam.DURATION to semibreve() * 10, EventParam.IS_UP to false)
    )
    val events = EG().getEventsForStave(StaveId(1, 1), listOf(EventType.WEDGE), ea(3), ea(6))
    assertEqual(1, events.toList().size)
    assert(!events.toList().first().second.isTrue(EventParam.END))
  }


  @Test
  fun testGetTimeSignatureAtExcludesHidden() {
    SAE(TimeSignature(3,4).toHiddenEvent(), ez(2))
    assertEqual(TimeSignature(4,4), EG().getTimeSignature(ez(4)))
  }

  @Test
  fun testGetTimeSignatureAfterTwoHiddenAtStart() {
    SAE(TimeSignature(3,4).toHiddenEvent(), ez(1))
    assertEqual(TimeSignature(4,4), EG().getTimeSignature(ez(4)))
  }

}