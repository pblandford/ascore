package com.philblandford.ascore.external.export.mxml.`in`.reader

import com.philblandford.ascore.external.export.mxml.out.*
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

