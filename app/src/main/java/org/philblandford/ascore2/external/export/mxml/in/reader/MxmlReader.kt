package org.philblandford.ascore2.external.export.mxml.`in`.reader

import com.philblandford.ascore.external.export.mxml.`in`.converter.mxmlScoreToScore
import com.philblandford.ascore.external.export.mxml.`in`.reader.parsePart
import com.philblandford.ascore.external.export.mxml.`in`.reader.parsePartList
import org.philblandford.ascore2.external.export.xml.readXml
import com.philblandford.kscore.api.InstrumentGetter
import com.philblandford.kscore.api.ProgressFunc2
import com.philblandford.kscore.api.noProgress2
import com.philblandford.kscore.engine.core.score.Score
import com.philblandford.kscore.engine.util.pMap
import com.philblandford.kscore.log.ksLogd
import kotlinx.coroutines.runBlocking
import org.philblandford.ascore2.external.export.mxml.out.MalformedXmlException
import org.philblandford.ascore2.external.export.mxml.out.MxmlBottomMargin
import org.philblandford.ascore2.external.export.mxml.out.MxmlCreator
import org.philblandford.ascore2.external.export.mxml.out.MxmlCredit
import org.philblandford.ascore2.external.export.mxml.out.MxmlCreditWords
import org.philblandford.ascore2.external.export.mxml.out.MxmlDefaults
import org.philblandford.ascore2.external.export.mxml.out.MxmlIdentification
import org.philblandford.ascore2.external.export.mxml.out.MxmlLeftMargin
import org.philblandford.ascore2.external.export.mxml.out.MxmlPageHeight
import org.philblandford.ascore2.external.export.mxml.out.MxmlPageLayout
import org.philblandford.ascore2.external.export.mxml.out.MxmlPageMargins
import org.philblandford.ascore2.external.export.mxml.out.MxmlPageWidth
import org.philblandford.ascore2.external.export.mxml.out.MxmlRightMargin
import org.philblandford.ascore2.external.export.mxml.out.MxmlScorePartwise
import org.philblandford.ascore2.external.export.mxml.out.MxmlStaffDistance
import org.philblandford.ascore2.external.export.mxml.out.MxmlStaffLayout
import org.philblandford.ascore2.external.export.mxml.out.MxmlSystemDistance
import org.philblandford.ascore2.external.export.mxml.out.MxmlSystemLayout
import org.philblandford.ascore2.external.export.mxml.out.MxmlSystemMargins
import org.philblandford.ascore2.external.export.mxml.out.MxmlTopMargin
import org.philblandford.ascore2.external.export.mxml.out.MxmlTopSystemDistance
import org.philblandford.ascore2.external.export.mxml.out.MxmlWork
import org.philblandford.ascore2.external.export.mxml.out.MxmlWorkTitle
import org.w3c.dom.Element

fun scoreFromMxml(
  mxml: String,
  dtdPath: String,
  instrumentGetter: InstrumentGetter,
  progress: ProgressFunc2 = noProgress2
): Score? {
  ksLogd("Preparing document builder")
  progress("Reading from file", 0f)
  return readXml(mxml, dtdPath)?.let { dom ->
    val mxmlScore = parseScorePartwise(dom.documentElement, progress)
    mxmlScore?.let { mxmlScoreToScore(it, instrumentGetter, progress) }
  }
}

private fun parseScorePartwise(
  scorePartwise: Element,
  progress: ProgressFunc2 = noProgress2
): MxmlScorePartwise? {

  if (scorePartwise.getChild("score-partwise") == null && scorePartwise.tagName != "score-partwise") {
    throw(MalformedXmlException("This does not appear to be a Music XML document: top node is '${scorePartwise.tagName}', expected 'score-partwise'"))
  }

  ksLogd("Getting preliminaries")
  val version = scorePartwise.getAttribute("version")
  val work = parseWork(scorePartwise)
  val identification = parseIdentification(scorePartwise)
  val defaults = parseDefaults(scorePartwise)
  val credits = parseCredits(scorePartwise)
  ksLogd("Getting part list")
  val partList = scorePartwise.getChild("part-list")?.let {
    parsePartList(it)
  }

  var partNum = 0

  return partList?.let {
    val partElements = scorePartwise.getElementsByTagName("part")

    val parts = runBlocking {
      (0 until partElements.length).toList().pMap { num ->
        partElements.item(num)?.let {
          progress(
            "Parsing ${partList.scoreParts.toList()[num].partName.name}",
            (partNum.toFloat() / partList.scoreParts.count() * 100)
          )
          val p = parsePart(it as Element)
          partNum++
          p
        }
      }.toList().filterNotNull()
    }
    MxmlScorePartwise(version, work, identification, defaults, credits, partList, parts)
  }
}

