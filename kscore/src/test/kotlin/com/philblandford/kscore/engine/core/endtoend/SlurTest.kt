package com.philblandford.kscore.engine.core.endtoend

import assertEqual
import com.philblandford.kscore.engine.core.area.Coord
import com.philblandford.kscore.engine.core.representation.RepTest
import com.philblandford.kscore.engine.types.*


import com.philblandford.kscore.engine.duration.crotchet
import com.philblandford.kscore.engine.duration.semiquaver
import grace
import org.junit.Test

class SlurTest : RepTest() {

  @Test
  fun testAddSlur() {
    SMV()
    SMV(eventAddress = eav(1, crotchet()))
    SAE(EventType.SLUR, params = paramMapOf(EventParam.END to ea(2), EventParam.IS_UP to true))
    RVA("Slur", ea(1))
  }

  @Test
  fun testAddSlurBelow() {
    SMV(60)
    SMV(60, eventAddress = eav(1, crotchet()))
    SAE(
      EventType.SLUR,
      params = paramMapOf(EventParam.END to ea(1, crotchet()), EventParam.IS_UP to false)
    )
    val slur = getArea("Slur", ea(1).copy(id = 1))!!
    val note = getArea("Tadpole", eav(1).copy(id = 1))!!
    val endNote = getArea("Tadpole", eav(1, crotchet()).copy(id = 1))!!
    assert(slur.coord.y > note.coord.y)
    assert(note.coord.x < slur.coord.x && note.coord.x + note.area.width > slur.coord.x)
  }

  @Test
  fun testAddSlurOverhang() {
    SMV()
    val stave2 = getStaveBar(1)
    SMV(eventAddress = eav(stave2))
    SAE(EventType.SLUR, params = paramMapOf(EventParam.END to ea(stave2), EventParam.IS_UP to true))
    val slurs = getAreas("Slur")
    assertEqual(2, slurs.size)
  }

  @Test
  fun testAddSlurEmptyBar() {
    SAE(EventType.SLUR, params = paramMapOf(EventParam.END to ea(2), EventParam.IS_UP to true))
    assert(isLeft("Slur", ea(1), "BarLine", eas(2,1,0))!!)
  }

  @Test
  fun testAddSlurGrace() {
    grace()
    grace()
    SAE(
      EventType.SLUR,
      eventAddress = eag(1),
      params = paramMapOf(
        EventParam.END to eag(1, graceOffset = semiquaver()),
        EventParam.IS_UP to true
      )
    )
    assert(getArea("Slur", eag(1))?.area?.width!! > 0)
  }

  @Test
  fun testAddSlurGraceToNormal() {
    SMV()
    grace()
    grace()
    SAE(
      EventType.SLUR,
      eventAddress = eag(1),
      params = paramMapOf(
        EventParam.END to ea(1),
        EventParam.IS_UP to true
      )
    )
    assert(getArea("Slur", eag(1))?.area?.width!! > 0)
  }

  @Test
  fun testAddSlurGraceSetCoord() {
    grace()
    grace()
    SAE(
      EventType.SLUR,
      eventAddress = eag(1),
      params = paramMapOf(
        EventParam.END to eag(1, graceOffset = semiquaver()),
        EventParam.IS_UP to true
      )
    )
    SSP(EventType.SLUR, EventParam.HARD_MID, Coord(0,-20), eag(1))
    assert(getArea("Slur", eag(1))?.area?.width!! > 0)
  }
}