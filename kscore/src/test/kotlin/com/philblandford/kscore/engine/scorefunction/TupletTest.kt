package com.philblandford.kscore.engine.scorefunction

import assertEqual
import com.philblandford.kscore.engine.core.area.Coord
import com.philblandford.kscore.engine.types.*

import com.philblandford.kscore.engine.dsl.dslChord
import com.philblandford.kscore.engine.dsl.rest
import com.philblandford.kscore.engine.duration.*
import com.philblandford.kscore.engine.map.EMK
import com.philblandford.kscore.engine.time.TimeSignature
import org.junit.Test

class TupletTest : ScoreTest() {

  @Test
  fun testAddTuplet() {
    SAE(
      EventType.TUPLET,
      eav(1),
      paramMapOf(EventParam.NUMERATOR to 3, EventParam.DENOMINATOR to 8)
    )
    SVE(EventType.TUPLET, eav(1))
  }

  @Test
  fun testAddTupletAddEvent() {
    SAE(
      EventType.TUPLET,
      eav(1),
      paramMapOf(EventParam.NUMERATOR to 3, EventParam.DENOMINATOR to 8)
    )
    SAE(EventType.DURATION, params = dslChord(quaver()).params, eventAddress = eav(1))
    SVP(EventType.DURATION, EventParam.DURATION, quaver(), eav(1))
    SVP(EventType.DURATION, EventParam.REAL_DURATION, tupletQuaver, eav(1))
  }

  @Test
  fun testAddTupletAddEventMidTuplet() {
    SAE(
      EventType.TUPLET,
      eav(1),
      paramMapOf(EventParam.NUMERATOR to 3, EventParam.DENOMINATOR to 8)
    )
    SAE(
      EventType.DURATION,
      params = dslChord(quaver()).params,
      eventAddress = eav(1, Duration(1, 6))
    )
    SVP(EventType.DURATION, EventParam.DURATION, quaver(), eav(1, Duration(1, 6)))
    SVP(EventType.DURATION, EventParam.REAL_DURATION, tupletQuaver, eav(1, Duration(1, 6)))
  }

  @Test
  fun testAddTupletRealDurationSet() {
    SAE(
      EventType.TUPLET,
      eav(1),
      paramMapOf(EventParam.NUMERATOR to 3, EventParam.DENOMINATOR to 8)
    )
    SVP(EventType.DURATION, EventParam.DURATION, quaver(), eav(1))
    SVP(EventType.DURATION, EventParam.REAL_DURATION, tupletQuaver, eav(1))
  }

  @Test
  fun testAddTupletHidden() {
    SAE(
      EventType.TUPLET,
      eav(1),
      paramMapOf(EventParam.NUMERATOR to 3, EventParam.HIDDEN to true)
    )
    SVP(EventType.TUPLET, EventParam.HIDDEN, true, eav(1))
  }

  @Test
  fun testAddTupletLastCrotchet() {
    SAE(rest())
    SAE(rest(), eav(1, minim()))
    SAE(
      EventType.TUPLET,
      eav(1, minim(1)),
      paramMapOf(EventParam.NUMERATOR to 3)
    )
    SVE(EventType.TUPLET, eav(1, minim(1)))
  }

  @Test
  fun testAddTupletLastCrotchetVoice2() {
    SAE(rest(), eav(1, dZero(), 2))
    SAE(rest(), eav(1, minim(), 2))
    SAE(
      EventType.TUPLET,
      eav(1, minim(1), 2),
      paramMapOf(EventParam.NUMERATOR to 3)
    )
    SVE(EventType.TUPLET, eav(1, minim(1), 2))
  }

  @Test
  fun testAddTupletLastTwoCrotchetsVoice2() {
    SAE(rest(), eav(1, dZero(), 2))
    SAE(rest(), eav(1, minim(), 2))
    SAE(
      EventType.TUPLET,
      eav(1, minim(), 2),
      paramMapOf(EventParam.NUMERATOR to 3)
    )
    SAE(
      EventType.TUPLET,
      eav(1, minim(1), 2),
      paramMapOf(EventParam.NUMERATOR to 3)
    )
    SVE(EventType.TUPLET, eav(1, minim(), 2))
    SVE(EventType.TUPLET, eav(1, minim(1), 2))
  }

