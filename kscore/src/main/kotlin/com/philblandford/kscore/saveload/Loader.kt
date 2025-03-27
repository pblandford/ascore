package com.philblandford.kscore.saveload

import com.philblandford.kscore.api.PercussionDescr
import com.philblandford.kscore.engine.beam.BeamDirectory
import com.philblandford.kscore.engine.core.area.Coord
import com.philblandford.kscore.engine.core.score.*
import com.philblandford.kscore.engine.duration.Duration
import com.philblandford.kscore.engine.duration.Offset
import com.philblandford.kscore.engine.map.EventHash
import com.philblandford.kscore.engine.map.EventMapKey
import com.philblandford.kscore.engine.map.eventHashOf
import com.philblandford.kscore.engine.map.eventMapOf
import com.philblandford.kscore.engine.eventadder.subadders.ChordDecoration
import com.philblandford.kscore.engine.time.TimeSignature
import com.philblandford.kscore.engine.types.*
import com.philblandford.kscore.log.ksLoge
import java.util.*


class Loader {

  private var saveFileVersion = CURRENT_VERSION


  fun createScoreFromBytes(bytes: ByteArray): Score? {
    val mutable = LinkedList(bytes.toList())
    loadVersion(mutable)
    return loadIterable<Part>(mutable)?.let { parts ->
      loadEventHash(mutable)?.let { events ->
        var score = Score(parts.toList(), eventMapOf(events), BeamDirectory(mapOf(), mapOf()))
        val beamDirectory = BeamDirectory.create(score)
        score = score.copy(beamDirectory = beamDirectory)
        score.readdMeta().setLyricOffset().ensureDMTimeSignaturesCorrect()
      }
    }
  }

  private fun loadVersion(bytes: LinkedList<Byte>) {
    val hasVersion = peekInt(bytes) == VERSION_MARKER
    if (hasVersion) {
      repeat(4) { bytes.removeAt(0) }
      loadInt(bytes)?.let { version ->
        saveFileVersion = version
      }
    } else {
      saveFileVersion = 0
    }
  }

  private fun loadPart(bytes: LinkedList<Byte>): Part? {
    return loadIterable<Stave>(bytes)?.let { staves ->
      loadEventHash(bytes)?.let {  events ->
        Part(
          staves.toList().toList(),
          eventMapOf(events)
        )
      }
    }
  }


  fun loadStave(bytes: LinkedList<Byte>): Stave? {
    return loadIterable<Bar>(bytes)?.let { bars ->
      loadEventHash(bytes)?.let { events ->
        Stave(
          bars.toList().toList(),
          eventMapOf(events)
        )
      }
    }
  }

  fun loadBar(bytes: LinkedList<Byte>): Bar? {
    return loadIterable<VoiceMap>(bytes)?.let { vms ->
      loadEventHash(bytes)?.let { events ->
        Bar(
          TimeSignature(4, 4),
          vms.toList().toList(),
          eventMapOf(events)
        )
      }
    }
  }

  fun loadVoiceMap(bytes: LinkedList<Byte>): VoiceMap? {
    return try {
      loadEventHash(bytes)?.let { hash ->
        loadIterable<Tuplet>(bytes)?.let { tuplets ->
          voiceMap(eventMapOf(hash), tuplets.toList())
        }
      }
    } catch (e:Exception) {
      null
    }
  }

  private fun loadTuplet(bytes: LinkedList<Byte>): Tuplet? {
    return loadDuration(bytes)?.let { offset ->
      loadByte(bytes)?.let { num ->
        loadByte(bytes)?.let { den ->
          loadByte(bytes)?.let { div ->
            loadDuration(bytes)?.let { duration ->
              loadBool(bytes)?.let { hidden ->
                loadEventHash(bytes)?.let { events ->
                  tuplet(
                    offset, num.toInt(), den.toInt(), div.toInt(), duration, hidden,
                    eventMapOf(events)
                  )
                }
              }
            }
          }
        }
      }
    }
  }

