package com.philblandford.kscore.sound

import assertEqual
import com.philblandford.kscore.log.ksLogv
import com.philblandford.kscore.engine.types.*
import com.philblandford.kscore.engine.core.score.*
import com.philblandford.kscore.engine.dsl.rest
import com.philblandford.kscore.engine.duration.*

import com.philblandford.kscore.sound.transform.ORNAMENT_MAX_NOTE
import org.apache.commons.io.IOUtils
import org.junit.Test
import com.philblandford.kscore.engine.scorefunction.ScoreTest
import java.io.File
import java.nio.charset.Charset

class MidiPlayLookupTest : ScoreTest() {

  private val barMsAt120 = 2000
  private val crotchetMSAt120 = 500

  @Test
  fun testCreateMidiPlayLookup() {
    SMV()
    val builder = midiBuilder(EG(), instrumentGetter)
    val lookup = createMidiPlayLookup(builder, instrumentGetter, EG())
    assertEqual(2, lookup.getEvents(0)?.count())
  }

  @Test
  fun testCreateMidiPlayLookupTwoBars() {
    SMV()
    SMV(60, eventAddress = eav(2))
    val builder = midiBuilder(EG(), instrumentGetter)
    val lookup = createMidiPlayLookup(builder, instrumentGetter, EG())
    assertEqual(2, lookup.getEvents(0)?.count())
    assertEqual(1, lookup.getEvents(barMsAt120)?.count())
  }

  @Test
  fun testCreateMidiPlayLookupNote() {
    SMV(62)
    val builder = midiBuilder(EG(), instrumentGetter)
    val lookup = createMidiPlayLookup(builder, instrumentGetter, EG())
    assertEqual(
      NoteOnEvent(62, 100, 0),
      lookup.getEvents(0)?.filterIsInstance<NoteOnEvent>()?.first()
    )
  }

  @Test
  fun testCreateMidiPlayLookupFourNotes() {
    repeat(4) {
      SMV(62, eventAddress = eav(1, crotchet().multiply(it)))
    }
    val builder = midiBuilder(EG(), instrumentGetter)
    val lookup = createMidiPlayLookup(builder, instrumentGetter, EG())
    repeat(4) {
      assertEqual(
        NoteOnEvent(62, 100, 0),
        lookup.getNote(500 * it)
      )
      assertEqual(
        NoteOffEvent(62, 0),
        lookup.getEndNote(500 * (it + 1))
      )
    }
  }

  @Test
  fun testCreateMidiPlayLookupNullIfNothing() {
    SMV(62)
    val lookup = MPL()
    assert(lookup.getEvents(20) == null)
  }

  @Test
  fun testCreateMidiPlayLookupRestHasEntry() {
    SMV(62)
    val lookup = MPL()
    assert(lookup.getEvents(500) != null)
    assert(lookup.getEvents(1000) != null)
  }

  @Test
  fun testCreateMidiPlayLookupEmptyBarHasEntry() {
    SMV(eventAddress = eav(2))
    val lookup = MPL()
    assert(lookup.getEvents(0) != null)
    assert(lookup.getEvents(barMsAt120) != null)
  }

  @Test
  fun testCreateMidiPlayLookupTransposition() {
    SCDT()
    SMV(62)
    val builder = midiBuilder(EG(), instrumentGetter)
    val lookup = createMidiPlayLookup(builder, instrumentGetter, EG())
    assertEqual(NoteOnEvent(60, 100, 0), lookup.getNote())
  }

  @Test
  fun testCreateMidiPlayLookupNoteVelocity() {
    SAE(EventType.DYNAMIC, ea(1), paramMapOf(EventParam.TYPE to DynamicType.FORTISSIMO))
    SMV(62)
    val builder = midiBuilder(EG(), instrumentGetter)
    val lookup = createMidiPlayLookup(builder, instrumentGetter, EG())

    assertEqual(NoteOnEvent(62, velocities[DynamicType.FORTISSIMO]!!, 0), lookup.getNote())
  }

