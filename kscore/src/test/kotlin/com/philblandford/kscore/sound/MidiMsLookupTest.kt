package com.philblandford.kscore.sound

import assertEqual
import com.philblandford.kscore.engine.types.EventType
import com.philblandford.kscore.engine.dsl.dslChord
import com.philblandford.kscore.engine.duration.*
import com.philblandford.kscore.engine.tempo.Tempo
import org.junit.Test

class MidiMsLookupTest {

  @Test
  fun testDurationToMs() {
    val tempo = Tempo(crotchet(), 120)
    val ms = durationToMs(crotchet(), tempo)
    assertEqual(60000 / 120, ms)
  }

  @Test
  fun testDurationToMsMinim() {
    val tempo = Tempo(crotchet(), 120)
    val ms = durationToMs(minim(), tempo)
    assertEqual((60000 / 120) * 2, ms)
  }

  @Test
  fun testDurationToMsQuaver() {
    val tempo = Tempo(crotchet(), 120)
    val ms = durationToMs(quaver(), tempo)
    assertEqual((60000 / 120) / 2, ms)
  }

  @Test
  fun testDurationToMsQuaverTempo() {
    val tempo = Tempo(quaver(), 120)
    val ms = durationToMs(quaver(), tempo)
    assertEqual(60000 / 120, ms)
  }

  @Test
  fun testDurationToMsCrotchetQuaverTempo() {
    val tempo = Tempo(quaver(), 120)
    val ms = durationToMs(crotchet(), tempo)
    assertEqual((60000 / 120) * 2, ms)
  }

  @Test
  fun testCreateLookup() {
    val el = createEventLookup(
      Tempo(crotchet(), 120),
      dZero() to crotchet(), crotchet() to crotchet()
    )
    val map = midiMsLookup(el)
    assertEqual(sortedMapOf(0 to dZero(), 500 to crotchet(), 1000 to minim()), map)
  }

  @Test
  fun testCreateLookupThreeNotes() {
    val el = createEventLookup(
      Tempo(crotchet(), 120), dZero() to crotchet(),
      crotchet() to crotchet(),
      minim() to crotchet()
    )
    val map = midiMsLookup(el)
    assertEqual(sortedMapOf(0 to dZero(), 500 to crotchet(), 1000 to minim(), 1500 to minim(1)), map)
  }

  @Test
  fun testCreateLookupFourNotes() {
    val el = createEventLookup(
      Tempo(crotchet(), 120), dZero() to crotchet(),
      crotchet() to crotchet(),
      minim() to crotchet(),
      minim(1) to crotchet()
    )
    val map = midiMsLookup(el)
    assertEqual(
      sortedMapOf(
        0 to dZero(), 500 to crotchet(), 1000 to minim(), 1500 to minim(1),
        2000 to semibreve()
      ), map
    )
  }

  @Test
  fun testCreateLookupTwoBars() {
    val el = createEventLookup(
      Tempo(crotchet(), 120), dZero() to crotchet(),
      crotchet() to crotchet(),
      minim() to crotchet(),
      semibreve() to crotchet(),
      semibreve().add(crotchet()) to crotchet()
    )
    val map = midiMsLookup(el)
    assertEqual(
      sortedMapOf(
        0 to dZero(), 500 to crotchet(), 1000 to minim(),
        1500 to minim(1),
        2000 to semibreve(), 2500 to semibreve().add(crotchet()), 3000 to semibreve(1)
      ), map
    )
  }

  @Test
  fun testCreateLookupQuaverTempo() {
    val el = createEventLookup(
      Tempo(quaver(), 120),
      dZero() to crotchet(),
      crotchet() to crotchet(),
      minim() to crotchet()
    )
    val map = midiMsLookup(el)
    assertEqual(
      sortedMapOf(0 to dZero(), 1000 to crotchet(), 2000 to minim(), 3000 to minim(1)),
      map
    )
  }

  @Test
  fun testCreateLookupTempoChange() {
    var el = createEventLookup(
      Tempo(crotchet(), 120),
      dZero() to semibreve(), semibreve() to semibreve()
    )
    el = el.addTempo(Tempo(crotchet(), 240), semibreve())
    val map = midiMsLookup(el)
    assertEqual(sortedMapOf(0 to dZero(), 2000 to semibreve(), 3000 to breve()), map)
  }

  private fun createEventLookup(tempo: Tempo, vararg events: Pair<Offset, Duration>): EventLookup {
    val durationEvents = events.toList().groupBy { it.first }.map { (offset, events) ->
      offset to mapOf(EventType.DURATION to events.map { ChannelEvent(0, dslChord(it.second)) })
    }.toMap()
    var zeroEvents = durationEvents[dZero()] ?: mapOf()
    zeroEvents = zeroEvents.plus(
      EventType.TEMPO to listOf(
        ChannelEvent(0, tempo.toEvent())
      )
    )
    return durationEvents.plus(dZero() to zeroEvents)
  }

  private fun EventLookup.addTempo(tempo: Tempo, offset: Offset):EventLookup {
    var map = this.get(offset) ?: mapOf()
    map = map.plus(EventType.TEMPO to listOf(ChannelEvent(0, tempo.toEvent())))
    return this.plus(offset to map)
  }
}