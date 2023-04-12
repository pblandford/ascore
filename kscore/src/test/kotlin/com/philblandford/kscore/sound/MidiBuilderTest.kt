package com.philblandford.kscore.sound

import assertEqual
import assertListEqual


import com.philblandford.kscore.engine.types.*
import com.philblandford.kscore.engine.duration.*
import com.philblandford.kscore.engine.pitch.harmony

import org.junit.Test
import com.philblandford.kscore.engine.scorefunction.ScoreTest

private typealias H = Horizontal

class MidiBuilderTest : ScoreTest() {

  @Test
  fun testCreateAddressList() {
    val addresses = createHorizontalList((1..1).toList(), listOf(H(1), H(1, crotchet())))
    assertEqual(listOf(H(1), H(1, crotchet())).toList(), addresses.toList())
  }

  @Test
  fun testCreateAddressListTwoBars() {
    val addresses = createHorizontalList(
      (1..2).toList(), listOf(H(1), H(1, crotchet()), H(2))
    )
    assertEqual(listOf(H(1), H(1, crotchet()), H(2)).toList(), addresses.toList())
  }

  @Test
  fun testCreateAddressListRepeatBar() {
    val addresses = createHorizontalList(
      (1..2).plus(1..2).toList(), listOf(H(1), H(1, crotchet()), H(2))
    )
    assertEqual(
      listOf(
        H(1),
        H(1, crotchet()),
        H(2),
        H(1),
        H(1, crotchet()),
        H(2)
      ).toList(), addresses.toList()
    )
  }


  @Test
  fun testCreateAddressListStartLater() {
    val addresses = createHorizontalList(
      (2..3).toList(), listOf(H(2), H(2, crotchet()), H(3))
    )
    assertEqual(listOf(H(2), H(2, crotchet()), H(3)).toList(), addresses.toList())
  }

  @Test
  fun testCreateOffsetToHorizontalMap() {
    repeat(4) { bar ->
      repeat(4) { offset ->
        SMV(eventAddress = eav(bar + 1, crotchet().multiply(offset)))
      }
    }
    val oth = getOTH(EG())
    assertEqual(H(), oth[dZero()])
    assertEqual(H(2), oth[semibreve()])
    assertEqual(H(2, crotchet()), oth[semibreve().add(crotchet())])
    assertEqual(H(3), oth[semibreve().multiply(2)])
  }

  @Test
  fun testCreateOffsetToHorizontalMapStartLater() {
    repeat(4) { bar ->
      repeat(4) { offset ->
        SMV(eventAddress = eav(bar + 1, crotchet().multiply(offset)))
      }
    }
    val oth = getOTH(EG(), 2, 3)
    assertEqual(H(2), oth[dZero()])
    assertEqual(H(3), oth[semibreve()])
  }

  @Test
  fun testCreateOffsetToHorizontalMapRepeatLater() {
    repeat(4) { bar ->
      repeat(4) { offset ->
        SMV(eventAddress = eav(bar + 1, crotchet().multiply(offset)))
      }
    }
    SAE(EventType.REPEAT_START, ez(3))
    SAE(EventType.REPEAT_END, ez(4))
    val oth = getOTH(EG())
    assertEqual(H(), oth[dZero()])
    assertEqual(H(2), oth[semibreve()])
    assertEqual(H(2, crotchet()), oth[semibreve().add(crotchet())])
    assertEqual(H(3), oth[semibreve().multiply(2)])
    assertEqual(H(3), oth[semibreve().multiply(2)])
    assertEqual(H(3), oth[semibreve().multiply(4)])
    assertEqual(H(4), oth[semibreve().multiply(5)])
  }

  @Test
  fun testCreateEventLookup() {
    SMV()
    SMV(eventAddress = eav(2))
    val lookup = getLookup(EG())
    assertEqual(2, lookup[dZero()]?.size)
    assertEqual(1, lookup[semibreve()]?.size)
  }

  @Test
  fun testCreateEventLookupThreeCrotchets() {
    SMV()
    SMV(eventAddress = eav(1, crotchet()))
    SMV(eventAddress = eav(1, minim()))
    val lookup = getLookup(EG())
    assertEqual(2, lookup[dZero()]?.size)
    assertEqual(1, lookup[crotchet()]?.size)
    assertEqual(1, lookup[minim()]?.size)
  }

  @Test
  fun testCreateEventLookupTwoBars() {
    repeat(2) { bar ->
      SMV(eventAddress = eav(bar + 1))
      SMV(eventAddress = eav(bar + 1, crotchet()))
      SMV(eventAddress = eav(bar + 1, minim()))
    }
    val lookup = getLookup(EG())
    assertEqual(2, lookup[dZero()]?.size)
    assertEqual(1, lookup[crotchet()]?.size)
    assertEqual(1, lookup[minim()]?.size)
    assertEqual(1, lookup[semibreve()]?.size)
    assertEqual(1, lookup[semibreve().add(crotchet())]?.size)
    assertEqual(1, lookup[semibreve().add(minim())]?.size)
  }

