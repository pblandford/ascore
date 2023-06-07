package com.philblandford.ascore.external.export.mxml.`in`.reader

import org.philblandford.ascore2.external.export.mxml.`in`.reader.getAttrOrNull
import org.philblandford.ascore2.external.export.mxml.`in`.reader.getChild
import org.philblandford.ascore2.external.export.mxml.`in`.reader.getChildren
import org.philblandford.ascore2.external.export.mxml.out.MxmlBeatUnit
import org.philblandford.ascore2.external.export.mxml.out.MxmlBeatUnitDot
import org.philblandford.ascore2.external.export.mxml.out.MxmlDashes
import org.philblandford.ascore2.external.export.mxml.out.MxmlDirection
import org.philblandford.ascore2.external.export.mxml.out.MxmlDirectionType
import org.philblandford.ascore2.external.export.mxml.out.MxmlDynamics
import org.philblandford.ascore2.external.export.mxml.out.MxmlDynamicsComponent
import org.philblandford.ascore2.external.export.mxml.out.MxmlF
import org.philblandford.ascore2.external.export.mxml.out.MxmlFf
import org.philblandford.ascore2.external.export.mxml.out.MxmlFff
import org.philblandford.ascore2.external.export.mxml.out.MxmlFfff
import org.philblandford.ascore2.external.export.mxml.out.MxmlFffff
import org.philblandford.ascore2.external.export.mxml.out.MxmlFfffff
import org.philblandford.ascore2.external.export.mxml.out.MxmlFp
import org.philblandford.ascore2.external.export.mxml.out.MxmlFz
import org.philblandford.ascore2.external.export.mxml.out.MxmlMetronome
import org.philblandford.ascore2.external.export.mxml.out.MxmlMf
import org.philblandford.ascore2.external.export.mxml.out.MxmlMp
import org.philblandford.ascore2.external.export.mxml.out.MxmlOctaveShift
import org.philblandford.ascore2.external.export.mxml.out.MxmlP
import org.philblandford.ascore2.external.export.mxml.out.MxmlPedal
import org.philblandford.ascore2.external.export.mxml.out.MxmlPerMinute
import org.philblandford.ascore2.external.export.mxml.out.MxmlPp
import org.philblandford.ascore2.external.export.mxml.out.MxmlPpp
import org.philblandford.ascore2.external.export.mxml.out.MxmlPppp
import org.philblandford.ascore2.external.export.mxml.out.MxmlPpppp
import org.philblandford.ascore2.external.export.mxml.out.MxmlPppppp
import org.philblandford.ascore2.external.export.mxml.out.MxmlRehearsal
import org.philblandford.ascore2.external.export.mxml.out.MxmlRf
import org.philblandford.ascore2.external.export.mxml.out.MxmlRfz
import org.philblandford.ascore2.external.export.mxml.out.MxmlSf
import org.philblandford.ascore2.external.export.mxml.out.MxmlSffz
import org.philblandford.ascore2.external.export.mxml.out.MxmlSfp
import org.philblandford.ascore2.external.export.mxml.out.MxmlSfpp
import org.philblandford.ascore2.external.export.mxml.out.MxmlSfz
import org.philblandford.ascore2.external.export.mxml.out.MxmlStaff
import org.philblandford.ascore2.external.export.mxml.out.MxmlWedge
import org.philblandford.ascore2.external.export.mxml.out.MxmlWords
import org.w3c.dom.Element

internal fun Element.parseDirection(): MxmlDirection? {
  val placement = getAttribute("placement")
  val staff = getChild("staff")?.let { MxmlStaff(it.textContent.toInt()) }

  val directionTypes = getChildren("direction-type").mapNotNull { directionType ->
    val name = directionType.childNodes.item(1)?.nodeName
    val mxmlDt = when (name) {
      "metronome" -> directionType.getChild("metronome")?.parseMetronome()
      "rehearsal" -> directionType.getChild("rehearsal")?.parseRehearsal()
      "words" -> directionType.getChild("words")?.parseWords()
      "dynamics" -> directionType.getChild("dynamics")?.parseDynamics()
      "wedge" -> directionType.getChild("wedge")?.parseWedge()
      "dashes" -> directionType.getChild("dashes")?.parseDashes()
      "octave-shift" -> directionType.getChild("octave-shift")?.parseOctaveShift()
      "pedal" -> directionType.getChild("pedal")?.parsePedal()
      else -> null
    }
    mxmlDt?.let { MxmlDirectionType(it) }

  }
  return MxmlDirection(placement, directionTypes, staff)
}

private fun Element.parseMetronome(): MxmlMetronome? {
  return getChild("beat-unit")?.textContent?.let { duration ->
    getChild("per-minute")?.textContent?.toInt()?.let { bpm ->
      val dots = getElementsByTagName("beat-unit-dot").length
      MxmlMetronome(
        MxmlBeatUnit(duration),
        (0 until dots).map { MxmlBeatUnitDot() },
        MxmlPerMinute(bpm)
      )
    }
  }
}

private fun Element.parseRehearsal(): MxmlRehearsal? {
  return textContent?.let { text -> MxmlRehearsal(text) }
}

private fun Element.parseWords(): MxmlWords? {
  return textContent?.let { text -> MxmlWords(text) }
}

private fun Element.parseDynamics(): MxmlDynamics? {
  getChildren().forEach {
    return textToMxmlDynamic(it)?.let { MxmlDynamics(it) }
  }
  return null
}

private fun textToMxmlDynamic(element: Element): MxmlDynamicsComponent? {
  return when (element.nodeName) {
    "pppppp" -> MxmlPppppp()
    "ppppp" -> MxmlPpppp()
    "pppp" -> MxmlPppp()
    "ppp" -> MxmlPpp()
    "pp" -> MxmlPp()
    "p" -> MxmlP()
    "ffffff" -> MxmlFfffff()
    "fffff" -> MxmlFffff()
    "ffff" -> MxmlFfff()
    "fff" -> MxmlFff()
    "ff" -> MxmlFf()
    "f" -> MxmlF()
    "fp" -> MxmlFp()
    "fz" -> MxmlFz()
    "mf" -> MxmlMf()
    "mp" -> MxmlMp()
    "rf" -> MxmlRf()
    "rfz" -> MxmlRfz()
    "sf" -> MxmlSf()
    "sfz" -> MxmlSfz()
    "sffz" -> MxmlSffz()
    "sfp" -> MxmlSfp()
    "sfpp" -> MxmlSfpp()
    else -> null
  }
}

private fun Element.parseWedge(): MxmlWedge? {
  return getAttribute("type")?.let { type ->
    MxmlWedge(type)
  }
}

private fun Element.parseDashes(): MxmlDashes? {
  return getAttribute("type")?.let { type ->
    MxmlDashes(type)
  }
}

private fun Element.parseOctaveShift(): MxmlOctaveShift? {
  return getAttribute("type")?.let { type ->
    val size = getAttribute("size")?.toInt()
    val number = getAttrOrNull("number")?.toInt()
    MxmlOctaveShift(type, size, number)
  }
}


private fun Element.parsePedal(): MxmlPedal? {
  return getAttribute("type")?.let { type ->
    val line = getAttribute("line")
    val sign = getAttribute("sign")
    MxmlPedal(type, line, sign)
  }
}

