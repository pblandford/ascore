package com.philblandford.kscore.engine.types

import com.philblandford.kscore.engine.core.representation.COMPOSER_TEXT_SIZE
import com.philblandford.kscore.engine.core.representation.SUBTITLE_TEXT_SIZE
import com.philblandford.kscore.engine.core.representation.TITLE_TEXT_SIZE
import com.philblandford.kscore.engine.core.score.ScoreLevelType
import com.philblandford.kscore.engine.duration.*

typealias BarNum = Int
typealias PartNum = Int
typealias StaveNum = Int
typealias Voice = Int
typealias PageNum = Int

data class Event(val eventType: EventType, val params: ParamMap = paramMapOf()) {
  constructor(eventType: EventType, subType: Any, params: ParamMap) :
      this(eventType, params.plus(EventParam.TYPE to subType))

  val subType = params.g<Any>(EventParam.TYPE)

  fun addParam(param: EventParam, value: Any?): Event {
    return value?.let {
      copy(params = params.plus(param to it))
    } ?: copy(params = params.minus(param))
  }

  fun addParam(vararg params: Pair<EventParam, Any?>): Event {
    return params.fold(this) { e, (k, v) ->
      e.addParam(k, v)
    }
  }

  fun addParams(paramMap: ParamMap): Event {
    return copy(params = params.plus(paramMap))
  }

  fun removeParam(param: EventParam): Event {
    return copy(params = params.minus(param))
  }

  fun removeParams(vararg param: EventParam): Event {
    return copy(params = params.minus(param))
  }

  fun <T> getParam(param: EventParam): T? {
    return when (val value = params[param]) {
      is Modifiable<*> -> value.value as? T?
      else -> value as? T?
    }
  }

  fun getInt(param: EventParam, default: Int = 0): Int {
    return getParam(param) ?: default
  }


  fun getIntOrNull(param: EventParam, default: Int = 0): Int? {
    return getParam(param)
  }


  fun getString(param: EventParam, default: String = ""): String {
    return getParam(param) ?: default
  }

  fun isTrue(eventParam: EventParam): Boolean {
    return params.isTrue(eventParam)
  }

  fun <T> setModValue(eventParam: EventParam, value: T, user: Boolean = false): Event {
    return addParam(eventParam, Modifiable(user, value))
  }

  inline fun <reified T> modifiable(eventParam: EventParam, default: T): Modifiable<T> {
    return when (val v = params[eventParam]) {
      is Modifiable<*> -> v as Modifiable<T>
      is T -> Modifiable(false, v)
      else -> Modifiable(false, default)
    }
  }

  fun isModified(eventParam: EventParam): Boolean {
    return when (val v = params[eventParam]) {
      is Modifiable<*> -> v.modified
      else -> false
    }
  }

  fun isUp(): Boolean {
    return isTrue(EventParam.IS_UP)
  }

  fun number(default: Int): Int {
    return getParam<Int>(EventParam.NUMBER) ?: default
  }
}

fun ParamMap.isTrue(eventParam: EventParam): Boolean {
  return when (val v = this[eventParam]) {
    is Boolean -> v
    is Modifiable<*> -> v.value == true
    else -> false
  }
}

const val INT_WILD = -1
val STAVE_ID_WILD = StaveId(-1, -1)
val DURATION_WILD = Duration(Int.MAX_VALUE)
val DURATION_MAX = Duration(Int.MAX_VALUE, 1)
val DZERO = Duration(0, 1)
fun Int.isWild() = this == INT_WILD
fun StaveId.isWild() = this == STAVE_ID_WILD
fun Duration.isWild() = this == DURATION_WILD

data class StaveId(val main: Int, val sub: Int) : Comparable<StaveId> {
  override fun compareTo(other: StaveId): Int {
    var ret = main - other.main
    if (ret == 0) ret = sub - other.sub
    return ret
  }
}

data class Horizontal (
  val barNum: Int = 1,
  val offset: Offset = dZero(),
  val graceOffset: Offset? = null
) : Comparable<Horizontal> {
  override fun compareTo(other: Horizontal): Int {
    var ret = barNum - other.barNum
    if (ret == 0) ret = offset.compareTo(other.offset)
    if (ret == 0) ret = (graceOffset ?: dZero()).compareTo(other.graceOffset ?: dZero())
    return ret
  }
}

