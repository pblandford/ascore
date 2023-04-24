package com.philblandford.kscore.engine.core.score.scorequery

import TestInstrumentGetter
import assertEqual
import com.philblandford.kscore.engine.core.score.*
import com.philblandford.kscore.engine.dsl.createScoreOneNote
import com.philblandford.kscore.engine.dsl.scoreAllCrotchets
import com.philblandford.kscore.engine.dsl.scoreBar2
import com.philblandford.kscore.engine.duration.*
import com.philblandford.kscore.engine.map.EMK
import com.philblandford.kscore.engine.map.EventMapKey
import com.philblandford.kscore.engine.eventadder.NewEventAdder
import com.philblandford.kscore.engine.eventadder.rightOrThrow
import com.philblandford.kscore.engine.pitch.harmony
import com.philblandford.kscore.engine.time.TimeSignature
import com.philblandford.kscore.engine.time.timeSignature
import com.philblandford.kscore.engine.types.*
import createCrotchets
import org.junit.Test
import com.philblandford.kscore.engine.scorefunction.ScoreTest

class GetEventsTest : ScoreTest() {

  @Test
  fun testGetEvent() {
    val score = createScoreOneNote()
    val res = score.getEvent(EventType.DURATION, eav(1))
    assertEqual(crotchet(), res!!.duration())
  }

  @Test
  fun testGetEventsNull() {
    val score = createScoreOneNote()
    val res = score.getEvents(EventType.DURATION)
    assertEqual(1, res?.size)
    assertEqual(crotchet(), res?.toList()?.first()?.second?.duration())
  }

  @Test
  fun testGetEventsBar() {
    val score = scoreBar2()
    val res = score.getEvents(EventType.DURATION, eav(2))
    assertEqual(1, res?.size)
  }

  @Test
  fun testGetEventsRepeatBar() {
    SMV()
    SAE(
      EventType.REPEAT_BAR,
      ea(2),
      paramMapOf(EventParam.NUMBER to 1)
    )
    val events = EG().getEvents(EventType.DURATION)!!
    assert(events[EMK(EventType.DURATION, eav(1))] != null)
    assert(events[EMK(EventType.DURATION, eav(2))] != null)
  }

  @Test
  fun testGetEventsRepeatBarTwice() {
    SMV()
    SAE(
      EventType.REPEAT_BAR,
      ea(2),
      paramMapOf(EventParam.NUMBER to 1)
    )
    SAE(
      EventType.REPEAT_BAR,
      ea(3),
      paramMapOf(EventParam.NUMBER to 1)
    )
    val events = EG().getEvents(EventType.DURATION)!!
    assert(events[EMK(EventType.DURATION, eav(1))] != null)
    assert(events[EMK(EventType.DURATION, eav(2))] != null)
    assert(events[EMK(EventType.DURATION, eav(3))] != null)
  }


  @Test
  fun testGetEventsForBar() {
    val score = createScoreOneNote()
    val events =
      score.getEvents(EventType.DURATION, ea(1).copy(offset = DURATION_WILD, voice = INT_WILD))
    assertEqual(1, events?.count())
  }

  @Test
  fun testGetEventsForBarTuplet() {
    SCD()
    SAE(
      tuplet(
        dZero(),
        3,
        crotchet()
      ).toEvent()
    )
    val events =
      EG().getEvents(EventType.TUPLET, ea(1).copy(offset = DURATION_WILD, voice = INT_WILD))
    assertEqual(2, events?.count())
  }

  @Test
  fun testGetEventTuplet() {
    SCD()
    SAE(
      tuplet(
        dZero(),
        3,
        crotchet()
      ).toEvent()
    )
    val event = EG().getEvent(EventType.TUPLET, eav(1))
    assertEqual(EventType.TUPLET, event?.eventType)
  }

  @Test
  fun testGetEventTupletEnd() {
    SCD()
    SAE(
      tuplet(
        dZero(),
        3,
        crotchet()
      ).toEvent()
    )
    val event = EG().getEvent(EventType.TUPLET, eav(1, Offset(1, 6)))
    assertEqual(true, event?.getParam(EventParam.END))
  }