  fun loadEventHash(bytes: LinkedList<Byte>): EventHash? {
    return loadInt(bytes)?.let { size ->
      (1..size).fold(eventHashOf()) { hash, _ ->
        loadEventMapKey(bytes)?.let { emk ->
          loadParams(bytes)?.let { params ->
            hash.plus(emk to Event(emk.eventType, params))
          }
        } ?: hash
      }
    }
  }

  fun loadEventMapKey(bytes: LinkedList<Byte>): EventMapKey? {
    return loadAny(bytes)?.let { type ->
      loadEventAddress(bytes)?.let { addr ->
        EventMapKey(type as EventType, addr)
      }
    }
  }

  fun loadParams(byteArray: LinkedList<Byte>): ParamMap? {
    try {
      return loadByte(byteArray)?.let { cnt ->
        (1..cnt).fold(paramMapOf().toMutableMap()) { map, _ ->
          loadAny(byteArray)?.let { param ->
            val value = loadAny(byteArray)
            map[param as EventParam] = value
            map
          } ?: map
        }
      }?.toMap()
    } catch(e:Exception) {
      ksLoge("loadParams", e)
    }
    return null
  }

  fun loadEventAddress(byteArray: LinkedList<Byte>): EventAddress? {
    return loadShort(byteArray)?.let { bar ->
      loadShort(byteArray)?.let { num ->
        loadShort(byteArray)?.let { den ->
          val graceOffset = loadAny(byteArray)
          loadShort(byteArray)?.let { main ->
            loadShort(byteArray)?.let { sub ->
              loadByte(byteArray)?.let { voice ->
                loadByte(byteArray)?.let { id ->
                  EventAddress(
                    bar.toInt(),
                    Duration(num.toInt(), den.toInt()),
                    graceOffset as Offset?,
                    StaveId(main.toInt(), sub.toInt()),
                    voice.toInt(),
                    id.toInt()
                  )
                }
              }
            }
          }
        }
      }
    }
  }


  fun loadShort(byteArray: LinkedList<Byte>): Short? {
    val b1 = byteArray[0].toInt().shl(8)
    val b2 = byteArray[1].toInt()
    val short = (b1 or b2).toShort()
    repeat(2) { byteArray.removeAt(0) }
    return short
  }

  fun peekInt(byteArray: LinkedList<Byte>): Int? {
    return try {
      val b1 = byteArray[0].toInt().shl(24) and 0xff000000.toInt()
      val b2 = byteArray[1].toInt().shl(16) and 0xff0000
      val b3 = byteArray[2].toInt().shl(8) and 0xff00
      val b4 = byteArray[3].toInt() and 0xff
      (b1 or b2 or b3 or b4)
    } catch (_:Exception) {
      null
    }
  }

  fun loadInt(byteArray: LinkedList<Byte>): Int? {
    val int = peekInt(byteArray)
    repeat(4) { byteArray.removeAt(0) }
    return int
  }

  fun loadByte(byteArray: LinkedList<Byte>): Byte? {
    val byte = byteArray[0]
    byteArray.removeAt(0)
    return byte
  }

  fun loadString(byteArray: LinkedList<Byte>): String? {
    try {
      val length = byteArray[0].toUByte().toInt()
      byteArray.removeFirst()
      val bytes = ByteArray(length)
      repeat(length) {
        bytes[it] = byteArray.removeAt(0)
      }
      return String(bytes)
    } catch(e:Exception) {
      ksLoge("loadString anomaly", e)
    }
    return null
  }

  fun loadPitch(bytes: LinkedList<Byte>): Pitch? {
    return loadAny(bytes)?.let { noteletter ->
      loadAny(bytes)?.let { accidental ->
        loadInt(bytes)?.let { octave ->
          loadBool(bytes)?.let { show ->
            Pitch(noteletter as NoteLetter, accidental as Accidental, octave, show)
          }
        }
      }
    }
  }

  fun loadDuration(byteArray: LinkedList<Byte>): Duration? {
    return loadShort(byteArray)?.let { num ->
      loadShort(byteArray)?.let { den ->
        Duration(num.toInt(), den.toInt())
      }
    }
  }

  fun loadCoord(byteArray: LinkedList<Byte>): Coord? {
    return loadInt(byteArray)?.let { x ->
      loadInt(byteArray)?.let { y ->
        Coord(x, y)
      }
    }
  }

