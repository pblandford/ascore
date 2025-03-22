package com.philblandford.kscore.engine.duration

import com.philblandford.kscore.engine.core.area.Coord
import com.philblandford.kscore.engine.eventadder.AnyResult
import com.philblandford.kscore.engine.eventadder.ok
import com.philblandford.kscore.engine.eventadder.subadders.ChordDecoration
import com.philblandford.kscore.engine.eventadder.then
import com.philblandford.kscore.engine.types.*
import com.philblandford.kscore.engine.types.EventParam.*
import com.philblandford.kscore.engine.util.removeAt
import com.philblandford.kscore.util.highestBit
import com.philblandford.kscore.util.numOnes
import org.apache.commons.math3.fraction.Fraction

typealias Duration = Fraction
typealias Offset = Fraction

val MAXIMA = Duration(8,1)
fun maxima(dots: Int = 0): Duration {
  return MAXIMA.dot(dots)
}

val LONGA = Duration(4,1)
fun longa(dots: Int = 0): Duration {
  return LONGA.dot(dots)
}

val BREVE = Duration(2,1)
fun breve(dots: Int = 0): Duration {
  return BREVE.dot(dots)
}

val SEMIBREVE = Duration(1,1)
fun semibreve(dots: Int = 0): Duration {
  return SEMIBREVE.dot(dots)
}

val MINIM = Duration(1,2)
fun minim(dots: Int = 0): Duration {
  return MINIM.dot(dots)
}

val CROTCHET = Duration(1,4)
fun crotchet(dots: Int = 0): Duration {
  return CROTCHET.dot(dots)
}

val QUAVER = Duration(1,8)
fun quaver(dots: Int = 0): Duration {
  return QUAVER.dot(dots)
}

val SEMIQUAVER = Duration(1,16)
fun semiquaver(dots: Int = 0): Duration {
  return SEMIQUAVER.dot(dots)
}

val DEMISEMIQUAVER = Duration(1,32)
fun demisemiquaver(dots:Int = 0): Duration {
  return DEMISEMIQUAVER.dot(dots)
}

val HEMIDEMISEMIQUAVER = Duration(1,64)
fun hemidemisemiquaver(dots:Int = 0): Duration {
  return HEMIDEMISEMIQUAVER.dot(dots)
}

fun dZero(): Duration {
  return DZERO
}

fun dWild(): Duration = DURATION_WILD

fun dMax(): Duration = DURATION_MAX

fun Duration.numDots(): Int {
  if (denominator == 1) return numerator.numOnes() - 1
  return (numerator + 1).highestBit() - 2
}

fun Int.reduceDots(num: Int): Int {
  var pos = highestBit() - num - 1
  var res = this
  while (pos > 0) {
    res = res and (1 shl (pos - 1)).inv()
    pos = pos shr 1
  }

  return res
}

fun Duration.undot(): Duration {

  val num = numerator.reduceDots(0)
  return Duration(num, denominator)
}

fun Duration.dot(numDots: Int = 0): Duration {
  var add = divide(2)
  var newVal = this
  repeat(numDots) {
    newVal = newVal.add(add)
    add = add.divide(2)
  }
  return newVal
}

fun Duration.toggleDotted(): Duration {
  return if (numDots() == 0) {
    dot()
  } else {
    undot()
  }
}

operator fun Duration.plus(other: Duration): Duration {
  return addC(other)
}

operator fun Duration.minus(other: Duration): Duration {
  return subtractC(other)
}

operator fun Duration.times(other: Duration): Duration {
  return multiplyC(other)
}

operator fun Duration.times(other: Int): Duration {
  return multiply(other)
}

operator fun Duration.div(other: Duration): Duration {
  return divideC(other)
}

operator fun Duration.div(other: Int): Duration {
  return divide(other)
}

fun max(one: Duration, two: Duration): Duration {
  return if (one > two) one else two
}

private val addCache = mutableMapOf<Pair<Duration, Duration>, Duration>()

fun Duration.addC(other: Duration): Duration {
  return try {
    addCache[Pair(this, other)] ?: run {
      val res = add(other)
      addCache[(Pair(this, other))] = res
      res
    }
  } catch (e:Exception) {
    this
  }
}

private val subtractCache = mutableMapOf<Pair<Duration, Duration>, Duration>()

fun Duration.subtractC(other: Duration): Duration {
  return subtractCache[Pair(this, other)] ?: run {
    val res = subtract(other)
    subtractCache[(Pair(this, other))] = res
    res
  }
}