  @Test
  fun testGetEventTupletLastBeat() {
    SCD()
    SMV()
    SMV(
      eventAddress = eav(
        1,
        minim()
      )
    )
    SAE(
      EventType.TUPLET, eav(1, minim(1)), paramMapOf(
        EventParam.NUMERATOR to 7,
        EventParam.DENOMINATOR to 16, EventParam.HIDDEN to false
      )
    )
    val event = EG().getEvent(EventType.TUPLET, eav(1, minim(1).add(Offset(6, 28))))
    assertEqual(true, event?.getParam(EventParam.END))
  }

  @Test
  fun testGetEventsForBarCorrectEventAddress() {
    val score = createScoreOneNote()
    val events =
      score.getEvents(EventType.DURATION, ea(1).copy(offset = DURATION_WILD, voice = INT_WILD))
    assertEqual(eav(1), events?.toList()?.first()?.first?.eventAddress)
  }


  @Test
  fun testGetEventAt() {
    val score =
      Score.create(
        TestInstrumentGetter(),
        4,
        TimeSignature(3, 4)
      )
    assertEqual(
      TimeSignature(3, 4),
      timeSignature(score.getEventAt(EventType.TIME_SIGNATURE, ea(2))!!.second)
    )
  }

  @Test
  fun testGetEventAtStaveEvent() {
    SCD()
    SAE(EventType.EXPRESSION_TEXT, ea(2), paramMapOf(EventParam.TEXT to "dolce", EventParam.IS_UP to true))
    assertEqual(
      "dolce",
      EG().getEventAt(EventType.EXPRESSION_TEXT, ea(3))?.second?.getParam(EventParam.TEXT)
    )
  }

  @Test
  fun testGetEventAtRetainsStaveId() {
    SCD()
    SAE(EventType.EXPRESSION_TEXT, ea(2), paramMapOf(EventParam.TEXT to "dolce", EventParam.IS_UP to true))
    assertEqual(
      StaveId(1,1),
      EG().getEventAt(EventType.EXPRESSION_TEXT, ea(3))?.first?.eventAddress?.staveId
    )
  }

  @Test
  fun testGetEventAtIgnoresVoice() {
    SCD()
    SAE(
      EventType.OCTAVE,
      ea(1),
      paramMapOf(EventParam.NUMBER to 1, EventParam.END to ea(3))
    )
    assert(EG().getEventAt(EventType.OCTAVE, eav(2)) != null)
  }

  @Test
  fun testGetEventsRange() {
    val score = scoreAllCrotchets(5)
    val events =
      score.getEvents(EventType.DURATION, eav(2, voice = INT_WILD), eav(3, voice = INT_WILD))
    assertEqual(5, events?.size)
    assertEqual(
      eav(2, dZero(), 1),
      events?.toList()?.sortedBy { it.first.eventAddress }?.first()?.first?.eventAddress
    )
  }

  @Test
  fun testGetEventsRangeTwoStaves() {
    SCDG()
    SMV()
    SMV(
      eventAddress = easv(
        1,
        dZero(),
        StaveId(1, 2)
      )
    )
    val events =
      EG().getEvents(EventType.DURATION, eav(1, voice = INT_WILD), eav(2, voice = INT_WILD))
    assertEqual(3, events?.size)
  }

  @Test
  fun testGetEventsRangeBarLevel() {
    SCD()
    SAE(
      EventType.HARMONY,
      eventAddress = ea(1),
      params = paramMapOf(EventParam.TEXT to "Cm7")
    )
    SAE(
      EventType.HARMONY,
      eventAddress = ea(2),
      params = paramMapOf(EventParam.TEXT to "Cm7")
    )

    val events =
      EG().getEvents(EventType.HARMONY, ea(1), ea(2, DURATION_WILD))
    assertEqual(2, events?.size)
  }

  @Test
  fun testGetEventsRangeWildCardAfter() {
    SCD()
    SAE(
      EventType.HARMONY,
      eventAddress = ea(1),
      params = paramMapOf(EventParam.TEXT to "Cm7")
    )
    SAE(
      EventType.HARMONY,
      eventAddress = ea(2),
      params = paramMapOf(EventParam.TEXT to "Cm7")
    )

    val events =
      EG().getEvents(EventType.HARMONY, ea(1), ea(8, DURATION_WILD))
    assertEqual(2, events?.size)
  }


