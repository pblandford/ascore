package com.philblandford.kscore.engine.accidental

import assertEqual
import com.philblandford.kscore.engine.dsl.dslChord
import com.philblandford.kscore.engine.types.*
import com.philblandford.kscore.engine.types.NoteLetter.*
import com.philblandford.kscore.engine.types.Accidental.*
import com.philblandford.kscore.engine.dsl.dslVoiceMap
import com.philblandford.kscore.engine.duration.*
import com.philblandford.kscore.engine.map.EMK
import com.philblandford.kscore.engine.map.EventHash
import com.philblandford.kscore.engine.map.EventMapKey
import com.philblandford.kscore.engine.map.eventHashOf
import org.junit.Test

class AccidentalMapperTest {

  @Test
  fun testMapNotKs() {
    val thisBar = dslVoiceMap { chord { pitch(A, SHARP) } }.getAllEvents()
    val res = mapAccidentals(eventHashOf(), thisBar, 0, false)
    assertEqual(true, show(res))
  }

  @Test
  fun testMapKs() {
    val thisBar = dslVoiceMap { chord { pitch(F, SHARP) } }.getAllEvents()
    val res = mapAccidentals(eventHashOf(), thisBar, 1, false)
    assertEqual(false, show(res))
  }

  @Test
  fun testMapNotKsOctave() {
    val thisBar = dslVoiceMap { chord { pitch(A, SHARP); pitch(A, SHARP, 5) } }.getAllEvents()
    val res = mapAccidentals(eventHashOf(), thisBar, 0, false)
    assertEqual(true, show(res, noteIdx = 0))
    assertEqual(true, show(res, noteIdx = 1))
  }

  @Test
  fun testMapNKsPreviousDifferentOctave() {
    val thisBar =
      dslVoiceMap { chord { pitch(A, SHARP) }; chord { pitch(A, NATURAL, 5) } }.getAllEvents()
    val res = mapAccidentals(eventHashOf(), thisBar, 0, false)
    assertEqual(true, show(res))
    assertEqual(true, show(res, crotchet()))
  }

  @Test
  fun testMapNotKsDifferentVoices() {
    val voice1 = dslVoiceMap { chord { pitch(A, SHARP) } }.getAllEvents()
    val voice2 = dslVoiceMap { chord { pitch(A, SHARP, 5) } }.getAllEvents()
      .map { it.key.copy(eventAddress = it.key.eventAddress.copy(voice = 2)) to it.value }
    val res = mapAccidentals(eventHashOf(), voice1.plus(voice2), 0, false)
    assertEqual(true, show(res, voice = 0))
    assertEqual(true, show(res, voice = 2))
  }

  @Test
  fun testMapPreviousAccidental() {
    val thisBar =
      dslVoiceMap { chord { pitch(F, SHARP) }; chord { pitch(F, SHARP) } }.getAllEvents()
    val res = mapAccidentals(eventHashOf(), thisBar, 0, false)
    assertEqual(true, show(res))
    assertEqual(false, show(res, crotchet()))
  }

  @Test
  fun testMapPreviousAccidentalDifferentVoice() {
    val voice1 = dslVoiceMap { chord { pitch(A, SHARP) } }.getAllEvents()
    val voice2 = dslVoiceMap { rest(); chord { pitch(A, SHARP, 5) } }.getAllEvents()
      .map { it.key.copy(eventAddress = it.key.eventAddress.copy(voice = 2)) to it.value }
    val res = mapAccidentals(eventHashOf(), voice1.plus(voice2), 0, false)
    assertEqual(true, show(res, voice = 0))
    assertEqual(true, show(res, crotchet(), voice = 2))
  }


  @Test
  fun testMapPreviousAccidentalDifferentTypesPrevious() {
    val thisBar = dslVoiceMap {
      chord { pitch(F, NATURAL) }; chord { pitch(F, SHARP) }; chord {
      pitch(
        F,
        NATURAL
      )
    }
    }.getAllEvents()
    val res = mapAccidentals(eventHashOf(), thisBar, 0, false)
    assertEqual(false, show(res))
    assertEqual(true, show(res, crotchet()))
    assertEqual(true, show(res, minim()))
  }

  @Test
  fun testMapAccidentalPreviousBar() {
    val lastBar = dslVoiceMap { chord { pitch(F, SHARP) } }.getAllEvents()
    val thisBar = dslVoiceMap { chord { pitch(F, NATURAL) } }.getAllEvents()
    val res = mapAccidentals(lastBar, thisBar, 0, false)
    assertEqual(true, show(res))
  }