  @Test
  fun testCreateMidiPlayLookupTempoChange() {
    SMV(62)
    val builder = midiBuilder(EG(), instrumentGetter)
    val lookup = createMidiPlayLookup(builder, instrumentGetter, EG())
    assertEqual(
      NoteOnEvent(62, 100, 0),
      lookup.getEvents(0)?.filterIsInstance<NoteOnEvent>()?.first()
    )
  }

  @Test
  fun testPercussionChannel() {
    SCD(instruments = listOf("Bass Drum 1"))
    SMV(35)
    val builder = midiBuilder(EG(), instrumentGetter)
    val lookup = createMidiPlayLookup(builder, instrumentGetter, EG())
    assertEqual(9, lookup.getNote()?.channel)
  }

  @Test
  fun testPercussionMidiVal() {
    SCD(instruments = listOf("Bass Drum 1"))
    SMV(35)
    val builder = midiBuilder(EG(), instrumentGetter)
    val lookup = createMidiPlayLookup(builder, instrumentGetter, EG())
    assertEqual(35, lookup.getNote()?.midiVal)
  }

  @Test
  fun testTiedNote() {
    SMV(duration = breve())
    val lookup = MPL()
    assert(lookup.getNote(barMsAt120) == null)
    assert(lookup.getEndNote(4000) != null)
  }

  @Test
  fun testTiedNoteTuplet() {
    SAE(tuplet(dZero(), 3, 8).toEvent(), eav(1))
    SMV(duration = quaver())
    SMV(duration = quaver(), eventAddress = eav(1, Offset(1, 12)))
    SAE(EventType.TIE, eav(1).copy(id = 1))
    val lookup = MPL()
    assert(lookup.getNote(0) != null)
    assert(lookup.getNote(166) == null)
    assert(lookup.getEndNote(332) != null)
  }

  @Test
  fun testTiedNoteFromTuplet() {
    SAE(tuplet(dZero(), 3, 8).toEvent(), eav(1))
    SMV(duration = quaver(), eventAddress = eav(1, Offset(1, 6)))
    SMV(eventAddress = eav(1, crotchet()))
    SAE(EventType.TIE, eav(1, Offset(1, 6)))
    val lookup = MPL()
    assert(lookup.getNote(332) != null)
    assert(lookup.getNote(1000) == null)
    assert(lookup.getEndNote(barMsAt120) == null)
  }

  @Test
  fun testTiedNoteChained() {
    SMV(duration = longa())
    val lookup = MPL()
    assert(lookup.getNote(barMsAt120) == null)
    assert(lookup.getNote(4000) == null)
    assert(lookup.getNote(6000) == null)
    assert(lookup.getEndNote(8000) != null)
  }

  @Test
  fun testTiedNoteWithArticulation() {
    SMV(duration = breve())
    SAE(EventType.ARTICULATION, eav(1), paramMapOf(EventParam.TYPE to ArticulationType.TENUTO))
    val lookup = MPL()
    assert(lookup.getNote(barMsAt120) == null)
    assert(lookup.getEndNote(4000) != null)
  }

  @Test
  fun testTiedNoteMarked() {
    SMV(duration = breve())
    val lookup = MPL()
    assert(lookup.getEvents(barMsAt120) != null)
    assert(lookup.msToAddress(barMsAt120) != null)
  }

  @Test
  fun testTiedNoteWithShuffle() {
    SMV(duration = quaver())
    SMV(duration = quaver(), eventAddress = eav(1, quaver()))
    SMV(
      duration = crotchet(),
      eventAddress = eav(1, crotchet()),
      extraParams = paramMapOf(EventParam.TIE_TO_LAST to true)
    )
    SSO(EventParam.OPTION_SHUFFLE_RHYTHM, true)
    val lookup = MPL()
    assert(lookup.getNote(333) != null)
    assert(lookup.getNote(crotchetMSAt120) == null)
  }