  @Test
  fun testCollateEventsRangeWildCardAfter() {
    SCD()
    SAE(
      EventType.HARMONY,
      eventAddress = ea(1),
      params = paramMapOf(EventParam.TEXT to "Cm7")
    )
    SAE(
      EventType.HARMONY,
      eventAddress = ea(2),
      params = paramMapOf(EventParam.TEXT to "Cm7")
    )

    val events =
      EG().collateEvents(listOf(EventType.HARMONY), ea(1), ea(8, DURATION_WILD))
    assertEqual(2, events?.size)
  }


  @Test
  fun testGetEventsSegment() {
    var score =
      Score.create(
        TestInstrumentGetter(),
        4,
        TimeSignature(3, 4)
      )
    score = NewEventAdder.addEvent(
      score,
      EventType.DYNAMIC,
      paramMapOf(EventParam.TYPE to DynamicType.FORTE_PIANO),
      ea(1)
    ).rightOrThrow()
    score =
      NewEventAdder.addEvent(score, EventType.SLUR, paramMapOf(EventParam.END to ea(2)), ea(1))
        .rightOrThrow()
    val events = score.getAllEvents(ea(1))
    assertEqual(1, events.filter { it.key.eventType == EventType.DYNAMIC }.size)
    assertEqual(1, events.filter { it.key.eventType == EventType.SLUR }.size)
  }

  @Test
  fun testGetEventsRangeTuplet() {
    SCD()
    SAE(
      EventType.TUPLET, eav(1), paramMapOf(
        EventParam.NUMERATOR to 3,
        EventParam.DENOMINATOR to 4
      )
    )
    val events =
      EG().getEvents(EventType.DURATION, eav(1), eav(1, minim()))
    assertEqual(4, events?.size)
    repeat(3) {
      assertEqual(
        Duration(1, 6), events?.get(
          EMK(
            EventType.DURATION, eav(
              1,
              Offset(1, 6) * it
            )
          )
        )?.realDuration()
      )
    }
  }

  @Test
  fun testGetEventsRangeTupletVoiceWild() {
    SCD()
    SAE(
      EventType.TUPLET, eav(1), paramMapOf(
        EventParam.NUMERATOR to 3,
        EventParam.DENOMINATOR to 4
      )
    )
    val events =
      EG().getEvents(
        EventType.DURATION, eav(1, voice = INT_WILD), eav(
          1, minim(),
          INT_WILD
        )
      )
    assertEqual(4, events?.size)
    repeat(3) {
      assertEqual(
        Duration(1, 6), events?.get(
          EMK(
            EventType.DURATION, eav(
              1,
              Offset(1, 6) * it
            )
          )
        )?.realDuration()
      )
    }
  }

  @Test
  fun testCollateEventsRange() {
    SCD()
    SAE(
      harmony("Cm")!!.toEvent(),
      ea(1)
    )
    SAE(
      harmony("Cm")!!.toEvent(),
      ea(2)
    )
    val events = EG().collateEvents(listOf(EventType.HARMONY), ea(1), ea(7))!!
    assertEqual(2, events.size)
  }

  @Test
  fun testCollateEventsRangeManyNotes() {
    SCD()
    createCrotchets(4)
    val events = EG().collateEvents(listOf(EventType.DURATION), ea(1), ea(3))!!
    assertEqual(9, events.size)
  }

  @Test
  fun testCollateEventsMultipleParts() {
    SCD(
      instruments = listOf(
        "Violin",
        "Viola"
      )
    )
    SMV(eventAddress = ea(2))
    SMV(
      eventAddress = ea(1).copy(
        staveId = StaveId(2, 1)
      )
    )
    val events = EG().collateEvents(listOf(EventType.DURATION))!!
    assertEqual(6, events.size)
  }

  @Test
  fun testCollateEventsSinglePartMode() {
    SCD(
      instruments = listOf(
        "Violin",
        "Viola"
      )
    )
    SMV(eventAddress = ea(2))
    SMV(
      eventAddress = ea(1).copy(
        staveId = StaveId(2, 1)
      )
    )
    SSP(
      EventType.UISTATE,
      EventParam.SELECTED_PART,
      1
    )
    val events = EG().collateEvents(listOf(EventType.DURATION))!!
    assertEqual(3, events.size)
  }

  @Test
  fun testCollateEventsRangeSinglePartModeManyNotes() {
    SCD(
      instruments = listOf(
        "Violin",
        "Viola"
      )
    )
    createCrotchets(4)
    SSP(
      EventType.UISTATE,
      EventParam.SELECTED_PART,
      1
    )
    val events = EG().collateEvents(listOf(EventType.DURATION), ea(1), ea(3))!!
    assertEqual(9, events.size)
  }