  @Test
  fun testMapAccidentalPreviousBarSecondNote() {
    val lastBar = dslVoiceMap { chord { pitch(F, SHARP) } }.getAllEvents()
    val thisBar =
      dslVoiceMap { chord { pitch(F, NATURAL) }; chord { pitch(F, NATURAL) } }.getAllEvents()
    val res = mapAccidentals(lastBar, thisBar, 0, false)
    assertEqual(true, show(res))
    assertEqual(false, show(res, crotchet()))
  }

  @Test
  fun testMapAccidentalPreviousBarInKs() {
    val lastBar = dslVoiceMap { chord { pitch(F, NATURAL) } }.getAllEvents()
    val thisBar = dslVoiceMap { chord { pitch(F, SHARP) } }.getAllEvents()
    val res = mapAccidentals(lastBar, thisBar, 1, false)
    assertEqual(true, show(res))
  }

  @Test
  fun testMapAccidentalSameChord() {
    val thisBar = dslVoiceMap { chord { pitch(F, NATURAL); pitch(F, SHARP) } }.getAllEvents()
    val res = mapAccidentals(eventHashOf(), thisBar, 0, false)
    assertEqual(true, show(res))
  }

  @Test
  fun testMapAccidentalSameChordDifferentVoices() {
    var thisBar = dslVoiceMap { chord { pitch(F, NATURAL) } }.getAllEvents()
      .map { it.key.copy(eventAddress = it.key.eventAddress.copy(voice = 1)) to it.value }.toMap()
    thisBar += dslVoiceMap { chord { pitch(F, SHARP) } }.getAllEvents()
      .map { it.key.copy(eventAddress = it.key.eventAddress.copy(voice = 2)) to it.value }
    val res = mapAccidentals(eventHashOf(), thisBar, 0, false)
    assertEqual(true, show(res, voice = 1))
    assertEqual(true, show(res, voice = 2))
  }


  @Test
  fun testMapAccidentalSameChordAfterGrace() {
    val thisBar = dslVoiceMap { chord { pitch(F, NATURAL) } }.getAllEvents().plus(
      EMK(EventType.DURATION, eagv(0)) to dslChord { pitch(F, SHARP) })

    val res = mapAccidentals(eventHashOf(), thisBar, 0, false)
    assertEqual(true, show(res))
  }

  @Test
  fun testMapConsecutiveNoteLettersSame() {
    val thisBar =
      dslVoiceMap { chord { pitch(F, NATURAL) }; chord { pitch(F, SHARP) } }.getAllEvents()
    val res = mapAccidentals(eventHashOf(), thisBar, 0, false)
    assertEqual(false, show(res))
    assertEqual(true, show(res, crotchet()))
  }

  @Test
  fun testMapPreviousAccidentalGrace() {
    val thisBar =
      dslVoiceMap { chord { pitch(F, SHARP) }; chord { pitch(F, SHARP) } }.getAllEvents().map {
        it.key.copy(
          eventAddress = it.key.eventAddress.copy(
            offset = dZero(),
            graceOffset = it.key.eventAddress.offset
          )
        ) to it.value
      }.toMap()
    val res = mapAccidentals(eventHashOf(), thisBar, 0, false)
    assertEqual(true, show(res, graceOffset = dZero()))
    assertEqual(false, show(res, graceOffset = crotchet()))
  }

  @Test
  fun testMapAccidentalPreviousBarNewKs() {
    val lastBar = dslVoiceMap { chord { pitch(F, SHARP) } }.getAllEvents()
    val thisBar = dslVoiceMap { chord { pitch(F, NATURAL) } }.getAllEvents()
    val res = mapAccidentals(lastBar, thisBar, 0, true)
    assertEqual(false, show(res))
  }

  @Test
  fun testMapThreeChromaticNotes() {
    val thisBar = dslVoiceMap {
      chord { pitch(C, NATURAL) }
      chord { pitch(C, SHARP) }
      chord { pitch(D, NATURAL) }
    }.getAllEvents()
    val res = mapAccidentals(eventHashOf(), thisBar, 0, false)
    assertEqual(false, show(res))
    assertEqual(true, show(res, crotchet()))
    assertEqual(false, show(res, minim()))
  }

  private fun show(
    eventHash: EventHash, offset: Offset = dZero(),
    graceOffset: Offset? = null, voice: Voice = 0, noteIdx: Int = 0
  ): Boolean {
    return eventHash.get(
      EventMapKey(
        EventType.DURATION,
        ez(0, offset).copy(graceOffset = graceOffset, voice = voice)
      )
    )?.getParam<Iterable<Event>>(EventParam.NOTES)?.toList()?.get(noteIdx)
      ?.getParam<Pitch>(EventParam.PITCH)?.showAccidental!!
  }
}