package com.philblandford.kscore.api

import com.philblandford.kscore.engine.core.score.Meta
import com.philblandford.kscore.engine.duration.Duration
import com.philblandford.kscore.engine.duration.crotchet
import com.philblandford.kscore.engine.duration.dot
import com.philblandford.kscore.engine.tempo.Tempo
import com.philblandford.kscore.engine.time.TimeSignature
import com.philblandford.kscore.engine.types.*

data class Location(val page: Int, val x: Int, val y: Int)
data class Rectangle(val x: Int, val y: Int, val width: Int, val height: Int)
data class ScoreArea(val page: Int, val rectangle: Rectangle, val tag:String = "") {
  val x = rectangle.x
  val y = rectangle.y
  val width = rectangle.width
  val height = rectangle.height
}

typealias ProgressFunc = (title: String, subtitle: String, percent: Float) -> Boolean // true means cancel
typealias ProgressFunc2 = (subtitle: String, percent: Float) -> Boolean

val noProgress: ProgressFunc = { _, _, _ -> false }
val noProgress2: ProgressFunc2 = { _, _ -> false }

data class NoteInputDescriptor(
  val midiVal: Int = 60,
  val duration: Duration = crotchet(),
  val dots: Int = 0,
  val accidental: Accidental = Accidental.SHARP,
  val isHold: Boolean = false,
  val isTie: Boolean = false,
  val isNoEdit: Boolean = false,
  val isSmall: Boolean = false,
  val noteHeadType: NoteHeadType = NoteHeadType.NORMAL,
  val isPlusOctave: Boolean = false,
  val isDottedRhythm: Boolean = false,
  val graceType: GraceType = GraceType.NONE,
  val graceInputMode: GraceInputMode = GraceInputMode.NONE
) {

  fun toEvent(): Event {
    return Event(
      EventType.NOTE, paramMapOf(
        EventParam.MIDIVAL to midiVal,
        EventParam.DURATION to duration.dot(dots),
        EventParam.ACCIDENTAL to accidental,
        EventParam.GRACE_TYPE to graceType,
        EventParam.GRACE_MODE to graceInputMode,
        EventParam.ADD_OCTAVE to isPlusOctave,
        EventParam.DOTTED_RHYTHM to isDottedRhythm,
        EventParam.NOTE_HEAD_TYPE to noteHeadType,
        EventParam.IS_SMALL to isSmall,
        EventParam.TIE_TO_LAST to isTie,
        EventParam.HOLD to isHold
      )
    )
  }
}

data class PartDescriptor(
  val instrument: Instrument, val label: String = instrument.name,
  val abbreviation: String = instrument.abbreviation
)

data class NewScoreDescriptor(
  var instruments: Iterable<Instrument> = listOf(),
  var keySignature: Int = 0,
  var timeSignature: TimeSignature = TimeSignature(4, 4),
  var upbeatEnabled: Boolean = false,
  var upBeat: TimeSignature = TimeSignature(1, 4),
  var tempo: Tempo = Tempo(crotchet(), 120),
  var meta: Meta = Meta(),
  var numBars: Int = 32,
  var template: String? = "",
  var pageSize: PageSize = PageSize.A4
)



typealias Ks = Int