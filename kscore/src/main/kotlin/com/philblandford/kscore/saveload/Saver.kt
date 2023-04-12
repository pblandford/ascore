package com.philblandford.kscore.saveload

import com.philblandford.kscore.api.PercussionDescr
import com.philblandford.kscore.engine.core.area.Coord
import com.philblandford.kscore.engine.core.score.*
import com.philblandford.kscore.engine.duration.Duration
import com.philblandford.kscore.engine.map.EventHash
import com.philblandford.kscore.engine.map.EventMapKey
import com.philblandford.kscore.engine.newadder.subadders.ChordDecoration
import com.philblandford.kscore.engine.types.*
import java.util.*

const val VERSION_MARKER = 0x00eebbcc
internal const val CURRENT_VERSION = 1


enum class SaveType {
  CLEF, DURATIONTYPE, EVENTTYPE, NOTELETTER, ACCIDENTAL, ARTICULATION, BOWING, ORNAMENT, ORNAMENT_TYPE, ARPEGGIO,
  DYNAMIC, TSTYPE, STAVEJOINTYPE,
  BREAK, NOTEHEADTYPE, BARLINETYPE, FERMATATYPE, PAUSETYPE, PEDALTYPE, WEDGETYPE, NAVIGATIONTYPE,
  LYRICTYPE, GLISSANDOTYPE, BARNUMBERING,
  PARAM, EVENTADDRESS, DURATION, COORD, META, MODIFIABLE, CHORDDECORATION, PERCUSSIONDESCR, NOTE, NULL, PITCH, TUPLET, VOICEMAP, BAR, STAVE, PART,
  ITERABLE, BOOL, INT, STRING, PAIR
}


class Saver {

  private var saveFileVersion = CURRENT_VERSION

  fun createSaveScore(score: Score, withVersion: Boolean = true): ByteArray {
    val versionBytes = if (withVersion) {
      saveVersion()
    } else {
      saveFileVersion = 0
      byteArrayOf()
    }
    val partBytes = saveIterable(score.parts)
    val eventBytes = saveEventHash(score.getAllLevelEvents())
    return versionBytes.plus(partBytes).plus(eventBytes)
  }


  private fun saveVersion(): ByteArray {
    return saveInt(VERSION_MARKER).plus(saveInt(CURRENT_VERSION))
  }


  private fun savePart(part: Part): ByteArray {
    val staveBytes = saveIterable(part.staves)
    val eventBytes = saveEventHash(part.getAllLevelEvents())
    return staveBytes.plus(eventBytes)
  }


  fun saveStave(stave: Stave): ByteArray {
    val barBytes = saveIterable(stave.bars)
    val eventBytes = saveEventHash(stave.getAllLevelEvents())
    return barBytes.plus(eventBytes)
  }

  fun saveBar(bar: Bar): ByteArray {
    val vmBytes = saveIterable(bar.voiceMaps)
    val eventBytes = saveEventHash(bar.getAllLevelEvents())
    return vmBytes.plus(eventBytes)
  }

  fun saveVoiceMap(voiceMap: VoiceMap): ByteArray {
    val events = saveEventHash(voiceMap.eventMap.getAllEvents())
    val tuplets = saveIterable(voiceMap.tuplets)
    return events.plus(tuplets)
  }

  private fun saveTuplet(tuplet: Tuplet): ByteArray {
    val offset = saveDuration(tuplet.offset)
    val numerator = saveByte(tuplet.timeSignature.numerator)
    val denominator = saveByte(tuplet.timeSignature.denominator)
    val divisor = saveByte(tuplet.childDivisor)
    val duration = saveDuration(tuplet.realDuration)
    val hidden = saveBool(tuplet.hidden)
    val events = saveEventHash(tuplet.eventMap.getAllEvents())
    return offset.plus(numerator).plus(denominator).plus(divisor).plus(duration).plus(hidden)
      .plus(events)
  }


  fun saveEventHash(eventHash: EventHash): ByteArray {
    val size = saveInt(eventHash.size)
    val entries = eventHash.toList().fold(byteArrayOf()) { arr, (emk, event) ->
      arr.plus(saveEventMapKey(emk)).plus(saveParams(event.params))
    }
    return size.plus(entries)
  }

  fun saveEventMapKey(emk: EventMapKey): ByteArray {
    val bytes = listOf(
      saveAny(emk.eventType),
      saveEventAddress(emk.eventAddress)
    )
    return bytes.reduce { one, two -> one.plus(two) }
  }

  fun saveParams(paramMap: ParamMap): ByteArray {
    val bytes = mutableListOf<Byte>()
    bytes.add(paramMap.size.toByte())
    paramMap.toList().forEach { (k, v) ->
      bytes.addAll(saveAny(k).toList())
      bytes.addAll(saveAny(v).toList())
    }
    return bytes.toByteArray()
  }

