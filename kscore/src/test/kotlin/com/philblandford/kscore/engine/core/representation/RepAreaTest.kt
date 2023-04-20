package com.philblandford.kscore.engine.core.representation

import assertEqual
import com.philblandford.kscore.api.Rectangle
import com.philblandford.kscore.api.ScoreArea
import com.philblandford.kscore.engine.core.area.AddressRequirement


import com.philblandford.kscore.engine.duration.breve
import com.philblandford.kscore.engine.duration.crotchet
import com.philblandford.kscore.engine.duration.dZero
import com.philblandford.kscore.engine.types.*
import com.philblandford.kscore.select.AreaToShow
import grace
import org.hamcrest.CoreMatchers.`is`
import org.hamcrest.MatcherAssert.assertThat
import org.junit.Test

class RepAreaTest : RepTest() {

  @Test
  fun testAreasAtAddress() {
    SMV()
    val areas = REP().getAreasAtAddress(ea(1))
    assert(areas.any { it.event.eventType == EventType.DURATION })
  }

  @Test
  fun testAreasAtAddressIncludesPreHeader() {
    SMV()
    val areas = REP().getAreasAtAddress(eas(1, staveId = StaveId(1, 0)))
    assert(areas.any { it.event.eventType == EventType.PART })
  }

  @Test
  fun testAreasAtAddressIncludesTempo() {
    SMV()
    val areas = REP().getAreasAtAddress(eas(1, staveId = StaveId(1, 0)))
    assert(areas.any { it.event.eventType == EventType.TEMPO })
  }

  @Test
  fun testAreasAtAddressIncludesTempoText() {
    SAE(EventType.TEMPO_TEXT, ea(1), paramMapOf(EventParam.TEXT to "Allegro"))
    val areas = REP().getAreasAtAddress(eas(1, staveId = StaveId(1, 0)))
    assert(areas.any { it.event.eventType == EventType.TEMPO_TEXT })
  }

  @Test
  fun testAreasAtAddressIncludesTie() {
    SMV(duration = breve())
    val areas = REP().getAreasAtAddress(ea(1))
    assert(areas.any { it.event.eventType == EventType.TIE })
  }

  @Test
  fun testAreasAtAddressIncludesLyric() {
    SAE(
      EventType.LYRIC, eav(1), paramMapOf(
        EventParam.TEXT to "hello",
        EventParam.NUMBER to 1
      )
    )
    val areas = REP().getAreasAtAddress(ea(1))
    assert(areas.any { it.event.eventType == EventType.LYRIC })
  }

  @Test
  fun testAreasAtAddressWithLyricIncludesDuration() {
    SMV()
    SAE(
      EventType.LYRIC, eav(1), paramMapOf(
        EventParam.TEXT to "hello",
        EventParam.NUMBER to 1
      )
    )
    val areas = REP().getAreasAtAddress(ea(1).copy(id = 1))
    assert(areas.any { it.event.eventType == EventType.LYRIC })
    assert(areas.any { it.event.eventType == EventType.DURATION })
  }

  @Test
  fun testAreasAtAddressIncludesUpDown() {
    SAE(EventType.SLUR, ea(1), paramMapOf(EventParam.IS_UP to true, EventParam.END to ea(2)))
    SAE(EventType.SLUR, ea(1), paramMapOf(EventParam.IS_UP to false, EventParam.END to ea(2)))
    val areas = REP().getAreasAtAddress(ea(1))
    assertEqual(2, areas.filter { it.event.eventType == EventType.SLUR }.size)
  }

  @Test
  fun testAreasAtAddressIncludesClefStartBar() {
    SAE(EventType.CLEF, ea(2), paramMapOf(EventParam.TYPE to ClefType.ALTO))
    val areas = REP().getAreasAtAddress(eas(2, staveId = StaveId(1, 1)))
    assert(areas.any { it.event.eventType == EventType.CLEF })
  }

  @Test
  fun testGetEventAddressByLocationSegment() {
    SMV()
    val expected = REP().getArea("Segment", ea(1))!!
    val coord = expected.first.coord
    val eventAddress =
      REP().getEventAddress(1, coord.x + 5, coord.y + 5, AddressRequirement.SEGMENT)
    assertEqual(ea(1), eventAddress)
  }

  @Test
  fun testGetEventAddressByLocationEmptyBar() {
    val expected = REP().getArea("Segment", ea(1))!!
    val coord = expected.first.coord
    val eventAddress =
      REP().getEventAddress(1, coord.x + 5, coord.y + 5, AddressRequirement.SEGMENT)
    assertEqual(ea(1), eventAddress)
  }

