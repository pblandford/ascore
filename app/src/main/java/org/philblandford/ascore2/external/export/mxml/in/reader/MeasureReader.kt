package com.philblandford.ascore.external.export.mxml.`in`.reader

import com.philblandford.ascore.external.export.mxml.out.*
import org.w3c.dom.Element

internal fun Element.parseMeasure(): MxmlMeasure {
  val measureElements = getChildren().mapNotNull { it.parse() }
  return MxmlMeasure("1", measureElements)
}

private fun Element.parse(): MxmlMeasureElement? {
  return when (nodeName) {
    "print" -> parsePrint()
    "attributes" -> parseAttributes()
    "direction" -> parseDirection()
    "note" -> parseNote()
    "backup" -> parseBackup()
    "forward" -> parseForward()
    "harmony" -> parseHarmony()
    "barline" -> parseBarline()
    else -> null
  }
}


private fun Element.parseBackup(): MxmlBackup? {
  return getTextElem("duration")?.let {
    MxmlBackup(MxmlDuration(it.toInt()))
  }
}

private fun Element.parseForward(): MxmlForward? {
  return getTextElem("duration")?.let {
    MxmlForward(MxmlDuration(it.toInt()))
  }
}

private fun Element.parseHarmony(): MxmlHarmony? {
  return getChild("root")?.parseRoot()?.let { root ->
    getTextElem("kind")?.let { kind ->
      val bass = getChild("bass")?.parseBass()
      val offset = getChild("offset")?.parseOffset()
      MxmlHarmony(root, MxmlKind(kind), bass, offset)
    }
  }
}

private fun Element.parseRoot(): MxmlRoot? {
  return getChild("root-step")?.let { bass ->
    val alter = getChild("root-alter")?.let {
      MxmlRootAlter(it.textContent.toInt())
    }
    MxmlRoot(MxmlRootStep(bass.textContent), alter)
  }
}

private fun Element.parseBass(): MxmlBass? {
  return getChild("bass-step")?.let { bass ->
    val alter = getChild("bass-alter")?.let {
      MxmlBassAlter(it.textContent.toInt())
    }
    MxmlBass(MxmlBassStep(bass.textContent), alter)
  }
}

private fun Element.parseOffset() : MxmlOffset? {
  return MxmlOffset(textContent.toInt())
}

private fun Element.parseBarline(): MxmlBarline? {
  val repeat = getChild("repeat")?.let { rpt ->
    MxmlRepeat(rpt.getAttribute("direction"))
  }
  val ending = getChild("ending")?.parseEnding()
  val location = getAttribute("location")
  val barStyle = getChild("bar-style")?.textContent?.let { MxmlBarStyle(it) }
  return MxmlBarline(location, barStyle, ending, repeat)
}

private fun Element.parseEnding(): MxmlEnding? {
  return getAttribute("number")?.let { number ->
    getAttribute("type")?.let { type ->
      try {
        MxmlEnding(number.toInt(), type)
      } catch (e:NumberFormatException) {
        MxmlEnding(1, type)
      }
    }
  }
}

private fun Element.parsePrint(): MxmlPrint? {
  val print = getAttribute("new-system")
  return MxmlPrint(print, null)
}