fun hz(offset: Offset) = Horizontal(0, offset)
val HZERO = hz(DZERO)
fun hZero() = HZERO

data class EventAddress(
  val barNum: BarNum = 0, val offset: Offset = dZero(),
  val graceOffset: Offset? = null,
  val staveId: StaveId = StaveId(0, 0), val voice: Voice = 0, val id: Int = 0
) : Comparable<EventAddress> {

  val isGrace = graceOffset != null

  fun match(other: EventAddress): Boolean {
    return (other.barNum.isWild() || barNum == other.barNum) &&
        (other.graceOffset?.isWild() ?: false || graceOffset == other.graceOffset) &&
        (other.staveId.isWild() || staveId == other.staveId) &&
        (other.voice.isWild() || voice == other.voice) &&
        (other.id.isWild() || id == other.id)
  }

  fun lt(other: EventAddress): Boolean {
    return compareTo(other) < 0
  }

  fun gt(other: EventAddress): Boolean {
    return compareTo(other) > 0
  }

  fun lte(other: EventAddress): Boolean {
    return compareTo(other) <= 0
  }

  fun gte(other: EventAddress): Boolean {
    return compareTo(other) >= 0
  }

  operator fun inc(num: Int = 1): EventAddress {
    return copy(barNum = barNum + num)
  }

  operator fun dec(num: Int = 1): EventAddress {
    return if (barNum > num) {
      copy(barNum = barNum - num)
    } else {
      this
    }
  }

  fun startBar(): EventAddress {
    return copy(offset = dZero(), graceOffset = null)
  }

  fun start(): EventAddress {
    return copy(barNum = 1, offset = dZero(), graceOffset = null)
  }

  fun isStart(): Boolean {
    return barNum == 1 && offset == dZero() && graceOffset == null
  }

  fun isWild() = this == EWILD

  val horizontal = Horizontal(barNum, offset, graceOffset)

  fun ifGraceOffset(): Offset = if (isGrace) graceOffset ?: dZero() else offset
  fun setIfGraceOffset(offset: Duration): EventAddress {
    return if (isGrace) {
      copy(graceOffset = offset)
    } else {
      copy(offset = offset)
    }
  }

  fun addIfGraceOffset(duration: Duration): EventAddress {
    return if (isGrace) {
      copy(graceOffset = (graceOffset ?: dZero()).addC(duration))
    } else {
      copy(offset = offset.addC(duration))
    }
  }

  fun voiceless(): EventAddress = EventAddress(barNum, offset, graceOffset, staveId, id = id)
  fun idless(): EventAddress = EventAddress(barNum, offset, graceOffset, staveId, voice)
  fun voiceIdless(): EventAddress = EventAddress(barNum, offset, graceOffset, staveId)
  fun staveless(): EventAddress = EventAddress(barNum, offset, graceOffset, szero)
  fun stavelessWithId(): EventAddress = EventAddress(barNum, offset, graceOffset, szero, id = id)
  fun barless(): EventAddress = EventAddress(0, offset, graceOffset, szero)
  fun graceless() = EventAddress(barNum, offset, null, staveId, voice, id)

  operator fun plus(num: Int): EventAddress = copy(barNum = barNum + num)
  operator fun minus(num: Int): EventAddress = if (barNum > 0) copy(barNum = barNum - num) else this

  override operator fun compareTo(other: EventAddress): Int {
    var ret = barNum - other.barNum
    if (ret == 0) ret = offset.compareTo(other.offset)
    if (ret == 0) ret = (graceOffset ?: dWild()).compareTo(other.graceOffset ?: dWild())
    if (ret == 0) ret = staveId.compareTo(other.staveId)
    if (ret == 0) ret = voice - other.voice
    if (ret == 0) ret = id - other.id
    return ret
  }
}

data class Happening(
  val eventAddress: EventAddress, val event: Event, val add: Boolean,
  val endAddress: EventAddress? = null,
  val originalAddress: EventAddress = eventAddress,
  val destination: ScoreLevelType? = null
)