  @Test
  fun testTiedNoteTieToLast() {
    SAE(rest())
    SMV(
      duration = quaver(),
      eventAddress = eav(1, crotchet()),
      extraParams = paramMapOf(EventParam.TIE_TO_LAST to true)
    )
    SMV(
      70,
      duration = quaver(),
      eventAddress = eav(1, crotchet(1)),
      extraParams = paramMapOf(EventParam.TIE_TO_LAST to true)
    )
    SMV(
      70,
      duration = crotchet(),
      eventAddress = eav(1, minim()),
      extraParams = paramMapOf(EventParam.TIE_TO_LAST to true)
    )
    val lookup = MPL()
    assert(lookup.getNote(crotchetMSAt120) != null)
    assert(lookup.getNote((crotchetMSAt120 * 1.5).toInt()) != null)
  }


  @Test
  fun testPedalEvent() {
    SAE(EventType.PEDAL, ea(1), paramMapOf(EventParam.END to ea(2)))
    val lookup = MPL()
    assert(lookup.getPedal()?.on == true)
    assert(lookup.getPedal(barMsAt120)?.on == false)
  }

  @Test
  fun testTrill() {
    SMV()
    SAE(EventType.ORNAMENT, eav(1), paramMapOf(EventParam.TYPE to OrnamentType.TRILL))
    val lookup = MPL()
    assert(lookup.getEvents(0) != null)
    assert(lookup.getEvents(ORNAMENT_MAX_NOTE) != null)
    assert(lookup.getEvents(ORNAMENT_MAX_NOTE * 2) != null)
    assert(lookup.getEvents(ORNAMENT_MAX_NOTE * 3) != null)
    assert(lookup.getEvents(ORNAMENT_MAX_NOTE * 4) != null)
  }

  @Test
  fun testLongTrill() {
    SMV(duration = semibreve())
    SAE(EventType.LONG_TRILL, eav(1), paramMapOf(EventParam.END to eav(1)))
    val lookup = MPL()
    val cnt = (crotchet() / ORNAMENT_MAX_NOTE).toInt()
    repeat(cnt) {
      assert(lookup.getEvents(ORNAMENT_MAX_NOTE * it) != null)
    }
  }

  @Test
  fun testTremolo() {
    SMV()
    SAE(EventType.TREMOLO, eav(1), paramMapOf(EventParam.TREMOLO_BEATS to quaver()))
    val lookup = MPL()
    assert(lookup.getEvents(0) != null)
    assert(lookup.getEvents(250) != null)
    assert(lookup.getEvents(500) != null)
  }

  @Test
  fun testTremoloTied() {
    SMV(duration = breve())
    SAE(EventType.TREMOLO, eav(1), paramMapOf(EventParam.TREMOLO_BEATS to quaver()))
    SAE(EventType.TREMOLO, eav(2), paramMapOf(EventParam.TREMOLO_BEATS to quaver()))
    val lookup = MPL()
    repeat(16) {
      assert(lookup.getEvents(250 * it)!!.count() > 0)
    }
  }

  @Test
  fun testCreateMidiPlayLookupEmptyBar() {
    SMV()
    SMV(eventAddress = eav(3))
    val lookup = MPL()
    assert(lookup.getEvents(0) != null)
    assert(lookup.getEvents(barMsAt120) != null)
    assert(lookup.getEvents(4000) != null)
  }

  @Test
  fun testMidScoreInstrument() {
    SMV()
    SAE(
      EventType.EXPRESSION_TEXT, eav(2), paramMapOf(
        EventParam.TEXT to "Violin",
        EventParam.IS_UP to false
      )
    )
    val lookup = MPL()
    assert(lookup.getEvents(barMsAt120)?.any { it is ProgramChangeEvent } == true)
  }

