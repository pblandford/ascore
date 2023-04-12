package com.philblandford.kscore.engine.map

import assertEqual
import com.philblandford.kscore.engine.core.score.OffsetLookupImpl
import com.philblandford.kscore.engine.core.score.offsetLookup
import com.philblandford.kscore.engine.duration.Duration
import com.philblandford.kscore.engine.types.*
import com.philblandford.kscore.engine.duration.crotchet
import com.philblandford.kscore.engine.duration.minim
import com.philblandford.kscore.engine.time.TimeSignature
import org.junit.Test

class RegionMapTest {

  @Test
  fun testCreateRegionMap() {
    val rm = RM(ez(1) to clef(ClefType.TREBLE))
    assertEqual(ClefType.TREBLE, rm[ez(1)]?.subType)
  }

  @Test
  fun testGetEventAfter() {
    val rm = RM(ez(1) to clef(ClefType.TREBLE))
    assertEqual(ClefType.TREBLE, rm[ez(2)]?.subType)
  }

  @Test
  fun testGetSecondEvent() {
    val rm = RM(
      ez(1) to clef(ClefType.TREBLE),
      ez(3) to clef(ClefType.BASS)
    )
    assertEqual(ClefType.BASS, rm[ez(3)]?.subType)
  }

  @Test
  fun testGetSecondEventAfter() {
    val rm = RM(
      ez(1) to clef(ClefType.TREBLE),
      ez(3) to clef(ClefType.BASS)
    )
    assertEqual(ClefType.BASS, rm[ez(4)]?.subType)
  }

  @Test
  fun testCreateRegionMapEndEvents() {
    val rm = RM(*longTrills(crotchet(), ea(1)).toTypedArray(), staveId = StaveId(1, 1))
    assert(rm[ea(1)] != null)
    assert(rm[ea(1, crotchet())] != null)
    assert(rm[ea(1, minim())] == null)
  }

  private fun longTrills(
    duration: Duration,
    eventAddress: EventAddress
  ): List<Pair<EventAddress, Event>> {
    val offsetLookup = offsetLookup(mapOf(1 to TimeSignature(4, 4)), 32)
    val end = offsetLookup.addDuration(eventAddress, duration)!!
    return listOf(
      eventAddress to Event(EventType.LONG_TRILL, paramMapOf(EventParam.DURATION to duration)),
      end to Event(
        EventType.LONG_TRILL,
        paramMapOf(EventParam.DURATION to duration, EventParam.END to true)
      )
    )
  }

  private fun clef(clefType: ClefType): Event {
    return Event(EventType.CLEF, paramMapOf(EventParam.TYPE to clefType))
  }

  private fun RM(
    vararg entries: Pair<EventAddress, Event>, numBars: Int = 4,
    staveId: StaveId = sZero()
  ): RegionMap {
    val eventType = entries.first().second.eventType
    val emks = entries.map { EMK(it.second.eventType, it.first) to it.second }
    val hash = eventHashOf(*emks.toTypedArray())
    return regionMap(hash, eventType, crotchets(numBars, staveId))
  }

  private fun crotchets(numBars: Int, staveId: StaveId = sZero()): Iterable<EventAddress> {
    return (1..numBars).flatMap { bar ->
      (0 until 4).map { offset ->
        EventAddress(bar, crotchet().multiply(offset), staveId = staveId)
      }
    }
  }
}