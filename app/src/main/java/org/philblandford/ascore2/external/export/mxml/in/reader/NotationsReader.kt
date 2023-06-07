package com.philblandford.ascore.external.export.mxml.`in`.reader

import org.philblandford.ascore2.external.export.mxml.`in`.reader.getChildren
import org.philblandford.ascore2.external.export.mxml.out.MxmlAccent
import org.philblandford.ascore2.external.export.mxml.out.MxmlAccidentalMark
import org.philblandford.ascore2.external.export.mxml.out.MxmlArpeggiate
import org.philblandford.ascore2.external.export.mxml.out.MxmlArticulations
import org.philblandford.ascore2.external.export.mxml.out.MxmlArticulationsElement
import org.philblandford.ascore2.external.export.mxml.out.MxmlFermata
import org.philblandford.ascore2.external.export.mxml.out.MxmlFingering
import org.philblandford.ascore2.external.export.mxml.out.MxmlGlissando
import org.philblandford.ascore2.external.export.mxml.out.MxmlInvertedMordent
import org.philblandford.ascore2.external.export.mxml.out.MxmlMordent
import org.philblandford.ascore2.external.export.mxml.out.MxmlNotations
import org.philblandford.ascore2.external.export.mxml.out.MxmlOrnaments
import org.philblandford.ascore2.external.export.mxml.out.MxmlOrnamentsElement
import org.philblandford.ascore2.external.export.mxml.out.MxmlSlur
import org.philblandford.ascore2.external.export.mxml.out.MxmlStaccatissimo
import org.philblandford.ascore2.external.export.mxml.out.MxmlStaccato
import org.philblandford.ascore2.external.export.mxml.out.MxmlStrongAccent
import org.philblandford.ascore2.external.export.mxml.out.MxmlTechnical
import org.philblandford.ascore2.external.export.mxml.out.MxmlTenuto
import org.philblandford.ascore2.external.export.mxml.out.MxmlTied
import org.philblandford.ascore2.external.export.mxml.out.MxmlTremolo
import org.philblandford.ascore2.external.export.mxml.out.MxmlTrillMark
import org.philblandford.ascore2.external.export.mxml.out.MxmlTuplet
import org.philblandford.ascore2.external.export.mxml.out.MxmlTurn
import org.philblandford.ascore2.external.export.mxml.out.MxmlWavyLine
import org.w3c.dom.Element

internal fun Element.parseNotations(): MxmlNotations? {
  val elements = getChildren().mapNotNull { child ->
    when (child.nodeName) {
      "tied" -> child.parseTied()
      "glissando" -> child.parseGlissando()
      "slur" -> child.parseSlur()
      "tuplet" -> child.parseTuplet()
      "articulations" -> child.parseArticulations()
      "ornaments" -> child.parseOrnaments()
      "arpeggiate" -> child.parseArpeggiate()
      "technical" -> child.parseTechnical()
      "fermata" -> child.parseFermata()
      else -> null
    }
  }
  return MxmlNotations(elements)
}

private fun Element.parseTied(): MxmlTied? {
  return getAttribute("type")?.let { type ->
    MxmlTied(type)
  }
}

private fun Element.parseSlur(): MxmlSlur? {
  return getAttribute("type")?.let { type ->
    val number = getAttribute("number")
    val placement = getAttribute("placement")
    MxmlSlur(type, number, placement)
  }
}

private fun Element.parseGlissando(): MxmlGlissando? {
  return getAttribute("type")?.let { type ->
    val lineType = getAttribute("line-type")
    MxmlGlissando(type, lineType)
  }
}

private fun Element.parseTuplet(): MxmlTuplet? {
  return getAttribute("type")?.let { type ->
    MxmlTuplet(type)
  }
}

private fun Element.parseArticulations(): MxmlArticulations? {
  val elements = getChildren().mapNotNull { strToMxmlArticulation(it.tagName) }
  return MxmlArticulations(elements)
}

private fun Element.parseArpeggiate(): MxmlArpeggiate? {
  return MxmlArpeggiate()
}


private fun strToMxmlArticulation(str: String): MxmlArticulationsElement? {
  return when (str) {
    "accent" -> MxmlAccent()
    "staccato" -> MxmlStaccato()
    "staccatissimo" -> MxmlStaccatissimo()
    "strong-accent" -> MxmlStrongAccent()
    "tenuto" -> MxmlTenuto()
    else -> null
  }
}

private fun Element.parseOrnaments(): MxmlOrnaments? {
  val marks = getChildren("accidental-mark").mapNotNull { it.parseAccidentalMark() }
  val elements = getChildren().mapNotNull { strToMxmlOrnament(it) }
  return MxmlOrnaments(elements, marks)
}

private fun Element.parseAccidentalMark(): MxmlAccidentalMark? {
  val placement = getAttribute("placement")
  return MxmlAccidentalMark(placement, textContent)
}

private fun strToMxmlOrnament(element: Element): MxmlOrnamentsElement? {
  return when (element.tagName) {
    "inverted-mordent" -> MxmlInvertedMordent()
    "mordent" -> MxmlMordent()
    "trill-mark" -> MxmlTrillMark()
    "turn" -> MxmlTurn()
    "tremolo" -> element.parseTremolo()
    "wavy-line" -> element.parseWavyLine()
    else -> null
  }
}

private fun Element.parseTremolo(): MxmlTremolo? {
  val beats = textContent.toInt()
  return MxmlTremolo("start", beats)
}

private fun Element.parseWavyLine(): MxmlWavyLine? {
  return getAttribute("type")?.let { type ->
    val placement = getAttribute("placement")
    MxmlWavyLine(type, placement)
  }
}

private fun Element.parseTechnical(): MxmlTechnical? {
  val fingerings = getChildren("fingering").map {
    MxmlFingering(it.textContent)
  }
  return MxmlTechnical(fingerings)
}

private fun Element.parseFermata(): MxmlFermata? {
  val shape = if (textContent.isEmpty()) "" else textContent
  return MxmlFermata(shape)
}