fun Happening.barRange(scoreBars: Int): Iterable<Int> {
  val start = if (eventAddress.isWild()) 1 else eventAddress.barNum
  val end = if (eventAddress.isWild()) scoreBars else endAddress?.barNum ?: start
  return start..end
}

val szero = StaveId(0, 0)
fun sZero() = szero
val ezero = EventAddress(0, dZero(), null, sZero(), 0, 0)
fun eZero() = ezero
fun ea(barNum: Int, offset: Duration = dZero()) =
  EventAddress(barNum, offset, null, StaveId(1, 1), 0, 0)

fun eav(barNum: Int, offset: Duration = dZero(), voice: Int = 1) =
  EventAddress(barNum, offset, null, StaveId(1, 1), voice, 0)

fun eas(barNum: Int, offset: Duration = dZero(), staveId: StaveId = sZero()) =
  EventAddress(barNum, offset, null, staveId, 0, 0)

fun eas(barNum: Int, main: Int, sub: Int, offset: Offset = dZero()) =
  EventAddress(barNum, offset, null, StaveId(main, sub), 0, 0)

fun easv(barNum: Int, offset: Duration = dZero(), staveId: StaveId = sZero(), voice: Int = 1) =
  EventAddress(barNum, offset, null, staveId, voice, 0)

fun easv(barNum: Int, main: Int, sub: Int, offset: Offset = dZero(), voice: Int = 1) =
  EventAddress(barNum, offset, null, StaveId(main, sub), voice, 0)


fun eag(
  barNum: Int = 1,
  offset: Duration = dZero(),
  graceOffset: Offset? = dZero()
) =
  EventAddress(barNum, offset, graceOffset, StaveId(1, 1), 0, 0)

fun eagv(
  barNum: Int = 1,
  offset: Duration = dZero(),
  graceOffset: Offset? = dZero(),
  voice: Int = 1
) =
  EventAddress(barNum, offset, graceOffset, StaveId(1, 1), voice, 0)

fun ez(barNum: Int, offset: Duration = dZero()) = EventAddress(barNum, offset, null, sZero(), 0, 0)

private val EWILD = EventAddress(INT_WILD, dWild())
fun eWild() = EWILD

typealias ParamMap = Map<EventParam, Any?>

@Suppress("UNCHECKED_CAST")
fun <T> ParamMap.g(eventParam: EventParam): T? {
  return get(eventParam) as? T
}

fun paramMapOf(vararg args: Pair<EventParam, Any?>): ParamMap {
  return hashMapOf(*args)
}

fun ParamMap.getValues(keys: Collection<EventParam>): ParamMap {
  return filter { keys.contains(it.key) }
}

enum class EventType {
  DURATION,
  NOTE,
  TUPLET,
  PLACE_HOLDER,
  NOTE_SHIFT,
  BAR,
  BARLINE,
  CLEF,
  KEY_SIGNATURE,
  TIME_SIGNATURE,
  TEMPO,
  NAVIGATION,
  INSTRUMENT,
  TEMPO_TEXT,
  EXPRESSION_TEXT,
  EXPRESSION_DASH,
  REHEARSAL_MARK,
  FREE_TEXT,
  STAVE,
  STAVE_JOIN,
  PART,
  BEAM,
  REPEAT_START,
  REPEAT_END,
  LYRIC,
  GLISSANDO,
  SLUR,
  TIE,
  OCTAVE,
  VOLTA,
  HARMONY,
  LONG_TRILL,
  PEDAL,
  ARPEGGIO,
  ORNAMENT,
  ARTICULATION,
  BOWING,
  FINGERING,
  DYNAMIC,
  PAUSE,
  FERMATA,
  REPEAT_BAR,
  TREMOLO,
  WEDGE,
  SPACE,
  BREAK,
  UISTATE,
  PLAYBACK_STATE,
  META,
  TITLE,
  SUBTITLE,
  COMPOSER,
  LYRICIST,
  FILENAME,
  LAYOUT,
  OPTION,
  TRANSPOSE,
  BAR_BREAK,
  NO_TYPE,

  TEST_SCORE_EVENT,
  TEST_PART_EVENT,
  TEST_STAVE_EVENT,
  TEST_VOICE_EVENT;
}

