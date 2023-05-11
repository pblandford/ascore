package com.philblandford.kscore.api

import com.philblandford.kscore.engine.types.*


data class PercussionDescr(
  val staveLine: Int, val midiId: Int,
  val up: Boolean, val name: String, val noteHead: NoteHeadType = NoteHeadType.NORMAL
)

data class InstrumentGroup(val name: String, val instruments: List<Instrument>)

data class Instrument(
  val name: String, val abbreviation: String, val group: String, val program: Int,
  val transposition: Int,
  val clefs: List<ClefType>, val soundFont: String, val bank: Int,
  val staveLines: Int = 5,
  val percussionDescrs: List<PercussionDescr> = listOf(),
  val label:String = name
) {
  val percussion = clefs.firstOrNull() == ClefType.PERCUSSION

  fun toEvent(): Event {
    var params = paramMapOf(
      EventParam.NAME to name, EventParam.ABBREVIATION to abbreviation, EventParam.GROUP to group, EventParam.PROGRAM to program,
      EventParam.TRANSPOSITION to transposition, EventParam.PERCUSSION to percussion, EventParam.CLEF to clefs,
      EventParam.SOUNDFONT to soundFont, EventParam.BANK to bank, EventParam.LABEL to label
    )
    if (percussion) {
      params = params.plus(EventParam.PERCUSSION_DESC to percussionDescrs).plus(EventParam.STAVE_LINES to staveLines)
    }
    return Event(EventType.INSTRUMENT, params)
  }
  companion object {
    fun default() = Instrument("Violin", "Vln", "Strings", 42,0, listOf(ClefType.TREBLE), "default", 0)

    fun fromEvent(event: Event):Instrument {
      val name = event.getParam<String>(EventParam.NAME) ?: ""
      val label = event.getParam<String>(EventParam.LABEL) ?: ""
      val abbreviation = event.getParam<String>(EventParam.ABBREVIATION) ?: ""
      val group = event.getParam<String>(EventParam.GROUP) ?: ""
      val program = event.getParam<Int>(EventParam.PROGRAM) ?: 0
      val transposition = event.getParam<Int>(EventParam.TRANSPOSITION) ?: 0
      val clefs = event.getParam<List<ClefType>>(EventParam.CLEF) ?: listOf(ClefType.TREBLE)
      val soundfont = event.getParam<String>(EventParam.SOUNDFONT) ?: "default"
      val bank = event.getParam<Int>(EventParam.BANK) ?: 0
      return Instrument(name, abbreviation, group, program, transposition, clefs, soundfont, bank, label = label)
    }
  }
}

fun instrument(params: ParamMap): Instrument? =
  instrument(
    Event(
      EventType.INSTRUMENT,
      params
    )
  )

fun instrument(event: Event): Instrument? {
  return event.getParam<Instrument>(EventParam.INSTRUMENT) ?:
   event.getParam<String>(EventParam.NAME)?.let { name ->
    event.getParam<String>(EventParam.ABBREVIATION)?.let { abb ->
      event.getParam<String>(EventParam.GROUP)?.let { group ->
        event.getParam<Int>(EventParam.PROGRAM)?.let { program ->
          event.getParam<Int>(EventParam.TRANSPOSITION)?.let { trans ->
            event.getParam<List<ClefType>>(EventParam.CLEF)?.let { clefs ->
              event.getParam<String>(EventParam.SOUNDFONT)?.let { soundFont ->
                event.getParam<Int>(EventParam.BANK)?.let { bank ->
                  val staveLines = event.getParam<Int>(EventParam.STAVE_LINES) ?: 0
                  val percussionDescrs =
                    event.getParam<List<PercussionDescr>>(EventParam.PERCUSSION_DESC) ?: listOf()
                  val label = event.getParam<String>(EventParam.LABEL) ?: name
                  Instrument(
                    name,
                    abb,
                    group,
                    program,
                    trans,
                    clefs,
                    soundFont,
                    bank,
                    staveLines,
                    percussionDescrs,
                    label
                  )
                }
              }
            }
          }
        }
      }
    }
  }
}

fun defaultInstrument(): Instrument {
  return Instrument(
    "Violin", "Vln", "Strings", 42, 0,
    listOf(ClefType.TREBLE), "default", 0
  )
}

fun defaultInstrumentGrand(): Instrument {
  return Instrument(
    "Piano", "Pno", "Piano", 42, 0,
    listOf(ClefType.TREBLE, ClefType.BASS), "default", 0
  )
}
