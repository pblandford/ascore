package com.philblandford.ascore.external.export.mxml.`in`.reader

import org.philblandford.ascore2.external.export.mxml.`in`.reader.getChild
import org.philblandford.ascore2.external.export.mxml.`in`.reader.getChildren
import org.philblandford.ascore2.external.export.mxml.out.MxmlInstrumentName
import org.philblandford.ascore2.external.export.mxml.out.MxmlMidiChannel
import org.philblandford.ascore2.external.export.mxml.out.MxmlMidiInstrument
import org.philblandford.ascore2.external.export.mxml.out.MxmlMidiProgram
import org.philblandford.ascore2.external.export.mxml.out.MxmlMidiUnpitched
import org.philblandford.ascore2.external.export.mxml.out.MxmlPartAbbreviation
import org.philblandford.ascore2.external.export.mxml.out.MxmlPartList
import org.philblandford.ascore2.external.export.mxml.out.MxmlPartName
import org.philblandford.ascore2.external.export.mxml.out.MxmlScoreInstrument
import org.philblandford.ascore2.external.export.mxml.out.MxmlScorePart
import org.w3c.dom.Element

internal fun parsePartList(partList: Element): MxmlPartList? {
  val scorePartNodes = partList.getElementsByTagName("score-part")
  val scoreParts = (0 until scorePartNodes.length).mapNotNull { num ->
    scorePartNodes.item(num)?.let { parseScorePart(it as Element) }
  }
  return MxmlPartList(scoreParts)
}

private fun parseScorePart(scorePart: Element): MxmlScorePart? {
  return scorePart.attributes.getNamedItem("id")?.let { id ->
    scorePart.getChild("part-name")?.textContent?.let { partName ->
      val scoreInstrument = scorePart.parseScoreInstrument()
      val midiInstrument = scorePart.parseMidiInstrument()
      val partAbbreviation = scorePart.getChild("part-abbreviation")?.textContent?.let {
        MxmlPartAbbreviation(it)
      }
      MxmlScorePart(
        id.textContent,
        MxmlPartName(partName),
        partAbbreviation,
        scoreInstrument,
        midiInstrument
      )
    }
  }
}

private fun Element.parseScoreInstrument(): List<MxmlScoreInstrument> {
  return getChildren("score-instrument").mapNotNull { element ->
    element.getAttribute("id")?.let { instrId ->
      element.getChild("instrument-name")?.let { name ->
        MxmlScoreInstrument(instrId, MxmlInstrumentName(name.textContent))
      }
    }
  }
}

private fun Element.parseMidiInstrument(): List<MxmlMidiInstrument> {
  return getChildren("midi-instrument").mapNotNull { element ->
    element.getAttribute("id")?.let { instrId ->
      element.getChild("midi-program")?.let { program ->
        element.getChild("midi-channel")?.let { channel ->
          val unpitched = element.getChild("midi-unpitched")
          MxmlMidiInstrument(
            instrId,
            MxmlMidiChannel(channel.textContent.toInt()),
            MxmlMidiProgram(program.textContent.toInt()),
            unpitched?.let { MxmlMidiUnpitched(it.textContent.toInt()) }
          )
        }
      }
    }
  }
}