private val decoratorTypes = setOf(
  EventType.SLUR, EventType.DYNAMIC, EventType.TIE, EventType.PEDAL,
  EventType.LONG_TRILL, EventType.OCTAVE
)

private val xGeogTypes = setOf(
  EventType.HARMONY, EventType.LYRIC, EventType.FERMATA
)

private val lineTypes = setOf(
  EventType.WEDGE, EventType.SLUR, EventType.PEDAL, EventType.OCTAVE, EventType.LONG_TRILL,
  EventType.VOLTA
)

internal fun EventType.isDecorator() = decoratorTypes.contains(this)
internal fun EventType.isXGeog() = xGeogTypes.contains(this)
internal fun EventType.isLine() = lineTypes.contains(this)

enum class
EventParam {
  TYPE,
  DURATION,
  REAL_DURATION,
  FULL_DURATION,
  GRACE_OFFSET_END,
  CONSOLIDATE,
  NOTES,
  SHARPS,
  PREVIOUS_SHARPS,
  MIDIVAL,
  CLEF,
  ACCIDENTAL,
  PITCH,
  POSITION,
  IS_UPSTEM,
  IS_UPSTEM_BEAM,
  IS_USER_STEM,
  IS_BEAMED,
  IS_SLASH,
  NUMERATOR,
  DENOMINATOR,
  HIDDEN,
  DIVISOR,
  MEMBERS,
  ORNAMENT,
  ARTICULATION,
  ARPEGGIO,
  BOWING,
  FINGERING,
  TEXT,
  START,
  END,
  PERCUSSION,
  PERCUSSION_DESC,
  STAVE_LINES,
  TEXT_SIZE,
  FONT,
  DASH_ADJUSTMENT,
  EXTRA_WIDTH,
  TONE,
  QUALITY,
  ROOT,
  NAME,
  LABEL,
  IS_UP,
  IS_SMALL,
  IS_START_TIE,
  IS_END_TIE,
  END_TIE,
  TIE_TO_LAST,
  IS_STRAIGHT,
  HARD_START,
  HARD_MID,
  HARD_END,
  BPM,
  NUMBER,
  NUM_BARS,
  INSTRUMENT,
  ACCIDENTAL_ABOVE,
  ACCIDENTAL_BELOW,
  TREMOLO_BEATS,
  AMOUNT,
  MARKER_POSITION,
  VOLUME,
  MUTE,
  SOLO,
  SELECTED_PART,
  FOR_STAVE,
  TRANSPOSITION,
  SOUNDFONT,
  BANK,
  PROGRAM,
  ABBREVIATION,
  GROUP,
  GRACE_TYPE,
  GRACE_MODE,
  ADD_OCTAVE,
  DOTTED_RHYTHM,
  NOTE_HEAD_TYPE,
  HOLD,
  SECTIONS,
  AFTER,
  SIMPLE,
  FORCE,
  OPTION_BAR_NUMBERING,
  OPTION_BARS_PER_LINE,
  OPTION_SHOW_MULTI_BARS,
  OPTION_SHOW_TRANSPOSE_CONCERT,
  OPTION_HIDE_EMPTY_STAVES,
  OPTION_SHOW_PART_NAME,
  OPTION_SHOW_PART_NAME_START_STAVE,
  OPTION_SHUFFLE_RHYTHM,
  OPTION_LOOP,
  OPTION_HARMONY,
  OPTION_HARMONY_INSTRUMENT,
  OPTION_LYRIC_FONT,
  OPTION_LYRIC_SIZE,
  OPTION_LYRIC_OFFSET,
  OPTION_LYRIC_OFFSET_BY_POSITION,
  OPTION_LYRIC_POSITIONS,
  OPTION_HARMONY_FONT,
  OPTION_HARMONY_SIZE,
  OPTION_HARMONY_OFFSET,
  LAYOUT_PAGE_WIDTH,
  LAYOUT_PAGE_HEIGHT,
  LAYOUT_TOP_MARGIN,
  LAYOUT_BOTTOM_MARGIN,
  LAYOUT_LEFT_MARGIN,
  LAYOUT_RIGHT_MARGIN,
  LAYOUT_STAVE_GAP,
  LAYOUT_SYSTEM_GAP,

  ;

  fun isModifiable(): Boolean {
    return this == IS_UPSTEM
  }
}

