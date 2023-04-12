package com.philblandford.kscore.sound


import com.philblandford.kscore.engine.types.*
import com.philblandford.kscore.engine.types.EventType.*
import com.philblandford.kscore.engine.types.NavigationType.*
import com.philblandford.kscore.engine.dsl.dslChord
import com.philblandford.kscore.engine.duration.Duration
import com.philblandford.kscore.engine.map.EMK
import com.philblandford.kscore.engine.map.EventMapKey
import com.philblandford.kscore.engine.map.eventHashOf
import junit.framework.Assert.assertEquals
import org.junit.Test


/**
 * Created by philb on 21/11/16.
 */
class MidiBarListTest {

  @Test
  fun testGetBarListSimple() {
    val bars = midiBarList(eventHashOf(), 20)
    assertEquals((1..20).toList(), bars)
  }

  @Test
  fun testGetBarEndRepeat() {
    val hash = eventHashOf(
      EMK(
        REPEAT_END,
        ez(8)
      ) to Event(REPEAT_END)
    )
    val bars = midiBarList(hash, 20)
    assertEquals((1..8).plus(1..20), bars)
  }

  @Test
  fun testCreateMidiOffsetMap() {
    val start: Map<EventMapKey, Event> =
      eventHashOf()
    val hash = (1..20).fold(start) { h, n ->
      h.plus(
        EMK(
          DURATION,
          ez(n)
        ) to dslChord()
      )
    }
    val map = eventHashToOffsets(hash, (1..20)).first
    (0..19).forEach {
      assertEquals(
        dslChord(), map[EMK(
          DURATION,
          ez(0, Duration(it))
        )]
      )
    }
  }

  @Test
  fun testCreateMidiOffsetMapWithRepeat() {
    val start: Map<EventMapKey, Event> =
      eventHashOf()
    val hash = (1..20).fold(start) { h, n ->
      h.plus(
        EMK(
          DURATION,
          ez(n)
        ) to dslChord()
      )
    }
    val map = eventHashToOffsets(hash, (1..8).plus(1..8).plus(9..20)).first
    (0..27).forEach {
      assertEquals(
        dslChord(), map[EMK(
          DURATION,
          ez(0, Duration(it))
        )]
      )
    }
  }

  @Test
  fun testGetBarStartRepeat() {
    val hash = eventHashOf(
      eh(4, REPEAT_START),
      eh(8, REPEAT_END)
    )
    val bars = midiBarList(hash, 20)
    assertEquals((1..8).plus(4..20), bars)
  }

  @Test
  fun testGetBarTwoRepeats() {
    val hash = eventHashOf(
      eh(8, REPEAT_END),
      eh(9, REPEAT_START),
      eh(10, REPEAT_END)
    )
    val bars = midiBarList(hash, 20)
    assertEquals((1..8).plus(1..10).plus(9..20), bars)
  }

  @Test
  fun testGetBarThreeRepeats() {
    val hash = eventHashOf(
      eh(8, REPEAT_END),
      eh(9, REPEAT_START),
      eh(10, REPEAT_END),
      eh(11, REPEAT_START),
      eh(20, REPEAT_END)
    )
    val bars = midiBarList(hash, 20)
    assertEquals((1..8).plus(1..10).plus(9..20).plus(11..20), bars)
  }

  @Test
  fun testGetBarDaCapo() {
    val hash = eventHashOf(eh(8, NAVIGATION, DA_CAPO))
    val bars = midiBarList(hash, 20)
    assertEquals((1..8).plus(1..20), bars)
  }

  @Test
  fun testGetBarDaCapoAlFine() {
    val hash = eventHashOf(
      eh(8, NAVIGATION, DA_CAPO),
      eh(4, NAVIGATION, FINE)
    )
    val bars = midiBarList(hash, 20)
    assertEquals((1..8).plus(1..4), bars)
  }