  fun saveEventAddress(eventAddress: EventAddress): ByteArray {
    val bytes = listOf(
      saveShort(eventAddress.barNum),
      saveShort(eventAddress.offset.numerator),
      saveShort(eventAddress.offset.denominator),
      saveAny(eventAddress.graceOffset),
      saveShort(eventAddress.staveId.main),
      saveShort(eventAddress.staveId.sub),
      saveByte(eventAddress.voice),
      saveByte(eventAddress.id)
    )
    return bytes.reduce { one, two -> one.plus(two) }
  }

  fun saveShort(short: Int): ByteArray {
    return byteArrayOf(short.and(0xff00).shr(8).toByte(), short.and(0xff).toByte())
  }

  fun saveInt(int: Int): ByteArray {
    return byteArrayOf(
      int.and(0xff000000.toInt()).shr(24).toByte(),
      int.and(0xff0000).shr(16).toByte(),
      int.and(0xff00).shr(8).toByte(),
      int.and(0xff).toByte()
    )
  }

  fun peekInt(byteArray: LinkedList<Byte>): Int? {
    val b1 = byteArray[0].toInt().shl(24) and 0xff000000.toInt()
    val b2 = byteArray[1].toInt().shl(16) and 0xff0000
    val b3 = byteArray[2].toInt().shl(8) and 0xff00
    val b4 = byteArray[3].toInt() and 0xff
    return (b1 or b2 or b3 or b4)
  }

  fun saveByte(byte: Int): ByteArray {
    return byteArrayOf(byte.toByte())
  }

  fun saveString(string: String): ByteArray {
    val bytes = string.toByteArray()
    return byteArrayOf(bytes.size.toByte()).plus(bytes)
  }


  fun saveDuration(duration: Duration): ByteArray {
    return saveShort(duration.numerator).plus(saveShort(duration.denominator))
  }

  fun savePitch(pitch: Pitch): ByteArray {
    val bytes = listOf(
      saveAny(pitch.noteLetter),
      saveAny(pitch.accidental),
      saveInt(pitch.octave),
      saveBool(pitch.showAccidental)
    )
    return bytes.reduce { one, two -> one.plus(two) }
  }

  fun saveCoord(coord: Coord): ByteArray {
    return saveInt(coord.x).plus(saveInt(coord.y))
  }

  private fun saveMetaSection(metaSection: MetaSection): ByteArray {
    val bytes = listOf(
      saveString(metaSection.text),
      saveInt(metaSection.size),
      saveString(metaSection.font ?: ""),
      if (saveFileVersion > 0) {
        saveCoord(metaSection.offset ?: Coord())
      } else {
        byteArrayOf()
      }
    )
    return bytes.reduce { one, two -> one.plus(two) }
  }

  fun saveMeta(meta: Meta): ByteArray {
    val bytes = listOf(
      saveString(meta.fileName),
      saveMetaSection(meta.getSection(MetaType.TITLE)),
      saveMetaSection(meta.getSection(MetaType.SUBTITLE)),
      saveMetaSection(meta.getSection(MetaType.COMPOSER)),
      saveMetaSection(meta.getSection(MetaType.LYRICIST))
    )
    return bytes.reduce { one, two -> one.plus(two) }
  }

  fun <T> saveModifiable(modifiable: Modifiable<T>): ByteArray {
    val bytes = listOf(
      saveBool(modifiable.modified),
      saveAny(modifiable.value)
    )
    return bytes.reduce { one, two -> one.plus(two) }
  }

  fun saveOrnament(ornament: Ornament): ByteArray {
    val bytes = listOf(
      saveAny(ornament.ornamentType),
      saveAny(ornament.accidentalAbove),
      saveAny(ornament.accidentalBelow)
    )
    return bytes.reduce { one, two -> one.plus(two) }
  }

  fun saveChordDecoration(chordDecoration: ChordDecoration<*>): ByteArray {
    val bytes = listOf(
      saveBool(chordDecoration.up),
      saveIterable(chordDecoration.items.filterNotNull())
    )
    return bytes.reduce { one, two -> one.plus(two) }
  }

  fun savePercussionDesc(percussionDescr: PercussionDescr): ByteArray {
    val bytes = listOf(
      saveByte(percussionDescr.staveLine),
      saveByte(percussionDescr.midiId),
      saveBool(percussionDescr.up),
      saveString(percussionDescr.name),
      saveString(percussionDescr.noteHead.toString())
    )
    return bytes.reduce { one, two -> one.plus(two) }
  }

  fun saveBool(value: Boolean): ByteArray {
    return saveByte(if (value) 1 else 0)
  }

  fun saveIterable(value: Iterable<Any>): ByteArray {
    val list = value.toList()
    val size = saveInt(list.size)
    val items = list.fold(byteArrayOf()) { bytes, item ->
      bytes.plus(saveAny(item))
    }
    return size.plus(items)
  }

  fun savePair(value:Pair<Any, Any>): ByteArray {
    return saveAny(value.first) + saveAny(value.second)
  }