  @Test
  fun testAddTupletToEmptyBar() {
    SAE(
      EventType.TUPLET,
      eav(1),
      paramMapOf(EventParam.NUMERATOR to 3)
    )
    SVE(EventType.TUPLET, eav(1))
  }

  @Test
  fun testAddTupletAddNote() {
    SAE(
      EventType.TUPLET,
      eav(1),
      paramMapOf(EventParam.NUMERATOR to 3, EventParam.DENOMINATOR to 8)
    )
    SMV(duration = quaver())
    SVP(EventType.DURATION, EventParam.DURATION, quaver(), eav(1))
    SVP(EventType.DURATION, EventParam.REAL_DURATION, tupletQuaver, eav(1))
  }

  @Test
  fun testAddTupletAddNoteSecondQuaver() {
    SAE(
      EventType.TUPLET,
      eav(1),
      paramMapOf(EventParam.NUMERATOR to 3, EventParam.DENOMINATOR to 8)
    )
    SMV(duration = quaver(), eventAddress = eav(1, tupletQuaver))
    SVP(EventType.DURATION, EventParam.DURATION, quaver(), eav(1, tupletQuaver))
    SVP(EventType.DURATION, EventParam.REAL_DURATION, tupletQuaver, eav(1, tupletQuaver))
  }

  @Test
  fun testAddTupletAddNoteSecondQuaverSecondTuplet() {
    SAE(
      EventType.TUPLET,
      eav(1),
      paramMapOf(EventParam.NUMERATOR to 3, EventParam.DENOMINATOR to 8)
    )
    SAE(
      EventType.TUPLET,
      eav(1, crotchet()),
      paramMapOf(EventParam.NUMERATOR to 3, EventParam.DENOMINATOR to 8)
    )
    SMV(duration = quaver(), eventAddress = eav(1, crotchet().add(tupletQuaver)))
    SVP(EventType.DURATION, EventParam.DURATION, quaver(), eav(1, crotchet().add(tupletQuaver)))
    SVP(
      EventType.DURATION,
      EventParam.REAL_DURATION,
      tupletQuaver,
      eav(1, crotchet().add(tupletQuaver))
    )
    SVP(
      EventType.DURATION,
      EventParam.TYPE,
      DurationType.CHORD,
      eav(1, crotchet().add(tupletQuaver))
    )
  }

  @Test
  fun testAddTupletAddNoteToExisting() {
    SAE(
      EventType.TUPLET,
      eav(1),
      paramMapOf(EventParam.NUMERATOR to 3, EventParam.DENOMINATOR to 8)
    )
    SMV(duration = quaver())
    SMV(65, duration = quaver())
    assertEqual(
      2,
      EG().getParam<Iterable<Event>>(EventType.DURATION, EventParam.NOTES, eav(1))?.count()
    )
  }

  @Test
  fun testAddTupletDeleteNoteFromExisting() {
    SAE(
      EventType.TUPLET,
      eav(1),
      paramMapOf(EventParam.NUMERATOR to 3, EventParam.DENOMINATOR to 8)
    )
    SMV(duration = quaver())
    SMV(65, duration = quaver())
    SDE(EventType.NOTE, eav(1).copy(id = 1))
    assertEqual(
      1,
      EG().getParam<Iterable<Event>>(EventType.DURATION, EventParam.NOTES, eav(1))?.count()
    )
  }

  @Test
  fun testAddTupletAddNoteAccidentalShown() {
    SAE(
      EventType.TUPLET,
      eav(1),
      paramMapOf(EventParam.NUMERATOR to 3, EventParam.DENOMINATOR to 8)
    )
    SMV(73, duration = quaver())
    SVP(
      EventType.NOTE,
      EventParam.PITCH,
      Pitch(NoteLetter.C, Accidental.SHARP, 5, true),
      eav(1).copy(id = 1)
    )
  }