  @Test
  fun testGetBarDaCapoAlFineEndRepeat() {
    val hash = eventHashOf(
      eh(8, NAVIGATION, DA_CAPO),
      eh(4, NAVIGATION, FINE),
      eh(4, REPEAT_END)
    )
    val bars = midiBarList(hash, 20)
    assertEquals((1..4).plus(1..8).plus(1..4).plus(1..4), bars)
  }

  @Test
  fun testGetBarDalSegno() {
    val hash = eventHashOf(
      eh(5, NAVIGATION, SEGNO),
      eh(8, NAVIGATION, DAL_SEGNO)
    )
    val bars = midiBarList(hash, 20)
    assertEquals((1..8).plus(5..20), bars)
  }

  @Test
  fun testGetBarCoda() {
    val hash = eventHashOf(
      eh(4, NAVIGATION, CODA),
      eh(11, NAVIGATION, DA_CAPO),
      eh(12, NAVIGATION, CODA)
    )
    val bars = midiBarList(hash, 20)
    assertEquals((1..11).plus(1..3).plus(12..20), bars)
  }

  @Test
  fun testGetBarCodaWithRepeats() {
    val hash = eventHashOf(
      eh(7, NAVIGATION, CODA),
      eh(8, REPEAT_END),
      eh(9, REPEAT_START),
      eh(12, NAVIGATION, DA_CAPO),
      eh(12, REPEAT_END),
      eh(13, NAVIGATION, CODA)
    )
    val bars = midiBarList(hash, 20)
    assertEquals((1..8).plus(1..8).plus(9..12).plus(9..12).plus(1..6).plus(13..20), bars)
  }

  @Test
  fun testGetBarVolta() {
    val hash = eventHashOf(
      eh(4, REPEAT_END),
      eh(4, VOLTA, extraParams = paramMapOf(EventParam.NUM_BARS to 1, EventParam.NUMBER to 1)),
      eh(5, VOLTA, extraParams = paramMapOf(EventParam.NUM_BARS to 1, EventParam.NUMBER to 2))
    )
    val bars = midiBarList(hash, 20)
    assertEquals((1..4).plus(1..3).plus(5..20), bars)
  }


  @Test
  fun testGetBarVolta3() {
    val hash = eventHashOf(
      eh(4, REPEAT_END),
      eh(4, VOLTA, extraParams = paramMapOf(EventParam.NUM_BARS to 1, EventParam.NUMBER to 1)),
      eh(5, REPEAT_END),
      eh(5, VOLTA, extraParams = paramMapOf(EventParam.NUM_BARS to 1, EventParam.NUMBER to 2)),
      eh(6, VOLTA, extraParams = paramMapOf(EventParam.NUM_BARS to 1, EventParam.NUMBER to 3))
    )
    val bars = midiBarList(hash, 20)
    assertEquals((1..4).plus(1..3).plus(5).plus(1..3).plus(6..20), bars)
  }


  @Test
  fun testGetBarDaCapoAlFineVoltas() {
    val hash = eventHashOf(
      eh(8, NAVIGATION, DA_CAPO),
      eh(6, NAVIGATION, FINE),
      eh(4, REPEAT_END),
      eh(4, VOLTA, extraParams = paramMapOf(EventParam.NUM_BARS to 1, EventParam.NUMBER to 1)),
      eh(5, VOLTA, extraParams = paramMapOf(EventParam.NUM_BARS to 1, EventParam.NUMBER to 2))
    )
    val bars = midiBarList(hash, 20)
    assertEquals(
      (1..4).plus(1..3).plus(5..8).plus(1..4).plus(1..3).plus(5..6), bars
    )
  }