data class Modifiable<T>(val modified: Boolean, val value: T, val ignore: Boolean = false)

enum class DurationType {
  REST, CHORD, NOTE, EMPTY, TUPLET_MARKER, NONE
}

enum class NoteHeadType {
  NORMAL, CROSS, DIAMOND
}

enum class ClefType {
  TREBLE, BASS, ALTO, TENOR, SOPRANO, MEZZO, TREBLE_15VA, TREBLE_8VA, TREBLE_8VB, BASS_8VA, BASS_8VB, PERCUSSION
}

enum class TimeSignatureType {
  COMMON, CUT_COMMON, CUSTOM
}

enum class MetaType {
  TITLE, SUBTITLE, COMPOSER, LYRICIST;

  fun toEventType(): EventType {
    return EventType.valueOf(toString())
  }

  fun textSize() = when (this) {
    TITLE -> TITLE_TEXT_SIZE
    SUBTITLE -> SUBTITLE_TEXT_SIZE
    COMPOSER -> COMPOSER_TEXT_SIZE
    LYRICIST -> COMPOSER_TEXT_SIZE
  }

}

enum class OrnamentType {
  TRILL, TURN, MORDENT, LOWER_MORDENT
}


data class Ornament(
  val ornamentType: OrnamentType, val accidentalAbove: Accidental? = null,
  val accidentalBelow: Accidental? = null
) {
  fun toEvent(): Event {
    val params = paramMapOf(
      EventParam.TYPE to ornamentType,
      EventParam.ACCIDENTAL_ABOVE to accidentalAbove,
      EventParam.ACCIDENTAL_BELOW to accidentalBelow
    )
    return Event(EventType.ORNAMENT, params)
  }
}

fun Event.ornament(): Ornament {
  return Ornament(
    subType as OrnamentType, getParam(EventParam.ACCIDENTAL_ABOVE),
    getParam(EventParam.ACCIDENTAL_BELOW)
  )
}


enum class ArpeggioType {
  NORMAL, UP, DOWN
}

enum class ArticulationType {
  ACCENT, STACCATO, TENUTO, STACCATISSIMO, MARCATO
}

enum class BowingType {
  LH_PIZZICATO, SNAP_PIZZICATO, HARMONIC, UP_BOW, DOWN_BOW
}

enum class BarLineType {
  NORMAL, DOUBLE, FINAL, START, START_REPEAT, END_REPEAT
}

enum class DynamicType {
  MOLTO_FORTISSIMO, FORTISSIMO, FORTE,
  MEZZO_FORTE, MEZZO_PIANO, PIANO, PIANISSIMO, MOLTO_PIANISSIMO, SFORZANDISSMO,
  SFORZANDO, SFORZANDO_PIANO, FORTE_PIANO
}

enum class NavigationType {
  CODA, DA_CAPO, DAL_SEGNO, FINE, SEGNO
}

enum class PauseType {
  BREATH, CAESURA
}

enum class FermataType {
  NORMAL, SQUARE, TRIANGLE
}

enum class GlissandoType {
  LINE, WAVY
}


enum class PedalType {
  LINE, STAR
}

enum class StaveJoinType {
  BRACKET, GRAND
}

enum class BreakType {
  SYSTEM, PAGE
}

enum class WedgeType {
  CRESCENDO, DIMINUENDO
}

enum class LyricType {
  START, MID, END, ALL
}

enum class PageSize {
  A2, A3, A4, A5, A6, A7
}

enum class BarNumbering {
  EVERY_SYSTEM,
  EVERY_BAR,
  NONE
}

enum class ExportType {
  MXML, MIDI, MP3, WAV, JPG, PDF, SAVE, ZIP;
}

enum class ImportType {
  XML, MXL, SAVE, TEXT, SOUNDFONT
}

enum class GraceType {
  NONE, APPOGGIATURA, ACCIACCATURA
}

enum class GraceInputMode {
  NONE, SHIFT, ADD
}

enum class FileSource {
  SAVE, TEMPLATE, AUTOSAVE, EXTERNAL, SOUNDFONT, THUMBNAIL
}


enum class RepeatBarType {
  ONE,
  TWO_START,
  TWO_END
}