  @Test
  fun testAddTupletAddEventRetainsRests() {
    SAE(
      EventType.TUPLET,
      eav(1),
      paramMapOf(EventParam.NUMERATOR to 3, EventParam.DENOMINATOR to 8)
    )
    SAE(EventType.DURATION, params = dslChord(quaver()).params, eventAddress = eav(1))
    SVP(EventType.DURATION, EventParam.DURATION, quaver(), eav(1, tupletQuaver))
    SVP(EventType.DURATION, EventParam.REAL_DURATION, tupletQuaver, eav(1, tupletQuaver))
    SVP(EventType.DURATION, EventParam.DURATION, quaver(), eav(1, tupletQuaver.multiply(2)))
    SVP(
      EventType.DURATION,
      EventParam.REAL_DURATION,
      tupletQuaver,
      eav(1, tupletQuaver.multiply(2))
    )
  }

  @Test
  fun testAddTupletAddQuaverInQuadruplet() {
    SAE(rest(crotchet(1)), eav(1))
    SAE(EventType.TUPLET, eav(1), paramMapOf(EventParam.NUMERATOR to 4))
    SMV(duration = quaver())
    SVP(EventType.DURATION, EventParam.DURATION, quaver(), eav(1))
    SVP(
      EventType.DURATION, EventParam.REAL_DURATION,
      Duration(3, 16), eav(1)
    )
  }

  @Test
  fun testAddTupletRestsAdded() {
    SAE(
      EventType.TUPLET,
      eav(1),
      paramMapOf(EventParam.NUMERATOR to 3, EventParam.DENOMINATOR to 8)
    )
    SVE(EventType.TUPLET, eav(1))
    SVP(EventType.DURATION, EventParam.REAL_DURATION, tupletQuaver, eav(1))
    SVP(EventType.DURATION, EventParam.REAL_DURATION, tupletQuaver, eav(1, tupletQuaver))
    SVP(
      EventType.DURATION,
      EventParam.REAL_DURATION,
      tupletQuaver,
      eav(1, tupletQuaver.multiply(2))
    )
  }

  @Test
  fun testAddTupletAddEventLater() {
    SAE(
      EventType.TUPLET,
      eav(1),
      paramMapOf(EventParam.NUMERATOR to 3, EventParam.DENOMINATOR to 8)
    )
    SMV(eventAddress = eav(1, minim()))
    SVP(EventType.TUPLET, EventParam.NUMERATOR, 3, eav(1))
  }

  @Test
  fun testGetEventsIncludesTupletRests() {
    SAE(
      EventType.TUPLET,
      eav(1),
      paramMapOf(EventParam.NUMERATOR to 3, EventParam.DENOMINATOR to 8)
    )
    val events = EG().getEvents(EventType.DURATION)!!
    assertEqual(
      tupletQuaver,
      events[EMK(EventType.DURATION, eav(1))]?.getParam<Duration>(EventParam.REAL_DURATION)
    )
    assertEqual(
      tupletQuaver,
      events[EMK(
        EventType.DURATION,
        eav(1, tupletQuaver)
      )]?.getParam<Duration>(EventParam.REAL_DURATION)
    )
    assertEqual(
      tupletQuaver,
      events[EMK(
        EventType.DURATION,
        eav(1, tupletQuaver.multiply(2))
      )]?.getParam<Duration>(
        EventParam.REAL_DURATION
      )
    )
  }

  @Test
  fun testAddTupletRestsAfter() {
    SAE(
      EventType.TUPLET,
      eav(1),
      paramMapOf(EventParam.NUMERATOR to 3, EventParam.DENOMINATOR to 8)
    )
    SVP(EventType.DURATION, EventParam.DURATION, crotchet(), eav(1, crotchet()))
    SVP(EventType.DURATION, EventParam.DURATION, minim(), eav(1, minim()))
  }