  @Test
  fun testGetBarDalSegnoCapoAlFineVoltas() {
    val hash = eventHashOf(
      eh(2, NAVIGATION, SEGNO),
      eh(3, REPEAT_START),
      eh(10, NAVIGATION, DAL_SEGNO),
      eh(6, REPEAT_END),
      eh(6, VOLTA, extraParams = paramMapOf(EventParam.NUM_BARS to 1, EventParam.NUMBER to 1)),
      eh(7, VOLTA, extraParams = paramMapOf(EventParam.NUM_BARS to 1, EventParam.NUMBER to 2))
    )
    val bars = midiBarList(hash, 20)
    assertEquals(
      (1..6).plus(3..5).plus(7..10).plus(2..6).plus(3..5).plus(7..20), bars
    )
  }

  @Test
  fun testGetBarVoltaTwice() {
    val hash = eventHashOf(
      eh(4, REPEAT_END),
      eh(4, VOLTA, extraParams = paramMapOf(EventParam.NUM_BARS to 1, EventParam.NUMBER to 1)),
      eh(5, VOLTA, extraParams = paramMapOf(EventParam.NUM_BARS to 1, EventParam.NUMBER to 2)),
      eh(5, REPEAT_START),
      eh(10, REPEAT_END),
      eh(10, VOLTA, extraParams = paramMapOf(EventParam.NUM_BARS to 1, EventParam.NUMBER to 1)),
      eh(11, VOLTA, extraParams = paramMapOf(EventParam.NUM_BARS to 1, EventParam.NUMBER to 2))
    )
    val bars = midiBarList(hash, 20)
    assertEquals((1..4).plus(1..3).plus(5..10).plus(5..9).plus(11..20), bars)
  }

  @Test
  fun testGetBarVoltaTwiceAfterGap() {
    val hash = eventHashOf(
      eh(4, REPEAT_END),
      eh(4, VOLTA, extraParams = paramMapOf(EventParam.NUM_BARS to 1, EventParam.NUMBER to 1)),
      eh(5, VOLTA, extraParams = paramMapOf(EventParam.NUM_BARS to 1, EventParam.NUMBER to 2)),
      eh(7, REPEAT_START),
      eh(10, REPEAT_END),
      eh(10, VOLTA, extraParams = paramMapOf(EventParam.NUM_BARS to 1, EventParam.NUMBER to 1)),
      eh(11, VOLTA, extraParams = paramMapOf(EventParam.NUM_BARS to 1, EventParam.NUMBER to 2))
    )
    val bars = midiBarList(hash, 20)
    assertEquals((1..4).plus(1..3).plus(5..10).plus(7..9).plus(11..20), bars)
  }

  @Test
  fun testGetBarVoltaTwiceAfterGapLongVoltas() {
    val hash = eventHashOf(
      eh(4, REPEAT_END),
      eh(4, VOLTA, extraParams = paramMapOf(EventParam.NUM_BARS to 1, EventParam.NUMBER to 1)),
      eh(5, VOLTA, extraParams = paramMapOf(EventParam.NUM_BARS to 1, EventParam.NUMBER to 2)),
      eh(7, REPEAT_START),
      eh(12, REPEAT_END),
      eh(10, VOLTA, extraParams = paramMapOf(EventParam.NUM_BARS to 3, EventParam.NUMBER to 1)),
      eh(13, VOLTA, extraParams = paramMapOf(EventParam.NUM_BARS to 3, EventParam.NUMBER to 2))
    )
    val bars = midiBarList(hash, 32)
    assertEquals((1..4).plus(1..3).plus(5..12).plus(7..9).plus(13..32), bars)
  }

  @Test
  fun testGetBarListSimpleRange() {
    val bars = midiBarList(eventHashOf(), 20, 2, 5)
    assertEquals((2..5).toList(), bars)
  }

  private fun eh(
    bar: Int, eventType: EventType, subType: Any? = null,
    extraParams: ParamMap = paramMapOf()
  ): Pair<EventMapKey, Event> {
    val params = subType?.let { paramMapOf(EventParam.TYPE to it).plus(extraParams) } ?: extraParams
    return EMK(eventType, ez(bar)) to Event(eventType, params)
  }


}
