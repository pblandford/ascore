package com.philblandford.kscore.engine.types

import assertEqual
import com.philblandford.kscore.engine.duration.crotchet
import com.philblandford.kscore.engine.duration.dWild
import com.philblandford.kscore.engine.duration.minim
import com.philblandford.kscore.engine.map.emptyEventMap
import org.junit.Test

class EventMapTest {

  @Test
  fun testAddEvent() {
    val em = emptyEventMap().putEvent(
      ea(1), Event(
        EventType.DYNAMIC,
        paramMapOf(EventParam.TYPE to DynamicType.FORTE)
      )
    )
    assertEqual(
      DynamicType.FORTE,
      em.getParam<DynamicType>(EventType.DYNAMIC, EventParam.TYPE, ea(1))
    )
  }

  @Test
  fun testDeleteEvent() {
    var em = emptyEventMap().putEvent(
      ea(1), Event(
        EventType.DYNAMIC,
        paramMapOf(EventParam.TYPE to DynamicType.FORTE)
      )
    )
    em = em.deleteEvent(ea(1), EventType.DYNAMIC)
    assert(em.getParam<DynamicType>(EventType.DYNAMIC, EventParam.TYPE, ea(1)) == null)
  }

  @Test
  fun testDeleteEventRange() {
    var em = emptyEventMap().putEvent(
      ea(1), Event(
        EventType.DYNAMIC,
        paramMapOf(EventParam.TYPE to DynamicType.FORTE)
      )
    )
    em = em.putEvent(
      ea(1, crotchet()), Event(
        EventType.DYNAMIC,
        paramMapOf(EventParam.TYPE to DynamicType.FORTE)
      )
    )
    em = em.deleteEvent(ea(1), EventType.DYNAMIC, ea(1, crotchet()))
    assert(em.getParam<DynamicType>(EventType.DYNAMIC, EventParam.TYPE, ea(1)) == null)
    assert(em.getParam<DynamicType>(EventType.DYNAMIC, EventParam.TYPE, ea(1, crotchet())) == null)
  }

  @Test
  fun testDeleteEventRangeWildOffset() {
    var em = emptyEventMap().putEvent(
      ea(1), Event(
        EventType.DYNAMIC,
        paramMapOf(EventParam.TYPE to DynamicType.FORTE)
      )
    )
    em = em.putEvent(
      ea(1, crotchet()), Event(
        EventType.DYNAMIC,
        paramMapOf(EventParam.TYPE to DynamicType.FORTE)
      )
    )
    em = em.deleteEvent(ea(1), EventType.DYNAMIC, ea(1, dWild()))
    assert(em.getParam<DynamicType>(EventType.DYNAMIC, EventParam.TYPE, ea(1)) == null)
    assert(em.getParam<DynamicType>(EventType.DYNAMIC, EventParam.TYPE, ea(1, crotchet())) == null)
  }

  @Test
  fun testGetEventsRange() {
    var em = add(emptyEventMap(), ea(1))
    em = add(em, ea(1, crotchet()))
    assertEqual(2, em.getEvents(EventType.DYNAMIC, ea(1), ea(1, crotchet()))?.size)
  }

  @Test
  fun testGetEventsRangeNotAll() {
    var em = add(emptyEventMap(), ea(1))
    em = add(em, ea(1, crotchet()))
    em = add(em, ea(1, minim()))
    assertEqual(2, em.getEvents(EventType.DYNAMIC, ea(1), ea(1, crotchet()))?.size)
  }


  @Test
  fun testGetEventsRangeNoEnd() {
    var em = add(emptyEventMap(), ea(1))
    em = add(em, ea(1, crotchet()))
    em = add(em, ea(1, minim()))
    assertEqual(3, em.getEvents(EventType.DYNAMIC, ea(1))?.size)
  }

  @Test
  fun testGetEventsRangeWildOffsetEnd() {
    var em = add(emptyEventMap(), ea(1))
    em = add(em, ea(2))
    val events = em.getEvents(EventType.DYNAMIC, ea(1), ea(1, dWild()))
    assertEqual(1, events?.size)
    assertEqual(ea(1), events?.toList()?.first()?.first?.eventAddress)
  }

  @Test
  fun testGetParamAt() {
    val em = emptyEventMap().putEvent(
      ea(1), Event(
        EventType.CLEF,
        paramMapOf(EventParam.TYPE to ClefType.BASS)
      )
    )
    assertEqual(ClefType.BASS, em.getParamAt<ClefType>(EventType.CLEF, EventParam.TYPE, ea(2)))
  }

  @Test
  fun testGetParamAtMidBar() {
    var em = emptyEventMap().putEvent(
      ea(1), Event(
        EventType.CLEF,
        paramMapOf(EventParam.TYPE to ClefType.TREBLE)
      )
    )
    em = em.putEvent(
      ea(1, minim()), Event(
        EventType.CLEF,
        paramMapOf(EventParam.TYPE to ClefType.BASS)
      )
    )
    assertEqual(ClefType.TREBLE, em.getParamAt<ClefType>(EventType.CLEF, EventParam.TYPE, ea(1)))
    assertEqual(
      ClefType.BASS,
      em.getParamAt<ClefType>(EventType.CLEF, EventParam.TYPE, ea(1, minim()))
    )
  }