  @Test
  fun testAddTupletRange() {
    repeat(2) { bar ->
      repeat(4) { offset ->
        SAE(rest(crotchet()), eav(bar + 1, crotchet() * offset))
      }
    }
    SAE(
      EventType.TUPLET, eav(1), paramMapOf(EventParam.NUMERATOR to 3, EventParam.DENOMINATOR to 8),
      eav(2, minim(1))
    )
    repeat(2) { bar ->
      repeat(4) { offset ->
        SVE(EventType.TUPLET, eav(bar + 1, crotchet() * offset))

      }
    }
  }


  @Test
  fun testAddTupletRangeDenominatorNotSpecified() {
    repeat(2) { bar ->
      repeat(4) { offset ->
        SAE(rest(crotchet()), eav(bar + 1, crotchet() * offset))
      }
    }
    SAE(
      EventType.TUPLET, eav(1), paramMapOf(EventParam.NUMERATOR to 3),
      eav(2, minim(1))
    )
    repeat(2) { bar ->
      repeat(4) { offset ->
        SVE(EventType.TUPLET, eav(bar + 1, crotchet() * offset))
      }
    }
  }

  @Test
  fun testAddTupletRangeEmptyBars() {
    SAE(
      EventType.TUPLET, eav(1), paramMapOf(EventParam.NUMERATOR to 3),
      eav(2)
    )
    repeat(2) { bar ->
      SVE(EventType.TUPLET, eav(bar + 1))
    }
  }

  @Test
  fun testAddOverExistingIsNoOp() {
    SAE(
      EventType.TUPLET,
      eav(1),
      paramMapOf(EventParam.NUMERATOR to 3, EventParam.DENOMINATOR to 8)
    )
    SAE(
      EventType.TUPLET, eav(1, Duration(1, 12)),
      paramMapOf(EventParam.NUMERATOR to 3, EventParam.DENOMINATOR to 8)
    )
    assertEqual(1, SCORE().getVoiceMap(eav(1))?.tuplets?.count())
    assertEqual(dZero(), SCORE().getVoiceMap(eav(1))?.tuplets?.first()?.offset)
  }

  @Test
  fun testAddTupletNotesBeams() {
    SAE(
      EventType.TUPLET,
      eav(1),
      paramMapOf(EventParam.NUMERATOR to 3, EventParam.DENOMINATOR to 8)
    )
    repeat(3) {
      SMV(
        duration = quaver(),
        eventAddress = eav(1, quaver().multiply(Duration(2, 3)).multiply(it))
      )
    }
    val beams = EG().getBeams().getBeamStrings().toList()
    assertEqual(listOf("8:8:8"), beams)
  }

  @Test
  fun testAddTupletNotesBeamsCrotchetQuaver() {
    SAE(
      EventType.TUPLET,
      eav(1),
      paramMapOf(EventParam.NUMERATOR to 3, EventParam.DENOMINATOR to 8)
    )
    SMV(duration = crotchet(), eventAddress = eav(1))
    SMV(duration = quaver(), eventAddress = eav(1, Offset(1, 6)))

    val beams = EG().getBeams().getBeamStrings().toList()
    assertEqual(0, beams.size)
  }

  @Test
  fun testAddQuintupletBeamAfterCrotchet() {
    SAE(
      EventType.TUPLET,
      eav(1),
      paramMapOf(EventParam.NUMERATOR to 5, EventParam.DENOMINATOR to 8)
    )
    SMV(duration = crotchet(), eventAddress = eav(1, Duration(1, 10)))
    SMV(duration = quaver(), eventAddress = eav(1, Offset(3, 10)))
    SMV(duration = quaver(), eventAddress = eav(1, Offset(4, 10)))

    val beams = EG().getBeams().toList()
    assertEqual(1, beams.size)
  }