  private fun loadMetaSection(bytes: LinkedList<Byte>): MetaSection? {
    return loadString(bytes)?.let { text ->
      loadInt(bytes)?.let { size ->
        loadString(bytes)?.let { font ->
          val coord = if (saveFileVersion > 0) {
            loadCoord(bytes) ?: Coord()
          } else Coord()
          MetaSection(text, size, font, coord)
        }
      }
    }
  }


  fun loadMeta(bytes: LinkedList<Byte>): Meta? {
    return loadString(bytes)?.let { filename ->
      loadMetaSection(bytes)?.let { title ->
        loadMetaSection(bytes)?.let { subtitle ->
          loadMetaSection(bytes)?.let { composer ->
            loadMetaSection(bytes)?.let { lyricist ->
              Meta(
                mapOf(
                  MetaType.TITLE to title,
                  MetaType.SUBTITLE to subtitle,
                  MetaType.COMPOSER to composer,
                  MetaType.LYRICIST to lyricist
                ), filename
              )
            }
          }
        }
      }
    }
  }

  fun loadModifiable(bytes: LinkedList<Byte>): Modifiable<Any>? {
    return loadBool(bytes)?.let { modified ->
      loadAny(bytes)?.let { value ->
        Modifiable(modified, value)
      }
    }
  }


  fun loadOrnament(bytes: LinkedList<Byte>): Ornament? {
    return loadAny(bytes)?.let { type ->
      val above = loadAny(bytes)
      val below = loadAny(bytes)
      Ornament(type as OrnamentType, above as Accidental?, below as Accidental?)
    }
  }

  fun loadChordDecoration(bytes: LinkedList<Byte>): ChordDecoration<*>? {
    return loadBool(bytes)?.let { up ->
      loadIterable<Any>(bytes)?.let { items ->
        ChordDecoration(up, items)
      }
    }
  }

  fun loadPercussionDesc(bytes: LinkedList<Byte>): PercussionDescr? {
    return loadByte(bytes)?.let { staveLine ->
      loadByte(bytes)?.let { midiId ->
        loadBool(bytes)?.let { up ->
          loadString(bytes)?.let { name ->
            loadString(bytes)?.let { notehead ->
              PercussionDescr(
                staveLine.toInt(),
                midiId.toInt(),
                up,
                name,
                NoteHeadType.valueOf(notehead)
              )
            }
          }
        }
      }
    }
  }

  fun loadBool(byteArray: LinkedList<Byte>): Boolean? {
    return loadByte(byteArray)?.let { bool ->
      (bool == 1.toByte())
    }
  }
  
  fun <Any> loadIterable(byteArray: LinkedList<Byte>): Iterable<Any>? {
    return loadInt(byteArray)?.let { count ->
      val list = mutableListOf<Any>()
      (1..count).forEach { num ->
        loadAny(byteArray)?.let { item ->
          //    ksLogv("loading $item number $num")
          list.add(item as Any)
        }
      }
      list
    }
  }
  fun loadPair(byteArray: LinkedList<Byte>):Pair<Any,Any>? {
    return loadAny(byteArray)?.let { first ->
    loadAny(byteArray)?.let { second ->
      first to second
    }}
  }

  fun loadAny(byteArray: LinkedList<Byte>): Any? {
      return loadByte(byteArray)?.let { idx ->
        saveTypeValues.getOrNull(idx.toInt())?.let { stv ->
          loadFuncs[stv]?.let { func ->
            func(byteArray)
          }
        }
      }
  }

  private val saveTypeValues = SaveType.values()

