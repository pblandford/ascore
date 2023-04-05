package com.philblandford.ascore.external.export.mxml.`in`.converter

import com.philblandford.ascore.external.export.mxml.out.MxmlPartList
import com.philblandford.ascore.external.export.mxml.out.MxmlScorePart
import com.philblandford.kscore.api.Instrument
import com.philblandford.kscore.api.InstrumentGetter
import com.philblandford.kscore.api.PercussionDescr
import com.philblandford.kscore.engine.types.ClefType
import kotlin.math.max

internal data class PartDesc(
  val id: String,
  val name: String,
  val abbrevation: String,
  val instruments: Map<String, Instrument>
)

internal fun mxmlPartListToParts(mxmlPartList: MxmlPartList, instrumentGetter: InstrumentGetter): Map<String, PartDesc> {

  return mxmlPartList.scoreParts.map { mxmlScorePart ->
    mxmlScorePart.toPart(instrumentGetter)
      .let { instrMap ->
        mxmlScorePart.id to PartDesc(
          mxmlScorePart.id, mxmlScorePart.partName.name.trim(),
          mxmlScorePart.partAbbreviation?.name?.trim() ?: mxmlScorePart.partName.name.trim(),
          instrMap
        )
      }
  }.toMap()
}

private fun MxmlScorePart.isPercussion(): Boolean {
  return midiInstrument.any { it.midiUnpitched != null }
}

private fun MxmlScorePart.toPart(instrumentGetter: InstrumentGetter): Map<String, Instrument> {
  if (isPercussion()) {
    return toPartPercussion()?.let { mapOf("$id-I1" to it) } ?: mapOf()
  }

  return scoreInstrument.withIndex().map { iv ->
    val name = iv.value.instrumentName.name
    val program = midiInstrument.toList()[iv.index].midiProgram.num

    val storedInstrument = instrumentGetter.getInstrument(name)
    val group = storedInstrument?.group ?: "Strings"
    val clefs = storedInstrument?.clefs ?: listOf(ClefType.TREBLE)
    val abbr = storedInstrument?.abbreviation ?: name
    iv.value.id to Instrument(
      name, abbr, group, program, 0, clefs, "default", 0
    )
  }.toMap()
}

private fun MxmlScorePart.toPartPercussion(): Instrument? {
  val name = scoreInstrument.first().instrumentName
  val partName = partName.name
  val abbr = partAbbreviation?.name ?: partName
  val staveLines = max(midiInstrument.count(), 5)

  var descrs = midiInstrument.map { midiInstrument ->
    midiInstrument.id to PercussionDescr(
      0,
      midiInstrument.midiUnpitched?.num ?: midiInstrument.midiProgram.num,
      false,
      midiInstrument.id
    )
  }.toMap()
  descrs = scoreInstrument.mapNotNull { scoreInstrument ->
    descrs[scoreInstrument.id]?.let { desc ->
      scoreInstrument.id to desc.copy(name = scoreInstrument.instrumentName.name)
    }
  }.toMap()
  return Instrument(
    partName, abbr, "Percussion", 1, 0,
    listOf(ClefType.PERCUSSION), "default", 0, staveLines, descrs.values.toList()
  )
}