package com.philblandford.kscore.engine.core.endtoend

import assertEqual
import com.philblandford.kscore.engine.core.area.Coord
import com.philblandford.kscore.engine.types.*

import core.representation.*
import org.junit.Test

class VoltaTest : RepTest() {

  @Test
  fun testVolta() {
    SAE(EventType.VOLTA, ez(1), paramMapOf(EventParam.END to ez(1),
      EventParam.NUMBER to 1))
    RVA("Volta", ez(1))
  }

  @Test
  fun testVoltaShownOnce() {
    SAE(EventType.VOLTA, ez(1), paramMapOf(EventParam.END to ez(1),
      EventParam.NUMBER to 1))
    assertEqual(1, getAreas("Volta").size)
  }


  @Test
  fun testVoltaOverTopStave() {
    RCD(instruments = listOf("Violin", "Viola"))
    SAE(EventType.VOLTA, ez(1), paramMapOf(EventParam.END to ez(1),
      EventParam.NUMBER to 1))
    assert(isAbove("Volta", ez(1), "Stave", eas(1, 1, 1))!!)
  }

  @Test
  fun testAddNoteSecondStavesNotAffectVolta() {
    RCD(instruments = listOf("Violin", "Viola"))
    SAE(EventType.VOLTA, ez(1), paramMapOf(EventParam.END to ez(1),
      EventParam.NUMBER to 1))
    val oldY = getArea("Stave", eas(1,1,1))!!.coord.y -  getArea("Volta", ez(1))!!.coord.y
    SMV(74, eventAddress = easv(1, 2, 1))
    val newY = getArea("Stave", eas(1,1,1))!!.coord.y -  getArea("Volta", ez(1))!!.coord.y
    assertEqual(oldY, newY)
  }

  @Test
  fun testVoltaTwoBars() {
    SAE(EventType.VOLTA, ez(1), paramMapOf(EventParam.END to ez(2),
      EventParam.NUMBER to 1))
    assertEqual(1, getAreas("Volta").size)
  }

  @Test
  fun testTwoVoltas() {
    SAE(EventType.VOLTA, ez(1), paramMapOf(EventParam.END to ez(1),
      EventParam.NUMBER to 1))
    SAE(EventType.VOLTA, ez(2), paramMapOf(EventParam.END to ez(2),
      EventParam.NUMBER to 1))
    RVA("Volta", ez(1))
    RVA("Volta", ez(2))
  }

  @Test
  fun testVoltaOverhang() {
    val startBar = getStaveBar(1)
    SAE(EventType.VOLTA, ez(startBar-1), paramMapOf(EventParam.END to ez(startBar),
      EventParam.NUMBER to 1))
    assertEqual(2, getAreas("Volta").size)
  }

  @Test
  fun testVoltaOverhangStartsStartStave() {
    val startBar = getStaveBar(1)
    SAE(EventType.VOLTA, ez(startBar-1), paramMapOf(EventParam.END to ez(startBar+1),
      EventParam.NUMBER to 1))
    val stave = getArea("Stave", ea(startBar))
    val area = stave?.area?.getArea(EventType.VOLTA, ez(startBar-1))
    val barArea = stave?.area?.getArea(EventType.BAR, ea(startBar))
    assertEqual(barArea?.first?.coord?.x!! + 8, area?.first?.coord?.x)
  }
  
  @Test
  fun testVoltaShift() {
    SAE(EventType.VOLTA, ez(1), paramMapOf(EventParam.END to ez(1),
      EventParam.NUMBER to 1))
    val before = getArea("Volta", ez(1))!!
    SSP(EventType.VOLTA, EventParam.HARD_START, Coord(0, -20), ez(1))
    val after = getArea("Volta", ez(1))!!
    assertEqual(before.coord.y - 20, after.coord.y)
  }

}