private val multiplyCache = mutableMapOf<Pair<Duration, Duration>, Duration>()

fun Duration.multiplyC(other: Duration): Duration {
  return multiplyCache[Pair(this, other)] ?: run {
    val res = multiply(other)
    multiplyCache[(Pair(this, other))] = res
    res
  }
}

private val divideCache = mutableMapOf<Pair<Duration, Duration>, Duration>()

fun Duration.divideC(other: Duration): Duration {
  return divideCache[Pair(this, other)] ?: run {
    val res = divide(other)
    divideCache[(Pair(this, other))] = res
    res
  }
}


fun Event.duration() = params.g<Duration>(DURATION) ?: dZero()

fun Event.realDuration() = params.g<Duration>(REAL_DURATION) ?: duration()

fun Event.setDuration(duration: Duration) =
  addParam(DURATION to duration, REAL_DURATION to duration)

fun Event.asString(): String {
  return "${getLetter()}${if (realDuration().numerator == 1) "" else "${realDuration().numerator}/"}${realDuration().denominator}"
}

fun Event.getLetter(): String {
  if (eventType == EventType.NOTE) {
    return "N"
  }
  return when (subType) {
    DurationType.CHORD -> "C"
    DurationType.REST -> "R"
    DurationType.EMPTY -> "E"
    DurationType.TUPLET_MARKER -> "T"
    DurationType.REPEAT_BEAT -> "B"
    else -> ""
  }
}

private var noteEventCache = mutableMapOf<Note, Event>()

data class Note(
  val duration: Duration,
  val pitch: Pitch = unPitched(),
  val noteHeadType: NoteHeadType = NoteHeadType.NORMAL,
  val isSmall: Boolean = false,
  val isStartTie: Boolean = false,
  val isEndTie: Boolean = false,
  val endTie: Duration = dZero(),
  val position: Coord = Coord(),
  val realDuration: Duration = duration,
  val percussionId: Int = 0,
  val percussion: Boolean = false
) {
  fun toEvent(): Event {
    return noteEventCache[this] ?: run {
      val event = Event(
        EventType.NOTE, paramMapOf(
          DURATION to duration,
          REAL_DURATION to realDuration,
          PITCH to pitch,
          POSITION to position,
          NOTE_HEAD_TYPE to noteHeadType,
          IS_SMALL to isSmall,
          IS_START_TIE to isStartTie,
          IS_END_TIE to isEndTie,
          END_TIE to endTie,
          MIDIVAL to percussionId,
          PERCUSSION to percussion
        )
      )
      noteEventCache[this] = event
      event
    }
  }

  fun shiftOctave(amount:Int):Note {
    return copy(pitch = pitch.shiftOctave(amount))
  }
}

fun Event.pitch(): Pitch? = getParam(PITCH)

fun note(event: Event): Note? {
  return event.getParam<Duration>(DURATION)?.let { duration ->
    event.getParam<Pitch>(PITCH)?.let { pitch ->
      val realDuration = event.getParam<Duration>(REAL_DURATION) ?: duration
      val position = event.getParam<Coord>(POSITION) ?: Coord()
      val noteHeadType = event.getParam<NoteHeadType>(NOTE_HEAD_TYPE) ?: NoteHeadType.NORMAL
      val isSmall = event.getParam<Boolean>(IS_SMALL) ?: false
      val isStartTie = event.getParam<Boolean>(IS_START_TIE) ?: false
      val isEndTie = event.getParam<Boolean>(IS_END_TIE) ?: false
      val endTie = event.getParam<Duration>(END_TIE) ?: dZero()
      val midiId = event.getParam<Int>(MIDIVAL) ?: 0
      val percussion = event.isTrue(PERCUSSION)
      Note(
        duration,
        pitch,
        noteHeadType,
        isSmall,
        isStartTie,
        isEndTie,
        endTie,
        position,
        realDuration,
        midiId,
        percussion
      )
    }
  }
}

