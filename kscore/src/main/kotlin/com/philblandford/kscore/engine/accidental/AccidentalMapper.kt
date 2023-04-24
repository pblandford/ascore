package com.philblandford.kscore.engine.accidental

import com.philblandford.kscore.engine.core.score.Bar
import com.philblandford.kscore.engine.duration.Duration
import com.philblandford.kscore.engine.duration.Offset
import com.philblandford.kscore.engine.map.EventHash
import com.philblandford.kscore.engine.map.eventHashOf
import com.philblandford.kscore.engine.pitch.getAccidental
import com.philblandford.kscore.engine.types.*

typealias NoteLetterMap = Map<NoteLetter, EventHash>

fun mapBar(
  lastBar: Bar?, thisBar: Bar,
  ks: Int, newKs: Boolean
): Bar {

  val lastBarEvents =
    lastBar?.allVoiceEvents?.filter { it.key.eventType == EventType.DURATION } ?: eventHashOf()
  val thisBarEvents = thisBar.allVoiceEvents.filter { it.key.eventType == EventType.DURATION }

  return if (thisBarEvents.isNotEmpty()) {

    val newThisBar = mapAccidentals(lastBarEvents, thisBarEvents, ks, newKs)
    val grouped = newThisBar.toList().groupBy { it.first.eventAddress.voice }

    val newMaps = thisBar.voiceMaps.withIndex().map { vmIv ->
      grouped[vmIv.index + 1]?.let { group ->
        vmIv.value.replaceVoiceEvents(group.toMap())
      } ?: vmIv.value
    }

    Bar(thisBar.timeSignature, newMaps, thisBar.eventMap)
  } else {
    thisBar
  }
}

fun mapAccidentals(
  previousBarEvents: EventHash, thisBarEvents: EventHash,
  ks: Int, newKs: Boolean
): EventHash {

  val previous = groupNotes(previousBarEvents)
  val grouped = groupNotes(thisBarEvents)

  return thisBarEvents.map { (k, v) ->
    k to evaluateChord(
      v,
      k.eventAddress.offset,
      k.eventAddress.graceOffset,
      k.eventAddress.voice,
      grouped,
      previous,
      ks,
      newKs
    )
  }.toMap()
}

private fun groupNotes(chords: EventHash): NoteLetterMap {
  val l = chords.flatMap { chord ->
    chord.value.getParam<Iterable<Event>>(EventParam.NOTES)?.withIndex()
      ?.map { (idx, note) -> chord.key.copy(eventAddress = chord.key.eventAddress.copy(id = idx + 1)) to note }
      ?: listOf()
  }
  return l.groupBy {
    (it.second.getParam<Pitch>(EventParam.PITCH) ?: Pitch(NoteLetter.C)).noteLetter
  }.map {
    it.key to it.value.toMap().toSortedMap()
  }.toMap()
}

private fun evaluateChord(
  chord: Event,
  offset: Duration,
  graceOffset: Offset?,
  voice: Int,
  thisBarNotes: NoteLetterMap,
  lastBarNotes: NoteLetterMap,
  ks: Int,
  newKs: Boolean
): Event {
  val pitches = chord.getParam<Iterable<Event>>(EventParam.NOTES)?.mapNotNull {
    it.getParam<Pitch>(EventParam.PITCH)
  } ?: listOf()
  val notes = chord.getParam<Iterable<Event>>(EventParam.NOTES)?.map { note ->
    note.getParam<Pitch>(EventParam.PITCH)?.let { pitch ->
      val inKs = isInKs(pitch, ks)
      val differentThisChord = haveDifferentThisChord(pitch, pitches)
      val sameThisBar = haveSameThisBar(thisBarNotes, note, offset, graceOffset, voice)
      val differentThisBar = haveDifferentThisBar(thisBarNotes, note, offset, graceOffset, voice)
      val differentLastBar =
        !newKs && haveDifferentLastBar(lastBarNotes, thisBarNotes, note, offset)
      val yes =
        decideShow(inKs, differentThisChord, sameThisBar, differentThisBar, differentLastBar)
      note.addParam(EventParam.PITCH, pitch.copy(showAccidental = yes))
    } ?: note
  }
  return notes?.let { chord.addParam(EventParam.NOTES, it) } ?: chord
}