private fun parseWork(scorePartwise: Element): MxmlWork? {
  return scorePartwise.getChild("work")?.let { elem ->
    elem.getChild("work-title")?.let { title ->
      MxmlWork(MxmlWorkTitle(title.textContent))
    }
  }
}

private fun parseIdentification(scorePartwise: Element): MxmlIdentification? {
  return scorePartwise.getChild("identification")?.let { elem ->
    val creators =
      elem.getChildren("creator")?.map { MxmlCreator(it.getAttribute("type"), it.textContent) }
    MxmlIdentification(creators ?: listOf())
  }
}

private fun parseDefaults(scorePartwise: Element): MxmlDefaults? {
  return scorePartwise.getChild("defaults")?.let { elem ->
    val pageLayout = elem.getChild("page-layout")?.parsePageLayout()
    val systemLayout = elem.getChild("system-layout")?.parseSystemLayout()
    val staffLayout = elem.getChild("staff-layout")?.parseStaffLayout()
    MxmlDefaults(null, pageLayout, systemLayout, staffLayout)
  }
}

private fun Element.parsePageLayout(): MxmlPageLayout? {

  val pageMargins = getChildren("page-margins").mapNotNull { it.parsePageMargin() }
  return getChild("page-width")?.let { width ->
    getChild("page-height")?.let { height ->
      MxmlPageLayout(
        MxmlPageHeight(height.textContent.toFloat()),
        MxmlPageWidth(width.textContent.toFloat()),
        pageMargins
      )
    }
  }
}

private fun Element.parsePageMargin(): MxmlPageMargins? {
  return getChild("left-margin")?.let { left ->
    getChild("right-margin")?.let { right ->
      getChild("top-margin")?.let { top ->
        getChild("bottom-margin")?.let { bottom ->
          val type = getAttribute("type")
          MxmlPageMargins(
            type,
            MxmlLeftMargin(left.textContent.toFloat()),
            MxmlRightMargin(right.textContent.toFloat()),
            MxmlTopMargin(top.textContent.toFloat()),
            MxmlBottomMargin(bottom.textContent.toFloat())
          )
        }
      }
    }
  }
}


private fun Element.parseSystemLayout(): MxmlSystemLayout? {

  val systemMargins = getChild("system-margins")?.parseSystemMargin()
  val systemDistance =
    getChild("system-distance")?.let { MxmlSystemDistance(it.textContent.toFloat()) }
  val topSystemDistance =
    getChild("top-system-distance")?.let { MxmlTopSystemDistance(it.textContent.toFloat()) }

  return MxmlSystemLayout(systemMargins, systemDistance, topSystemDistance)
}

private fun Element.parseSystemMargin(): MxmlSystemMargins? {
  return getChild("left-margin")?.let { left ->
    getChild("right-margin")?.let { right ->
      MxmlSystemMargins(
        MxmlLeftMargin(left.textContent.toFloat()),
        MxmlRightMargin(right.textContent.toFloat())
      )
    }
  }
}

private fun Element.parseStaffLayout(): MxmlStaffLayout? {

  val staffDistance =
    getChild("staff-distance")?.let { MxmlStaffDistance(it.textContent.toFloat()) }

  return MxmlStaffLayout(staffDistance)
}

private fun parseCredits(scorePartwise: Element): List<MxmlCredit> {
  val creditElements = scorePartwise.getElementsByTagName("credit")
  return (0 until creditElements.length).mapNotNull {
    creditElements.item(it)?.let {
      (it as Element).getChild("credit-words")?.let {
        val words = MxmlCreditWords(
          it.getAttribute("justify"),
          it.getAttribute("valign"), it.textContent
        )
        MxmlCredit(words)
      }
    }
  }
}

internal fun Element.getAttrOrNull(name: String): String? {
  val attr = getAttribute(name)
  return if (attr == "") {
    null
  } else {
    attr
  }
}

internal fun Element.getTextElem(name: String): String? {
  return getChild(name)?.textContent
}

internal fun Element.getChild(name: String): Element? {
  val children = childNodes
  (0..children.length).mapNotNull {
    children.item(it)?.let { if (it is Element && it.tagName == name) return it }
  }
  return null
}

internal fun Element.getChildren(name: String): Iterable<Element> {
  return getChildren().filter { it.tagName == name }
}

internal fun Element.getChildren(): Iterable<Element> {
  val children = childNodes
  return (0..childNodes.length).mapNotNull {
    children.item(it)
  }.filterIsInstance<Element>()
}
