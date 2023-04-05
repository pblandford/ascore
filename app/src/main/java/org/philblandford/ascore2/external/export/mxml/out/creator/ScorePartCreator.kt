package com.philblandford.ascore.external.export.mxml.out.creator

import com.philblandford.ascore.external.export.mxml.out.*
import com.philblandford.kscore.api.Instrument
import com.philblandford.kscore.api.instrument
import com.philblandford.kscore.engine.core.score.Part
import com.philblandford.kscore.engine.types.EventType

internal fun createScorePart(part: Part, num: Int): MxmlScorePart? {
  val id = "P$num"

  val instruments = getInstruments(part)
  return instruments.toList().firstOrNull()?.let { main ->
    val scoreInstruments = instruments.withIndex().flatMap { iv ->
      createScoreInstrument(iv.value, id, iv.index+1)
    }
    val midiInstruments = instruments.withIndex().flatMap { iv ->
      createMidiInstrument(iv.value, id, num, iv.index+1)
    }
    return MxmlScorePart(
      id, MxmlPartName(part.label), MxmlPartAbbreviation(main.abbreviation),
      scoreInstruments, midiInstruments
    )
  }
}

private fun getInstruments(part: Part): Iterable<Instrument> {
  val partInstruments = part.getEvents(EventType.INSTRUMENT)?.values ?: listOf()
  val staveInstruments = part.staves.flatMap { stave ->
    stave.getEvents(EventType.INSTRUMENT)?.values ?: listOf()
  }
  return partInstruments.plus(staveInstruments).mapNotNull { instrument(it) }
}

private fun createScoreInstrument(
  instrument: Instrument,
  id: String, instrumentNum:Int
): Iterable<MxmlScoreInstrument> {
  return if (instrument.percussion) {
    createScoreInstrumentsPercussion(instrument, id)
  } else {
    listOf(
      MxmlScoreInstrument(
        "$id-I$instrumentNum",
        MxmlInstrumentName(instrument.name)
      )
    )
  }
}

private fun createMidiInstrument(
  instrument: Instrument,
  id: String,
  partNum: Int,
  instrumentNum:Int
): Iterable<MxmlMidiInstrument> {
  return if (instrument.percussion) {
    createMidiInstrumentsPercussion(instrument, id)
  } else {
    listOf(
      MxmlMidiInstrument(
        "$id-I$instrumentNum",
        MxmlMidiChannel(partNum),
        MxmlMidiProgram(instrument.program)
      )
    )
  }
}

private fun createScoreInstrumentsPercussion(
  instrument: Instrument,
  id: String
): Iterable<MxmlScoreInstrument> {
  return instrument.percussionDescrs.map { descr ->
    MxmlScoreInstrument("$id-I${descr.midiId}", MxmlInstrumentName(descr.name))
  }
}

private fun createMidiInstrumentsPercussion(
  instrument: Instrument,
  id: String
): Iterable<MxmlMidiInstrument> {
  return instrument.percussionDescrs.map { descr ->
    MxmlMidiInstrument(
      "$id-I${descr.midiId}",
      MxmlMidiChannel(10), MxmlMidiProgram(descr.midiId),
      MxmlMidiUnpitched(descr.midiId)
    )
  }
}