private fun decideShow(
  inKs: Boolean, differentThisChord: Boolean, sameThisBar: Boolean, differentThisBar: Boolean,
  differentLastBar: Boolean
): Boolean {
  return (!inKs && !sameThisBar) || differentThisChord || differentThisBar || differentLastBar
}

private fun noteAccidental(note: Event): Accidental? {
  return note.getParam<Pitch>(EventParam.PITCH)?.accidental
}

private fun noteOctave(note: Event): Int? {
  return note.getParam<Pitch>(EventParam.PITCH)?.octave
}


private fun haveDifferentThisChord(pitch: Pitch, otherPitches: Iterable<Pitch>): Boolean {
  return otherPitches.any { it.noteLetter == pitch.noteLetter && it.accidental != pitch.accidental }
}

private fun haveSameThisBar(
  noteLetterMap: NoteLetterMap,
  note: Event, offset: Offset, graceOffset: Offset?, voice: Int
): Boolean {
  return previousThisBar(
    noteLetterMap,
    note,
    offset,
    graceOffset,
    voice,
    includeThisOffset = false,
    sameOctave = false
  ) { one, two -> one == two }
}

private fun haveDifferentThisBar(
  noteLetterMap: NoteLetterMap,
  note: Event, offset: Offset, graceOffset: Offset?, voice: Int
): Boolean {
  return previousThisBar(
    noteLetterMap,
    note,
    offset,
    graceOffset,
    voice,
    sameOctave = true
  ) { one, two -> one != two }
}

private fun haveDifferentLastBar(
  lastBar: NoteLetterMap,
  thisBar: NoteLetterMap,
  note: Event, offset: Duration
): Boolean {


  return note.getParam<Pitch>(EventParam.PITCH)?.let { pitch ->

    thisBar[pitch.noteLetter]?.let { thisNote ->
      if (thisNote.toList().first().first.eventAddress.offset != offset) {
        return false
      }
    }

    lastBar[pitch.noteLetter]?.let { thisNote ->
      thisNote.toList().maxByOrNull { it.first.eventAddress.offset }?.let { last ->
        noteAccidental(last.second) != noteAccidental(note)
      }
    }
  } ?: false

}

private fun previousThisBar(
  noteLetterMap: NoteLetterMap,
  note: Event,
  offset: Offset,
  graceOffset: Offset?,
  voice: Int,
  includeThisOffset: Boolean = true,
  sameOctave: Boolean = true,
  test: (Accidental?, Accidental?) -> Boolean
): Boolean {
  return note.getParam<Pitch>(EventParam.PITCH)?.let { pitch ->

    val thisNote = noteLetterMap[pitch.noteLetter] ?: eventHashOf()

    fun matchGrace(eventAddress: EventAddress) =
      eventAddress.graceOffset?.let { thisGo ->
        graceOffset?.let { go -> eventAddress.offset == offset && thisGo < go } ?: run {
          eventAddress.offset == offset
        }
      } ?: false

    var previous = thisNote.toList()
      .takeWhile { it.first.eventAddress.offset < offset || matchGrace(it.first.eventAddress) }

    if (includeThisOffset) {
      thisNote.toList().find {
        it.first.eventAddress.offset == offset && it.first.eventAddress.graceOffset == graceOffset &&
            it.first.eventAddress.voice != voice
      }?.let {
        previous = previous + it
      }
    }

    if (!sameOctave) {
      val noteOctave = noteOctave(note)
      previous = previous.filter { noteOctave(it.second) == noteOctave }
    }
    previous.lastOrNull()?.let { pair ->
      test(noteAccidental(pair.second), noteAccidental(note))
    }
  } ?: false
}

private fun isInKs(pitch: Pitch, ks: Int): Boolean {
  return pitch.accidental == getAccidental(pitch.noteLetter, ks)
}