  @Test
  fun testAddQuadrupletNotesBeams() {
    SAE(TimeSignature(12, 8).toEvent(), ez(1))
    SAE(rest(crotchet(1)))
    SAE(
      EventType.TUPLET,
      eav(1),
      paramMapOf(EventParam.NUMERATOR to 4)
    )
    repeat(4) {
      SMV(
        duration = semiquaver(),
        eventAddress = eav(1, Duration(3, 32).multiply(it))
      )
    }
    val beams = EG().getBeams().getBeamStrings().toList()
    assertEqual(listOf("16:16:16:16"), beams)
  }

  @Test
  fun testAddTupletNotesBeamsVoice2() {
    SAE(
      EventType.TUPLET,
      eav(1, dZero(), 2),
      paramMapOf(EventParam.NUMERATOR to 3, EventParam.DENOMINATOR to 8)
    )
    repeat(3) {
      SMV(
        duration = quaver(),
        eventAddress = eav(1, quaver().multiply(Duration(2, 3).multiply(it)), 2)
      )
    }
    val beams = EG().getBeams().getBeamStrings().toList()
    assertEqual(listOf("8:8:8"), beams)
  }

  @Test
  fun testAddTupletNotesBeamsVoiceDownstem() {
    SAE(
      EventType.TUPLET,
      eav(1, dZero(), 2),
      paramMapOf(EventParam.NUMERATOR to 3, EventParam.DENOMINATOR to 8)
    )
    repeat(3) {
      SMV(
        duration = quaver(),
        eventAddress = eav(1, quaver().multiply(Duration(2, 3).multiply(it)), 2)
      )
    }
    val beams = EG().getBeams()
    assertEqual(false, beams.toList().first().second.up)
  }

  @Test
  fun testAddTupletNotesBeamsVoiceDownstemTwoNotes() {
    SAE(
      EventType.TUPLET,
      eav(1, dZero(), 2),
      paramMapOf(EventParam.NUMERATOR to 3, EventParam.DENOMINATOR to 8)
    )
    repeat(2) {
      SMV(
        duration = quaver(),
        eventAddress = eav(1, quaver().multiply(Duration(2, 3).multiply(it)), 2)
      )
    }
    val beams = EG().getBeams()
    assertEqual(false, beams.toList().first().second.up)
  }

  @Test
  fun testAddQuintuplet() {
    SAE(
      EventType.TUPLET,
      eav(1),
      paramMapOf(EventParam.NUMERATOR to 5, EventParam.DENOMINATOR to 8)
    )
    SVP(EventType.TUPLET, EventParam.NUMERATOR, 5, eav(1))
    SVP(EventType.TUPLET, EventParam.DENOMINATOR, 8, eav(1))
  }


  @Test
  fun testAddSextuplet() {
    SAE(
      EventType.TUPLET,
      eav(1),
      paramMapOf(EventParam.NUMERATOR to 6, EventParam.DENOMINATOR to 8)
    )
    SVP(EventType.TUPLET, EventParam.NUMERATOR, 6, eav(1))
    SVP(EventType.TUPLET, EventParam.DENOMINATOR, 8, eav(1))
  }

  @Test
  fun testAddSeptuplet() {
    SAE(
      EventType.TUPLET,
      eav(1),
      paramMapOf(EventParam.NUMERATOR to 7, EventParam.DENOMINATOR to 16)
    )
    SVP(EventType.TUPLET, EventParam.NUMERATOR, 7, eav(1))
    SVP(EventType.TUPLET, EventParam.DENOMINATOR, 16, eav(1))
    SVP(EventType.DURATION, EventParam.TYPE, DurationType.REST, eav(1, crotchet()))
  }

  @Test
  fun testAddSextupletQuavers() {
    SAE(
      EventType.TUPLET,
      eav(1),
      paramMapOf(EventParam.NUMERATOR to 6, EventParam.DENOMINATOR to 8)
    )
    repeat(6) {
      val address = eav(1, quaver().multiply(Duration(2, 3).multiply(it)))
      SMV(duration = quaver(), eventAddress = address)
      SVP(EventType.DURATION, EventParam.REAL_DURATION, quaver().multiply(Duration(2, 3)))
    }
  }