data class Chord(
  val duration: Duration,
  val notes: Iterable<Note>,
  val isUpstem: Modifiable<Boolean> = Modifiable(false, false),
  val realDuration: Duration = duration,
  val articulations: ChordDecoration<ArticulationType>? = null,
  val fingerings: ChordDecoration<Int>? = null,
  val ornament: ChordDecoration<Ornament>? = null,
  val arpeggio: ChordDecoration<ArpeggioType>? = null,
  val bowing: ChordDecoration<BowingType>? = null,
  val tremoloBeats: ChordDecoration<Duration>? = null,
  val isBeamed: Boolean = false,
  val isSlash: Boolean = false
) {
  fun upstem() = isUpstem.value

  fun toEvent(): Event {
    var event = Event(EventType.DURATION, paramMapOf(
      TYPE to DurationType.CHORD,
      DURATION to duration,
      IS_UPSTEM to isUpstem,
      IS_BEAMED to isBeamed,
      IS_SLASH to isSlash,
      REAL_DURATION to realDuration,
      NOTES to notes.map { it.toEvent() }
    ))
    articulations?.let { event = event.addParam(ARTICULATION, articulations) }
    fingerings?.let { event = event.addParam(FINGERING, fingerings) }
    ornament?.let { event = event.addParam(ORNAMENT, ornament) }
    arpeggio?.let { event = event.addParam(ARPEGGIO, arpeggio) }
    bowing?.let { event = event.addParam(BOWING, bowing) }
    tremoloBeats?.let { event = event.addParam(TREMOLO_BEATS, tremoloBeats) }
    return event
  }

  fun replaceNote(idx: Int, note: Note): Chord {
    val mut = notes.toMutableList()
    mut.removeAt(idx)
    mut.add(note)
    return copy(notes = mut.toList().sortedBy { it.position.y })
  }

  fun addNote(note: Note): Chord {
    val newNotes = notes.filterNot {
      if (it.percussion) {
        it.percussionId == note.percussionId
      } else {
        it.pitch == note.pitch
      }
    }.plus(note)

    return copy(notes = newNotes.sortedBy { it.position.y })
  }

  fun removeNote(idx: Int): Chord {
    return copy(notes = notes.toList().removeAt(idx))
  }

  fun removeNote(cond: (Note) -> Boolean): Chord {
    return copy(notes = notes.toList().filterNot(cond))
  }

  fun findNote(pitch: Pitch): Pair<Int, Note>? {
    return notes.withIndex().find { it.value.pitch.midiVal == pitch.midiVal }
      ?.let { it.index + 1 to it.value }
  }

  fun transformNotes(trans: (Note) -> Note): Chord {
    val newNotes = notes.map(trans)
    return copy(notes = newNotes)
  }

  fun transformNote(idx: Int, trans: (Note) -> Note): Chord {
    return notes.toList().getOrNull(idx)?.let(trans)?.let { newNote ->
      replaceNote(idx, newNote)
    } ?: this
  }

  fun transformNoteOrFail(idx: Int, trans: (Note) -> AnyResult<Note>): AnyResult<Chord> {
    return notes.toList().getOrNull(idx)?.let { note ->
      trans(note).then { replaceNote(idx, it).ok() }
    } ?: this.ok()
  }

  fun setDuration(duration: Duration, realDuration: Duration = duration): Chord {
    return copy(duration = duration, realDuration = realDuration).transformNotes {
      it.copy(duration = duration, realDuration = realDuration)
    }
  }
}

fun chord(event: Event): Chord? {
  return event.getParam<Duration>(DURATION)?.let { duration ->
    event.getParam<Iterable<Event>>(NOTES)?.let { notes ->
      val upStem = event.modifiable(IS_UPSTEM, false)
      val realDuration = event.getParam<Duration>(REAL_DURATION) ?: duration
      val articulations = event.getParam<ChordDecoration<ArticulationType>>(ARTICULATION)
      val fingerings = event.getParam<ChordDecoration<Int>>(FINGERING)
      val ornament = event.getParam<ChordDecoration<Ornament>>(ORNAMENT)
      val arpeggio = event.getParam<ChordDecoration<ArpeggioType>>(ARPEGGIO)
      val bowing = event.getParam<ChordDecoration<BowingType>>(BOWING)
      val tremoloBeats = event.getParam<ChordDecoration<Duration>>(TREMOLO_BEATS)
      val beamed = event.isTrue(IS_BEAMED)
      val slash = event.isTrue(IS_SLASH)
      Chord(
        duration,
        notes.mapNotNull { note(it) }.sortedBy { -it.pitch.midiVal },
        upStem,
        realDuration,
        articulations,
        fingerings,
        ornament,
        arpeggio,
        bowing,
        tremoloBeats,
        beamed,
        slash
      )
    }
  }
}

fun Event.isUpstem(): Boolean {
  return getParam<Boolean>(IS_UPSTEM_BEAM) ?: getParam<Boolean>(IS_UPSTEM) ?: false
}

