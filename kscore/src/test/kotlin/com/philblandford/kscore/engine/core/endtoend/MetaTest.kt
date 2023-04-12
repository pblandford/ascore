package com.philblandford.kscore.engine.core.endtoend

import assertEqual
import com.philblandford.kscore.engine.core.area.Coord
import com.philblandford.kscore.engine.core.score.ScoreContainer
import com.philblandford.kscore.engine.types.*
import core.representation.RepTest
import org.junit.Test

class MetaTest : RepTest() {

  @Test
  fun testSetTitle() {
    setMeta(MetaType.TITLE, "Wibble")
    RVA("Title", eZero())
  }

  @Test
  fun testTitleNotOnPage2() {
    RCD(bars = 100)
    setMeta(MetaType.TITLE, "Wibble")
    val page = REP().getPage(2)!!.base
    assert(page.findByTagSingle("Title") == null)
  }

  @Test
  fun testSetSubTitle() {
    setMeta(MetaType.SUBTITLE, "Wibble")
    RVA("Subtitle", eZero())
  }

  @Test
  fun testSetComposer() {
    setMeta(MetaType.COMPOSER, "By me")
    RVA("Composer", eZero())
  }

  @Test
  fun testMoveTitle() {
    setMeta(MetaType.TITLE, "Wibble")
    val before = getArea("Title", eZero())!!.coord
    setMetaOffset(MetaType.TITLE, 20, 20)
    val after = getArea("Title", eZero())!!.coord
    assertEqual(before.plus(Coord(20, 20)), after)
  }

  @Test
  fun testMoveSubTitle() {
    setMeta(MetaType.SUBTITLE, "Wibble")
    val before = getArea("Subtitle", eZero())!!.coord
    setMetaOffset(MetaType.SUBTITLE, 20, 20)
    val after = getArea("Subtitle", eZero())!!.coord
    assertEqual(before.plus(Coord(20, 20)), after)
  }

  @Test
  fun testMoveComposer() {
    setMeta(MetaType.COMPOSER, "Wibble")
    val before = getArea("Composer", eZero())!!.coord
    setMetaOffset(MetaType.COMPOSER, 20, 20)
    val after = getArea("Composer", eZero())!!.coord
    assertEqual(before.plus(Coord(20, 20)), after)
  }

  @Test
  fun testDeleteTitle() {
    setMeta(MetaType.TITLE, "Wibble")
    SDE(EventType.TITLE, eZero(), paramMapOf(EventParam.TYPE to MetaType.TITLE))
    assert(getArea("Title", eZero()) == null)
  }

  @Test
  fun testDeleteTitleSparesSubtitle() {
    setMeta(MetaType.TITLE, "Wibble")
    setMeta(MetaType.SUBTITLE, "Wobble")
    val subTitleWidth = getArea("Subtitle", eZero())?.area?.width!!
    SDE(EventType.TITLE, eZero(), paramMapOf(EventParam.TYPE to MetaType.TITLE))
    assert(getArea("Title", eZero()) == null)
    assertEqual(subTitleWidth, getArea("Subtitle", eZero())?.area?.width)
  }


  private fun setMeta(metaType: MetaType, text: String) {
    SAE(
      Event(metaType.toEventType(), paramMapOf(EventParam.TEXT to text)),
      eZero()
    )
  }


  private fun setMetaOffset(metaType: MetaType, x: Int, y: Int) {
    SSP(metaType.toEventType(), EventParam.HARD_START, Coord(x, y), eZero())
  }
}

