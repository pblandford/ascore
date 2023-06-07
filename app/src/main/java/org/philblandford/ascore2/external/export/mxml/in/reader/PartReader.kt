package com.philblandford.ascore.external.export.mxml.`in`.reader

import org.philblandford.ascore2.external.export.mxml.out.MxmlPart
import com.philblandford.kscore.log.ksLogd
import org.philblandford.ascore2.external.export.mxml.`in`.reader.getChildren
import org.w3c.dom.Element


internal fun parsePart(part: Element): MxmlPart {
  val id = part.getAttribute("id")
  ksLogd("Getting part $id")
  val measureNodes = part.getChildren("measure")
  val measures = measureNodes.map { it.parseMeasure() }
  return MxmlPart(id, measures)
}