  @Test
  fun testMidScoreInstrumentBeforeStart() {
    SMV()
    SAE(
      EventType.EXPRESSION_TEXT, eav(2), paramMapOf(
        EventParam.TEXT to "Piano",
        EventParam.IS_UP to false
      )
    )
    val lookup = MPL(ea(3))
    assert(lookup.getEvents(0)!!.any { (it as ProgramChangeEvent).program == 1 })
  }


  @Test
  fun testCreateMidiPlayLookupRepeatBar() {
    SMV()
    SAE(EventType.REPEAT_BAR, ea(2), paramMapOf(EventParam.NUMBER to 1))
    val builder = midiBuilder(EG(), instrumentGetter)
    val lookup = createMidiPlayLookup(builder, instrumentGetter, EG())
    assertEqual(1, lookup.getEvents(barMsAt120)?.count())
  }

  @Test
  fun testCreateMidiPlayLookupRepeatBarWithRange() {
    SMV()
    SAE(EventType.REPEAT_BAR, ea(2), paramMapOf(EventParam.NUMBER to 1))
    val builder = midiBuilder(EG(), instrumentGetter, ea(1), ea(4))
    val lookup = createMidiPlayLookup(builder, instrumentGetter, EG())
    assertEqual(1, lookup.getEvents(barMsAt120)?.count())
  }

  @Test
  fun testAllOffsets() {
    SMV(duration = crotchet())
    val lookup = MPL()
    assertEqual(
      listOf(dZero(), crotchet(), minim(), semibreve()).toList(),
      lookup.allOffsets().toList()
    )
  }

  @Test
  fun testCreateMidiPlayLookupStart() {
    SMV(60)
    SMV(70, eventAddress = eav(3))
    val lookup = MPL(ea(3))
    assert(lookup.getEvents(0)!!.filterIsInstance<NoteOnEvent>().any { it.midiVal == 70 })
  }

  @Test
  fun testCreateMidiPlayLookupStartEnd() {
    SMV(60, endAddress = eav(3, minim(1)))
    val lookup = MPL(ea(1), ea(2, minim(1)))
    assertEqual(8, lookup.allOffsets().count())
  }

  @Test
  fun testGetMetaEvents() {
    val lookup = MPL()
    val metaEvents = lookup.getMetaEvents()
    assertEqual(3, metaEvents[dZero()]?.count())
  }

  @Test
  fun testNotesCreatedGraceBeforeRest() {
    SMV(eventAddress = eagv(1))
    val builder = midiBuilder(EG(), instrumentGetter)
    val lookup = createMidiPlayLookup(builder, instrumentGetter, EG())
    assert(lookup.getNote(0) != null)
  }

  @Test
  fun testNotesCreatedGraceFirstBeat() {
    SMV()
    SMV(eventAddress = eagv(1))
    val builder = midiBuilder(EG(), instrumentGetter)
    val lookup = createMidiPlayLookup(builder, instrumentGetter, EG())
    assert(lookup.getNote(0) != null)
  }

  private fun MPL(start: EventAddress? = null, end: EventAddress? = null): MidiPlayLookup {
    val builder = midiBuilder(EG(), instrumentGetter, start, end)
    return createMidiPlayLookup(builder, instrumentGetter, EG())
  }

  private fun MidiPlayLookup.getNote(ms: Int = 0): NoteOnEvent? {
    return getEvents(ms)?.filterIsInstance<NoteOnEvent>()?.firstOrNull()
  }

  private fun MidiPlayLookup.getEndNote(ms: Int = 0): NoteOffEvent? {
    return getEvents(ms)?.filterIsInstance<NoteOffEvent>()?.firstOrNull()
  }

  private fun MidiPlayLookup.getPedal(ms: Int = 0): PedalEvent? {
    return getEvents(ms)?.filterIsInstance<PedalEvent>()?.firstOrNull()
  }

}