  @Test
  fun testGetEventAfter() {
    val em = emptyEventMap().putEvent(
      ea(5), Event(
        EventType.CLEF,
        paramMapOf(EventParam.TYPE to ClefType.BASS)
      )
    )
    val res = em.getEventAfter(EventType.CLEF, ea(2))!!
    assertEqual(ea(5), res.first.eventAddress)
    assertEqual(ClefType.BASS, res.second.subType)
  }

  @Test
  fun testShiftEvents() {
    var map = (1..10).fold(emptyEventMap()) { em, num ->
      em.putEvent(
        ea(num), Event(
          EventType.NO_TYPE,
          paramMapOf(EventParam.NUMBER to num)
        )
      )
    }
    map = map.shiftEvents(4, 2)
    assertEqual(4, map.getParam<Int>(EventType.NO_TYPE, EventParam.NUMBER, ea(6)))
  }

  @Test
  fun testShiftEventsEventGone() {
    var map = (1..10).fold(emptyEventMap()) { em, num ->
      em.putEvent(
        ea(num), Event(
          EventType.NO_TYPE,
          paramMapOf(EventParam.NUMBER to num)
        )
      )
    }
    map = map.shiftEvents(4, 2)
    assert(map.getEvent(EventType.NO_TYPE, ea(4)) == null)
  }

  @Test
  fun testShiftEventsBack() {
    var map = (1..10).fold(emptyEventMap()) { em, num ->
      em.putEvent(
        ea(num), Event(
          EventType.NO_TYPE,
          paramMapOf(EventParam.NUMBER to num)
        )
      )
    }
    map = map.shiftEvents(4, -2)
    assertEqual(6, map.getParam<Int>(EventType.NO_TYPE, EventParam.NUMBER, ea(4)))
  }

  @Test
  fun testShiftEventsBackEventGone() {
    var map = (1..10).fold(emptyEventMap()) { em, num ->
      em.putEvent(
        ea(num), Event(
          EventType.NO_TYPE,
          paramMapOf(EventParam.NUMBER to num)
        )
      )
    }
    map = map.shiftEvents(4, -1)
    assertEqual(9, map.getEvents(EventType.NO_TYPE)?.size)
  }

  @Test
  fun testShiftEventsNotContinuous() {
    var map = emptyEventMap().putEvent(
      ea(3), Event(
        EventType.NO_TYPE,
        paramMapOf(EventParam.NUMBER to 3)
      )
    )
    map = map.shiftEvents(3, 2)
    assertEqual(3, map.getParam<Int>(EventType.NO_TYPE, EventParam.NUMBER, ea(5)))
  }

  @Test
  fun testShiftEventsBackEventGoneNotContinuous() {
    var map = emptyEventMap().putEvent(
      ea(3), Event(
        EventType.NO_TYPE,
        paramMapOf(EventParam.NUMBER to 3)
      )
    )
    map = map.shiftEvents(3, -1)
    assertEqual(0, map.getEvents(EventType.NO_TYPE)?.size)
  }

  @Test
  fun testDeleteRange() {
    var map = (1..4).fold(emptyEventMap()) { map, num ->
      map.putEvent(ez(num), Event(EventType.NO_TYPE))
    }
    map = map.deleteRange(ez(1), ez(4))
    assert(map.getEvents(EventType.NO_TYPE).isNullOrEmpty())
  }

  @Test
  fun testDeleteRangeAllTypes() {
    var map =
      (1..4).fold(emptyEventMap()) { map, num ->
        listOf(EventType.NO_TYPE, EventType.BOWING, EventType.TEMPO_TEXT).fold(map) { m, type ->
          m.putEvent(ez(num), Event(EventType.NO_TYPE))
        }
      }
    map = map.deleteRange(ez(1), ez(4))
    assert(map.getEvents(EventType.NO_TYPE).isNullOrEmpty())
  }

  @Test
  fun testDeleteRangeSpareMe() {
    var map = (0..3).fold(emptyEventMap()) { map, num ->
      map.putEvent(ez(1, crotchet().multiply(num)), Event(EventType.NO_TYPE))
    }
    map = map.deleteRange(ez(1), ez(4)) { ev, ea -> ea.offset == crotchet() }
    assertEqual(1, map.getEvents(EventType.NO_TYPE)?.size)
  }


  private fun add(eventMap: EventMap, eventAddress: EventAddress): EventMap {
    return eventMap.putEvent(
      eventAddress, Event(
        EventType.DYNAMIC,
        paramMapOf(EventParam.TYPE to DynamicType.FORTE)
      )
    )
  }
}