  @Test
  fun testGetEventAddressByLocationMiddleOfEmptyBar() {
    val expected = REP().getArea("Segment", ea(1))!!
    val coord = expected.first.coord
    val eventAddress =
      REP().getEventAddress(1, coord.x + expected.second.width/2, coord.y + 5, AddressRequirement.SEGMENT)
    assertEqual(ea(1), eventAddress)
  }

  @Test
  fun testGetEventAddressByLocationTwoParts() {
    RCD(instruments = listOf("Violin", "Viola"))
    val expected = REP().getArea("Segment", eas(1, 2 ,1))!!
    val coord = expected.first.coord
    val eventAddress =
      REP().getEventAddress(1, coord.x + expected.second.width/2, coord.y + 5, AddressRequirement.SEGMENT)
    assertEqual(eas(1,2,1), eventAddress)
  }

  @Test
  fun testGetEventAddressByLocationTwoStaves() {
    RCD(instruments = listOf("Piano"))
    val expected = REP().getArea("Segment", eas(1, 1 ,2))!!
    val coord = expected.first.coord
    val eventAddress =
      REP().getEventAddress(1, coord.x + expected.second.width/2, coord.y + 5, AddressRequirement.SEGMENT)
    assertEqual(eas(1,1,2), eventAddress)
  }

  @Test
  fun testGetEventAddressByLocationSegmentSecondCrotchet() {
    SMV()
    val expected = REP().getArea("Segment", ea(1, crotchet()))!!
    val coord = expected.first.coord
    val eventAddress =
      REP().getEventAddress(1, coord.x + 5, coord.y + 5, AddressRequirement.SEGMENT)
    assertEqual(ea(1, crotchet()), eventAddress)
  }

  @Test
  fun testGetEventAddressByLocationSegmentGrace() {
    grace()
    val expected = REP().getArea("Segment", eag(1))!!
    val coord = expected.first.coord
    val eventAddress =
      REP().getEventAddress(1, coord.x + 5, coord.y + 5, AddressRequirement.SEGMENT)
    assertEqual(eag(1, graceOffset =  dZero()), eventAddress)
  }

  @Test
  fun testGetEventAddressByLocationSegmentSecondBar() {
    SMV(eventAddress = ea(2))
    val expected = REP().getArea("Segment", ea(2))!!
    val coord = expected.first.coord
    val eventAddress =
      REP().getEventAddress(1, coord.x + 5, coord.y + 5, AddressRequirement.SEGMENT)
    assertEqual(ea(2), eventAddress)
  }

  @Test
  fun testGetEventAddressByLocationEvent() {
    SAE(
      EventType.DYNAMIC,
      params = paramMapOf(EventParam.TYPE to DynamicType.FORTE_PIANO, EventParam.IS_UP to true)
    )
    val expected = REP().getArea("Dynamic", ea(1))!!
    val coord = expected.first.coord
    val eventAddress =
      REP().getEventAddress(1, coord.x + 5, coord.y + 5, AddressRequirement.EVENT)
    assertEqual(ea(1), eventAddress)
  }

  @Test
  fun testGetAreaToShow() {
    SAE(EventType.TEMPO_TEXT, ez(1), paramMapOf(EventParam.TEXT to "Adagio"))
    val expected = REP().getArea("TempoText", ez(1))!!
    val coord = expected.first.coord
    val ats =
      REP().getAreaToShow(1, coord.x + 5, coord.y + 5)
    val expectedAts = AreaToShow(
      ScoreArea(
        1,
        Rectangle(
          expected.first.coord.x,
          expected.first.coord.y,
          expected.second.width,
          expected.second.height
        )
      ),
      expected.first.eventAddress, expected.second.event!!
    )
    assertEqual(expectedAts, ats)
  }

  @Test
  fun testGetAreaEventType() {
    SAE(EventType.TEMPO_TEXT, ez(1), paramMapOf(EventParam.TEXT to "Hello"))
    val area = REP().getArea(EventType.TEMPO_TEXT, ez(1))!!.area
    assertThat(area.event?.eventType, `is`(EventType.TEMPO_TEXT))
  }

  @Test
  fun testGetAreaEventTypeBarLine() {
    SAE(EventType.BARLINE, ez(2), paramMapOf(EventParam.TYPE to BarLineType.DOUBLE))
    val area = REP().getArea(EventType.BARLINE, eas(1, 1, 0))!!.area
    assertThat(area.event?.eventType, `is`(EventType.BARLINE))
  }

  @Test
  fun testGetEndBarSegment() {
    SSO(EventParam.OPTION_SHOW_MULTI_BARS, true)
    val num = REP().getEndBarForSegment(ea(1))
    assertThat(num, `is`(32))
  }

}