  fun saveAny(value: Any?): ByteArray {
    return getSaveTypeFunc(value)?.let { (st, func) ->
      byteArrayOf(st.ordinal.toByte()).plus(func(value))
    } ?: byteArrayOf()
  }

  private fun getSaveTypeFunc(value: Any?): Pair<SaveType, (Any?) -> ByteArray>? {
    if (value == null) {
      return SaveType.NULL to { a -> byteArrayOf() }
    }
    return when (value) {
      is ClefType -> SaveType.CLEF to { a -> saveString(a.toString()) }
      is DurationType -> SaveType.DURATIONTYPE to { a -> saveString(a.toString()) }
      is EventType -> SaveType.EVENTTYPE to { a -> saveString(a.toString()) }
      is EventParam -> SaveType.PARAM to { a -> saveString(a.toString()) }
      is NoteLetter -> SaveType.NOTELETTER to { a -> saveString(a.toString()) }
      is Accidental -> SaveType.ACCIDENTAL to { a -> saveString(a.toString()) }
      is ArpeggioType -> SaveType.ARPEGGIO to { a -> saveString(a.toString()) }
      is ArticulationType -> SaveType.ARTICULATION to { a -> saveString(a.toString()) }
      is BowingType -> SaveType.BOWING to { a -> saveString(a.toString()) }
      is OrnamentType -> SaveType.ORNAMENT_TYPE to { a -> saveString(a.toString()) }
      is Ornament -> SaveType.ORNAMENT to { a -> saveOrnament(a as Ornament) }
      is DynamicType -> SaveType.DYNAMIC to { a -> saveString(a.toString()) }
      is TimeSignatureType -> SaveType.TSTYPE to { a -> saveString(a.toString()) }
      is StaveJoinType -> SaveType.STAVEJOINTYPE to { a -> saveString(a.toString()) }
      is BreakType -> SaveType.BREAK to { a -> saveString(a.toString()) }
      is NoteHeadType -> SaveType.NOTEHEADTYPE to { a -> saveString(a.toString()) }
      is BarLineType -> SaveType.BARLINETYPE to { a -> saveString(a.toString()) }
      is FermataType -> SaveType.FERMATATYPE to { a -> saveString(a.toString()) }
      is PauseType -> SaveType.PAUSETYPE to { a -> saveString(a.toString()) }
      is PedalType -> SaveType.PEDALTYPE to { a -> saveString(a.toString()) }
      is WedgeType -> SaveType.WEDGETYPE to { a -> saveString(a.toString()) }
      is NavigationType -> SaveType.NAVIGATIONTYPE to { a -> saveString(a.toString()) }
      is LyricType -> SaveType.LYRICTYPE to { a -> saveString(a.toString()) }
      is GlissandoType -> SaveType.GLISSANDOTYPE to { a -> saveString(a.toString()) }
      is BarNumbering -> SaveType.BARNUMBERING to { a -> saveString(a.toString()) }
      is EventAddress -> SaveType.EVENTADDRESS to { a -> saveEventAddress(a as EventAddress) }
      is Duration -> SaveType.DURATION to { a -> saveDuration(a as Duration) }
      is Coord -> SaveType.COORD to { a -> saveCoord(a as Coord) }
      is Meta -> SaveType.META to { a -> saveMeta(a as Meta) }
      is Modifiable<*> -> SaveType.MODIFIABLE to { a -> saveModifiable(a as Modifiable<*>) }
      is ChordDecoration<*> -> SaveType.CHORDDECORATION to { a -> saveChordDecoration(a as ChordDecoration<*>) }
      is PercussionDescr -> SaveType.PERCUSSIONDESCR to { a -> savePercussionDesc(a as PercussionDescr) }
      is Pitch -> SaveType.PITCH to { a -> savePitch(a as Pitch) }
      is Tuplet -> SaveType.TUPLET to { a -> saveTuplet(a as Tuplet) }
      is VoiceMap -> SaveType.VOICEMAP to { a -> saveVoiceMap(a as VoiceMap) }
      is Bar -> SaveType.BAR to { a -> saveBar(a as Bar) }
      is Stave -> SaveType.STAVE to { a -> saveStave(a as Stave) }
      is Part -> SaveType.PART to { a -> savePart(a as Part) }
      is Iterable<*> -> SaveType.ITERABLE to { a -> saveIterable(a as Iterable<Any>) }
      is Pair<*,*> -> SaveType.PAIR to { a -> savePair(a as Pair<Any, Any>)}
      is Int -> SaveType.INT to { a -> saveInt(a as Int) }
      is Boolean -> SaveType.BOOL to { a -> saveBool(a as Boolean) }
      is String -> SaveType.STRING to { a -> saveString(a as String) }
      is Event -> SaveType.NOTE to { a -> saveParams((a as Event).params) }
      else -> throw Exception("Finish me! ${value?.let { it::class }}")
    }
  }

}