  @Test
  fun testAddOctuplet6_8() {
    SAE(
      EventType.TUPLET,
      eav(1),
      paramMapOf(EventParam.NUMERATOR to 8, EventParam.DENOMINATOR to 16)
    )
    SVP(EventType.TUPLET, EventParam.DURATION, crotchet(1))
  }

  @Test
  fun testAddDecuplet6_8() {
    SAE(rest(crotchet(1)))
    SAE(
      EventType.TUPLET,
      eav(1),
      paramMapOf(EventParam.NUMERATOR to 10)
    )
    SVP(EventType.TUPLET, EventParam.DURATION, crotchet(1))
  }

  @Test
  fun testAddTupletVoice2() {
    SAE(
      EventType.TUPLET,
      eav(1, voice = 2),
      paramMapOf(EventParam.NUMERATOR to 3, EventParam.DENOMINATOR to 8)
    )
    SVE(EventType.TUPLET, eav(1, voice = 2))
  }


  @Test
  fun testGetTupletsInVoice() {
    SAE(
      EventType.TUPLET,
      eav(1),
      paramMapOf(EventParam.NUMERATOR to 3, EventParam.DENOMINATOR to 8)
    )
    SAE(
      EventType.TUPLET,
      eav(1, crotchet()),
      paramMapOf(EventParam.NUMERATOR to 3, EventParam.DENOMINATOR to 8)
    )
    val tuplets = EG().getEvents(EventType.TUPLET, eav(1, DURATION_WILD))
    assertEqual(4, tuplets?.size)
  }

  @Test
  fun testGetTupletAt() {
    SAE(
      EventType.TUPLET,
      eav(1),
      paramMapOf(EventParam.NUMERATOR to 3, EventParam.DENOMINATOR to 8)
    )
    SVPA(EventType.TUPLET, EventParam.NUMERATOR, 3, eav(1))
  }

  @Test
  fun testGetTupletNotAt() {
    SAE(
      EventType.TUPLET,
      eav(1),
      paramMapOf(EventParam.NUMERATOR to 3, EventParam.DENOMINATOR to 8)
    )
    SVNPA(EventType.TUPLET, EventParam.NUMERATOR, eav(1, crotchet()))
  }

  @Test
  fun testGetTupletAtMidBar() {
    SAE(
      EventType.TUPLET,
      eav(1, minim()),
      paramMapOf(EventParam.NUMERATOR to 3, EventParam.DENOMINATOR to 8)
    )
    SVPA(EventType.TUPLET, EventParam.NUMERATOR, 3, eav(1, minim()))
  }

  @Test
  fun testGetTupletAtMidTuplet() {
    SAE(
      EventType.TUPLET,
      eav(1, minim()),
      paramMapOf(EventParam.NUMERATOR to 3, EventParam.DENOMINATOR to 8)
    )
    SVPA(EventType.TUPLET, EventParam.NUMERATOR, 3, eav(1, minim().add(Duration(1, 12))))
  }

  @Test
  fun testGetTupletEndTuplet() {
    triplet()
    SVPA(EventType.TUPLET, EventParam.NUMERATOR, 3, eav(1, Duration(1, 6)))
  }

  @Test
  fun testGetTupletMembers() {
    triplet()
    SVP(
      EventType.TUPLET,
      EventParam.MEMBERS,
      listOf(dZero(), Duration(1, 12), Duration(1, 6)),
      eav(1)
    )
  }

  @Test
  fun testTupletVoiceMap() {
    SAE(
      EventType.TUPLET,
      eav(1),
      paramMapOf(EventParam.NUMERATOR to 3, EventParam.DENOMINATOR to 8)
    )
    assertEqual("R12:R12:R12:R4:R2", EG().getVoiceMap(eav(1))?.eventString())
  }

  @Test
  fun testTupletVoiceMapWithNotes() {
    triplet(true)
    assertEqual("C12:C12:C12:R4:R2", EG().getVoiceMap(eav(1))?.eventString())
  }