  @Test
  fun testCollateEventsSinglePartModeManyNotesSecondPart() {
    SCD(
      instruments = listOf(
        "Violin",
        "Viola"
      )
    )
    createCrotchets(4, StaveId(2, 1))
    SSP(
      EventType.UISTATE,
      EventParam.SELECTED_PART,
      2
    )
    val events = EG().collateEvents(listOf(EventType.DURATION))!!
    assertEqual(16, events.size)
  }

  @Test
  fun testCollateEventsIncludesTie() {
    SCD()
    SMV(duration = breve())
    val events = EG().collateEvents(listOf(EventType.TIE))!!
    assert(events.any { it.value.eventType == EventType.TIE })
  }

  @Test
  fun testCollateEventsIncludesTieOnTuplet() {
    SCD()
    SAE(
      EventType.TUPLET,
      eav(1),
      paramMapOf(EventParam.NUMERATOR to 3, EventParam.DENOMINATOR to 8)
    )
    SMV(
      eventAddress = eav(
        1,
        Offset(1, 6)
      )
    )
    val events = EG().collateEvents(listOf(EventType.TIE))!!
    assert(
      events.containsKey(
        EventMapKey(
          EventType.TIE,
          eav(1, Duration(1, 6)).copy(id = 1)
        )
      )
    )
    assert(
      events.containsKey(
        EventMapKey(
          EventType.TIE,
          eav(1, crotchet()).copy(id = 1)
        )
      )
    )
  }

  @Test
  fun testCollateEventsIncludesTupletMembers() {
    SCD()
    SAE(
      EventType.TUPLET,
      eav(1),
      paramMapOf(EventParam.NUMERATOR to 3, EventParam.DENOMINATOR to 8)
    )
    val events = EG().collateEvents(listOf(EventType.TUPLET))!!
    assertEqual(
      listOf(dZero(), Offset(1, 12), Offset(1, 6)),
      events.toList().first().second.getParam(EventParam.MEMBERS)
    )
  }

  @Test
  fun testCollateEventsRepeatBar() {
    SCD()
    SMV()
    SAE(
      EventType.REPEAT_BAR,
      ea(2),
      paramMapOf(EventParam.NUMBER to 1)
    )
    val events = EG().collateEvents(listOf(EventType.DURATION))!!
    assert(events[EMK(EventType.DURATION, eav(1))] != null)
    assert(events[EMK(EventType.DURATION, eav(2))] != null)
  }


  @Test
  fun testGetEventsForStaveRange() {
    SCD()
    SAE(
      harmony("Cm")!!.toEvent(),
      ea(1)
    )
    SAE(
      harmony("Cm")!!.toEvent(),
      ea(2)
    )
    val events = EG().getEventsForStave(StaveId(1, 1), listOf(EventType.HARMONY), ea(1), ea(7))
    assertEqual(2, events.size)
  }

  @Test
  fun testGetEventsForStaveRangeIncludeBreak() {
    SCD()
    SAE(
      EventType.REPEAT_BAR,
      ea(2),
      paramMapOf(EventParam.NUMBER to 2)
    )
    val events = EG().getEventsForStave(StaveId(1, 1), listOf(EventType.REPEAT_BAR), ea(1), ea(7))
    assertEqual(1, events.size)
  }

  @Test
  fun testGetEventsVoiceWild() {
    SCD()
    SMV()
    val events = EG().getEvents(EventType.DURATION, ea(1).copy(voice = INT_WILD))
    assertEqual(3, events?.size)
  }

  @Test
  fun testGetEventsVoiceWildRange() {
    SCD()
    SMV()
    val events = EG().getEvents(
      EventType.DURATION, ea(1).copy(voice = INT_WILD),
      ea(1, minim()).copy(voice = INT_WILD)
    )
    assertEqual(3, events?.size)
  }

  @Test
  fun testGetEventsPartSelect() {
    SCD(
      instruments = listOf(
        "Violin",
        "Viola"
      )
    )
    SMV()
    SMV(
      eventAddress = easv(
        1,
        dZero(),
        StaveId(2, 1)
      )
    )
    sc.setSelectedPart(1)
    val events = EG().getEvents(EventType.DURATION)
    assertEqual(3, events?.size)
  }


}