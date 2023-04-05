package com.philblandford.ascore.external.export.mxml.`in`.reader

import com.philblandford.ascore.external.export.mxml.out.*
import org.w3c.dom.Element


internal fun Element.parseAttributes(): MxmlAttributes {
  val divisions = getTextElem("divisions")?.toInt()?.let { MxmlDivisions(it) }
  val key = getChild("key")?.parseKey()
  val time = getChild("time")?.parseTime()
  val staves = getChild("staves")?.parseStaves()
  val clef = getChildren("clef").mapNotNull { it.parseClef() }
  val staffDetails = getChild("staff-details")?.parseStaffDetails()
  val transpose = getChild("transpose")?.parseTranspose()
  val measureStyle = getChildren("measure-style").mapNotNull {  it.parseMeasureStyle() }
  return MxmlAttributes(divisions, key, time, staves, clef, staffDetails, transpose, measureStyle)
}

private fun Element.parseKey(): MxmlKey? {
  return getElementsByTagName("fifths").item(0)?.textContent?.toInt()?.let { fifths ->
    MxmlKey(MxmlFifths(fifths))
  }
}

private fun Element.parseTime(): MxmlTime? {
  return getTextElem("beats")?.toInt()?.let { beat ->
    return getTextElem("beat-type")?.toInt()?.let { beatType ->
      MxmlTime(MxmlBeats(beat), MxmlBeatType(beatType))
    }
  }
}

private fun Element.parseStaves(): MxmlStaves? {
  return textContent?.let { MxmlStaves(it.toInt()) }
}


private fun Element.parseClef(): MxmlClef? {
  return getTextElem("sign")?.let { sign ->
    val line = getTextElem("line")?.let { MxmlLine(it.toInt()) }
    val octave = getTextElem("clef-octave-change")?.let { MxmlClefOctaveChange(it.toInt()) }
    val number = getAttributeOrNull("number")?.toInt()
    return MxmlClef(MxmlSign(sign), line, octave, number)
  }
}

private fun Element.parseStaffDetails(): MxmlStaffDetails? {
  val staffLines = getTextElem("staff-lines")?.let { MxmlStaffLines(it.toInt()) }
  return MxmlStaffDetails(staffLines)
}

private fun Element.parseTranspose(): MxmlTranspose? {
  val diatonic = getTextElem("diatonic")?.let { MxmlDiatonic(it.toInt()) }
  return getTextElem("chromatic")?.let { chromatic ->
    MxmlTranspose(diatonic, MxmlChromatic(chromatic.toInt()))
  }
}

private fun Element.parseMeasureStyle(): MxmlMeasureStyle? {
  val staff = getAttribute("staff")?.toIntOrNull() ?: 1
  return getChild("measure-repeat")?.let { mRepeat ->
    mRepeat.getAttribute("type")?.let { type ->
      val slashes = mRepeat.getAttribute("slashes")?.toInt() ?: 1
      MxmlMeasureStyle(staff, MxmlMeasureRepeat(type, slashes))
    }
  }
}