  @Test
  fun testDeleteTuplet() {
    triplet()
    SDE(EventType.TUPLET, eav(1))
    SVNE(EventType.TUPLET, eav(1))
  }

  @Test
  fun testDeleteTupletRemovesEvents() {
    triplet(denominator = 2)
    SDE(EventType.TUPLET, eav(1))
    SVNE(EventType.DURATION, eav(1))
  }

  @Test
  fun testDeleteTupletRange() {
    repeat(4) {
      triplet(offset = crotchet() * it)
    }
    SDE(EventType.TUPLET, eav(1), endAddress = eav(2))
    repeat(4) {
      SVNE(EventType.TUPLET, eav(1, crotchet() * it))
    }
  }

  @Test
  fun testDeleteNoteInTuplet() {
    triplet(true)
    SDE(EventType.NOTE, eav(1).copy(id = 1))
    SVP(EventType.DURATION, EventParam.TYPE, DurationType.REST, eav(1))
  }

  @Test
  fun testDeleteNoteInTupletFollowedByRests() {
    triplet(false)
    SMV(duration = quaver())
    SDE(EventType.NOTE, eav(1).copy(id = 1))
    SVP(EventType.DURATION, EventParam.TYPE, DurationType.REST, eav(1))
    SVP(EventType.DURATION, EventParam.DURATION, quaver(), eav(1))
  }

  @Test
  fun testDeleteTupletReplacedWithRest() {
    triplet()
    SMV(eventAddress = eav(1, minim()))
    SDE(EventType.TUPLET, eav(1))
    SVP(EventType.DURATION, EventParam.TYPE, DurationType.REST, eav(1))
    SVP(EventType.DURATION, EventParam.DURATION, minim(), eav(1))
  }

  @Test
  fun testAddNoteInTupletOffsetDifferentVoiceIsNoop() {
    SAE(
      EventType.TUPLET,
      eav(1),
      paramMapOf(EventParam.NUMERATOR to 3, EventParam.DENOMINATOR to 8)
    )
    SMV(eventAddress = eav(1, Offset(1, 12), voice = 2))
    SVNE(EventType.DURATION, eav(1, Offset(1, 12), 2))
  }

  @Test
  fun testRangeDeleteRemovesTuplet() {
    SAE(
      EventType.TUPLET,
      eav(1),
      paramMapOf(EventParam.NUMERATOR to 3, EventParam.DENOMINATOR to 8)
    )
    SDR(ea(1), ea(1, crotchet()))
    SVNE(EventType.TUPLET, eav(1))
  }

  @Test
  fun testRangeDeleteWithinTupletRemovesTuplet() {
    SAE(
      EventType.TUPLET,
      eav(1),
      paramMapOf(EventParam.NUMERATOR to 3, EventParam.DENOMINATOR to 2)
    )
    SDR(ea(1), ea(1, Offset(2,3)))
    SVNE(EventType.TUPLET, eav(1))
  }

  private fun triplet(
    withNotes: Boolean = false, offset: Offset = dZero(),
    numerator: Int = 3, denominator: Int = 8
  ) {
    SAE(
      EventType.TUPLET,
      eav(1, offset),
      paramMapOf(EventParam.NUMERATOR to numerator, EventParam.DENOMINATOR to denominator)
    )
    if (withNotes) {
      repeat(3) {
        SMV(
          duration = quaver(),
          eventAddress = eav(1, Offset(1, 12).multiply(it))
        )
      }
    }
  }

  @Test
  fun testSetTupletHardStart() {
    SAE(
      EventType.TUPLET,
      eav(1),
      paramMapOf(EventParam.NUMERATOR to 3, EventParam.DENOMINATOR to 2)
    )
    SSP(EventType.TUPLET, EventParam.HARD_START, Coord(20,20), eav(1))
    SVP(EventType.TUPLET, EventParam.HARD_START, Coord(20,20), eav(1))
  }

  private val tupletQuaver = quaver().multiply(Duration(2, 3))
}