  private val loadFuncs = mapOf<SaveType, (LinkedList<Byte>) -> Any?>(
    SaveType.CLEF to { b -> loadEnum(b) { ClefType.valueOf(it) } },
    SaveType.DURATIONTYPE to { b -> loadEnum(b) { DurationType.valueOf(it) } },
    SaveType.EVENTTYPE to { b -> loadEnum(b) { EventType.valueOf(it) } },
    SaveType.PARAM to { b -> loadEnum(b) { EventParam.valueOf(it) } },
    SaveType.NOTELETTER to { b -> loadEnum(b) { NoteLetter.valueOf(it) } },
    SaveType.ACCIDENTAL to { b -> loadEnum(b) { Accidental.valueOf(it) } },
    SaveType.ARTICULATION to { b -> loadEnum(b) { ArticulationType.valueOf(it) } },
    SaveType.BOWING to { b -> loadEnum(b) { BowingType.valueOf(it) } },
    SaveType.ARPEGGIO to { b -> loadEnum(b) { ArpeggioType.valueOf(it) } },
    SaveType.ORNAMENT to { b -> loadOrnament(b) },
    SaveType.ORNAMENT_TYPE to { b -> loadEnum(b) { OrnamentType.valueOf(it) } },
    SaveType.DYNAMIC to { b -> loadEnum(b) { DynamicType.valueOf(it) } },
    SaveType.TSTYPE to { b -> loadEnum(b) { TimeSignatureType.valueOf(it) } },
    SaveType.STAVEJOINTYPE to { b -> loadEnum(b) { StaveJoinType.valueOf(it) } },
    SaveType.BREAK to { b -> loadEnum(b) { BreakType.valueOf(it) } },
    SaveType.NOTEHEADTYPE to { b -> loadEnum(b) { NoteHeadType.valueOf(it) } },
    SaveType.BARLINETYPE to { b -> loadEnum(b) { BarLineType.valueOf(it) } },
    SaveType.FERMATATYPE to { b -> loadEnum(b) { FermataType.valueOf(it) } },
    SaveType.PAUSETYPE to { b -> loadEnum(b) { PauseType.valueOf(it) } },
    SaveType.PEDALTYPE to { b -> loadEnum(b) { PedalType.valueOf(it) } },
    SaveType.WEDGETYPE to { b -> loadEnum(b) { WedgeType.valueOf(it) } },
    SaveType.LONGTRILLTYPE to { b -> loadEnum(b) { LongTrillType.valueOf(it) } },
    SaveType.NAVIGATIONTYPE to { b -> loadEnum(b) { NavigationType.valueOf(it) } },
    SaveType.LYRICTYPE to { b -> loadEnum(b) { LyricType.valueOf(it) } },
    SaveType.GLISSANDOTYPE to { b -> loadEnum(b) { GlissandoType.valueOf(it) } },
    SaveType.BARNUMBERING to { b -> loadEnum(b) { BarNumbering.valueOf(it) } },
    SaveType.EVENTADDRESS to { b -> loadEventAddress(b) },
    SaveType.DURATION to { b -> loadDuration(b) },
    SaveType.COORD to { b -> loadCoord(b) },
    SaveType.META to { b -> loadMeta(b) },
    SaveType.MODIFIABLE to { b -> loadModifiable(b) },
    SaveType.CHORDDECORATION to { b -> loadChordDecoration(b) },
    SaveType.PERCUSSIONDESCR to { b -> loadPercussionDesc(b) },
    SaveType.NOTE to { b -> loadParams(b)?.let { Event(EventType.NOTE, it) } },
    SaveType.PAIR to { b -> loadPair(b) },
    SaveType.PITCH to { b -> loadPitch(b) },
    SaveType.TUPLET to { b -> loadTuplet(b) },
    SaveType.VOICEMAP to { b -> loadVoiceMap(b) },
    SaveType.BAR to { b -> loadBar(b) },
    SaveType.STAVE to { b -> loadStave(b) },
    SaveType.PART to { b -> loadPart(b) },
    SaveType.ITERABLE to { b -> loadIterable<Event>(b) },
    SaveType.BOOL to { b -> loadBool(b) },
    SaveType.INT to { b -> loadInt(b) },
    SaveType.STRING to { b -> loadString(b) },
    SaveType.NULL to { b -> null },
    SaveType.BEAMTYPE to { b -> loadEnum(b) { BeamType.valueOf(it) } },

    )

  private inline fun <reified E : Enum<E>> loadEnum(byteArray: LinkedList<Byte>, conv: (String) -> E): E? {
    return loadString(byteArray)?.let { str ->
      try {
        conv(str)
      } catch (e:Exception) {
        ksLoge("Unknown type $str")
        null
      }
    }
  }

}