  @Test
  fun testCreateEventLookupWithRepeat() {
    SMV()
    SMV(eventAddress = eav(2))
    SAE(EventType.REPEAT_END, ez(2))
    val lookup = getLookup(EG())
    assertEqual(2, lookup[dZero()]?.size)
    assertEqual(1, lookup[semibreve()]?.size)
    assertEqual(2, lookup[semibreve().multiply(2)]?.size)
    assertEqual(1, lookup[semibreve().multiply(3)]?.size)
  }


  @Test
  fun testCreateChannelMap() {
    val map = createChannels(EG())
    assertEqual(
      mapOf(StaveId(1,1) to 0), map
    )
  }

  @Test
  fun testCreateChannelMap9NotUsed() {
    SCD(instruments = (1..10).map { "Violin" })
    val map = createChannels(EG())
    assertEqual(8, map[StaveId(9,1)])
    assertEqual(10, map[StaveId(10,1)])
  }

  @Test
  fun testCreateChannelMapPercussion() {
    SCD(instruments = listOf("Bass Drum 1"))
    val map = createChannels(EG())
    assertEqual(9, map[StaveId(1,1)])
  }

  @Test
  fun testCreateMidiBuilderStartLaterInstrumentsInitialised() {
    SMV()
    SMV(eventAddress = eav(2))
    val midiBuilder = midiBuilder(EG(), instrumentGetter, ea(2), ea(3))
    assert(midiBuilder.getEvents(dZero()).any { it.event.eventType == EventType.INSTRUMENT })
  }

  @Test
  fun testCreateMidiBuilderInitialInstrumentEvents() {
    SCD(instruments =listOf("Violin", "Viola"))
    val midiBuilder = midiBuilder(EG(), instrumentGetter)
    val events = midiBuilder.getEvents(dZero()).filter { it.event.eventType == EventType.INSTRUMENT }
    assertListEqual(listOf(0,1), events.toList().map { it.channel }.sorted())
  }

  @Test
  fun testCreateMidiBuilderStartLaterOffsetInstrumentsInitialised() {
    SMV(eventAddress = eav(2))
    SMV(eventAddress = eav(2, crotchet()))
    val midiBuilder = midiBuilder(EG(), instrumentGetter, ea(2, crotchet()), ea(3))
    assert(midiBuilder.getEvents(crotchet()).any { it.event.eventType == EventType.INSTRUMENT })
  }

  @Test
  fun testCreateMidiBuilderHarmonies() {
    SAE(harmony("C")!!.toEvent())
    SSO(EventParam.OPTION_HARMONY, true)
    val midiBuilder = midiBuilder(EG(), instrumentGetter)
    assert(midiBuilder.getEvents(dZero()).any { it.event.subType == DurationType.CHORD })
  }

  @Test
  fun testCreateMidiBuilderHarmonyInstrument() {
    SAE(harmony("C")!!.toEvent())
    SSO(EventParam.OPTION_HARMONY, true)
    val midiBuilder = midiBuilder(EG(), instrumentGetter)
    assert(midiBuilder.getEvents(dZero()).any { it.channel == 1 && it.event.eventType == EventType.INSTRUMENT })
  }

  @Test
  fun testCreateMidiBuilderHarmonyInstrumentOption() {
    SAE(harmony("C")!!.toEvent())
    SSO(EventParam.OPTION_HARMONY, true)
    SSO(EventParam.OPTION_HARMONY_INSTRUMENT, "Viola")
    val midiBuilder = midiBuilder(EG(), instrumentGetter)
    assert(midiBuilder.getEvents(dZero()).any {
      it.channel == 1 && it.event.getParam<String>(
        EventParam.NAME
      ) == "Viola"
    })
  }

  @Test
  fun testCreateMidiBuilderPartSelect() {
    SCD(instruments = listOf("Violin", "Viola"))
    SMV()
    sc.setSelectedPart(2)
    val midiBuilder = midiBuilder(EG(), instrumentGetter)
    assert(!midiBuilder.getEvents(dZero()).any { it.event.subType == DurationType.CHORD })
  }

  private fun getLookup(
    scoreQuery: ScoreQuery, start: Int? = null,
    end: Int? = null
  ): EventLookup {
    val events = scoreQuery.collateEvents(listOf(EventType.DURATION, EventType.TIME_SIGNATURE))!!
    val oth = getOTH(scoreQuery, start, end)
    val cm = createChannels(scoreQuery)
    return eventLookup(oth, events, cm, scoreQuery)
  }

  private fun getOTH(
    scoreQuery: ScoreQuery,
    start: Int? = null,
    end: Int? = null
  ): Map<Offset, Horizontal> {
    val events = getAllEvents(scoreQuery, start?.let { ea(it) }, end?.let { ea(it) }, instrumentGetter)
    val barList = midiBarList(scoreQuery, start, end)
    return offsetToHorizontalMap(barList, events)
  }
}

