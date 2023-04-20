package com.philblandford.kscore.engine.core.endtoend

import com.philblandford.kscore.engine.core.area.Coord
import com.philblandford.kscore.engine.core.representation.LYRIC_SIZE
import com.philblandford.kscore.engine.duration.crotchet
import com.philblandford.kscore.engine.types.*
import com.philblandford.kscore.engine.core.representation.RepTest
import org.junit.Test

class LyricTest : RepTest() {

  @Test
  fun testAddLyric() {
    SMV()
    SAE(EventType.LYRIC, eventAddress = eav(1), params = paramMapOf(EventParam.TEXT to "wibble",
      EventParam.NUMBER to 1))
    RVA("Lyric", eav(1).copy(id = 1))
  }

  @Test
  fun testAddLyricSegmentExpands() {
    SMV()
    SAE(EventType.LYRIC, eventAddress = eav(1), params = paramMapOf(EventParam.TEXT to "wibbleeeeeeee",
      EventParam.NUMBER to 1))
    val segment = getArea("Segment", ea(1))?.coord?.x!!
    val segment2 = getArea("Segment", ea(1, crotchet()))?.coord?.x!!
    val lyric = getArea("Lyric", eav(1).copy(id = 1))?.area!!
    val segWidth = segment2 - segment
    assert(segWidth >= lyric.width)
  }


  @Test
  fun testAddLyricTwoLines() {
    SMV()
    SAE(EventType.LYRIC, eventAddress = eav(1), params = paramMapOf(EventParam.TEXT to "wibble",
      EventParam.NUMBER to 1))
    SAE(EventType.LYRIC, eventAddress = eav(1), params = paramMapOf(EventParam.TEXT to "wobble",
      EventParam.NUMBER to 2))
    val lyric1 = getArea("Lyric", eav(1).copy(id = 1))!!
    val lyric2 = getArea("Lyric", eav(1).copy(id = 2))!!

    assert(lyric1.coord.y + lyric1.area.height < lyric2.coord.y)
  }

  @Test
  fun testSetLyricSizeOption() {
    SMV()
    SAE(EventType.LYRIC, eventAddress = eav(1), params = paramMapOf(EventParam.TEXT to "wibble",
      EventParam.NUMBER to 1))
    val existingSize = getArea("Lyric", eav(1).copy(id = 1))?.area?.width!!
    SSO(EventParam.OPTION_LYRIC_SIZE, LYRIC_SIZE + 20)
    val newSize = getArea("Lyric", eav(1).copy(id = 1))?.area?.width!!
    assert(newSize > existingSize)
  }

  @Test
  fun testSetLyricOffsetOption() {
    SMV()
    SAE(EventType.LYRIC, eventAddress = eav(1), params = paramMapOf(EventParam.TEXT to "wibble",
      EventParam.NUMBER to 1))
    val existingY = getArea("Lyric", eav(1).copy(id = 1))?.coord?.y!!
    SSO(EventParam.OPTION_LYRIC_OFFSET_BY_POSITION, false to 20)
    val newY = getArea("Lyric", eav(1).copy(id = 1))?.coord?.y!!
    assert(newY > existingY)
  }

  @Test
  fun testSetLyricIndividualOffset() {
    SAE(EventType.LYRIC, eav(1), paramMapOf(EventParam.TEXT to "wibble", EventParam.NUMBER to 1))
    SAE(EventType.LYRIC, eav(2), paramMapOf(EventParam.TEXT to "wibble", EventParam.NUMBER to 1))

    val existingY1 = getArea("Lyric", eav(1).copy(id = 1))?.coord?.y!!
    val existingY2 = getArea("Lyric", eav(2).copy(id = 1))?.coord?.y!!
    SSP(EventType.LYRIC, EventParam.HARD_START, Coord(0,20), eav(1).copy(id = 1))
    val newY1 = getArea("Lyric", eav(1).copy(id = 1))?.coord?.y!!
    val newY2 = getArea("Lyric", eav(2).copy(id = 1))?.coord?.y!!
    assert(newY1 > existingY1)
    assert(newY2 == existingY2)
  }

  @Test
  fun testAddLyricAbove() {
    SMV()
    SAE(EventType.LYRIC, eventAddress = eav(1), params = paramMapOf(EventParam.TEXT to "wibble",
      EventParam.NUMBER to 1))
    SSO(EventParam.OPTION_LYRIC_POSITIONS, 1 to true)
    val staveY = getArea("StaveLines", ea(1))!!.coord.y
    val lyricBottom = getArea("Lyric", eav(1).copy(id = 1))?.let { it.coord.y + it.area.height }!!
    assert(lyricBottom < staveY)
  }

  @Test
  fun testAdjustLyricAbove() {
    SMV()
    SAE(EventType.LYRIC, eventAddress = eav(1), params = paramMapOf(EventParam.TEXT to "wibble",
      EventParam.NUMBER to 1))
    SSO(EventParam.OPTION_LYRIC_POSITIONS, 1 to true)
    val existingY = getArea("Lyric", eav(1).copy(id = 1))?.coord?.y!!
    SSO(EventParam.OPTION_LYRIC_OFFSET_BY_POSITION, true to -20)
    val newY = getArea("Lyric", eav(1).copy(id = 1))?.coord?.y!!
    assert(newY < existingY)
  }
}