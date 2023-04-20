package com.philblandford.kscore.engine.core.endtoend

import assertEqual
import com.philblandford.kscore.engine.core.representation.RepTest
import com.philblandford.kscore.engine.types.*


import com.philblandford.kscore.engine.duration.crotchet
import com.philblandford.kscore.engine.duration.minim

import org.junit.Test

class PedalTest : RepTest() {

  @Test
  fun testAddPedal() {
    SAE(EventType.PEDAL, params = paramMapOf(EventParam.END to ea(2)))
    RVA("Pedal", ea(1))
  }

  @Test
  fun testAddPedalOnlyOne() {
    SAE(EventType.PEDAL, params = paramMapOf(EventParam.END to ea(2)))
    val areas = getAreas("Pedal")
    assertEqual(1, areas.size)
  }
  
  @Test
  fun testAddPedalOverhang() {
    SAE(EventType.PEDAL, params = paramMapOf(EventParam.END to ea(EG().numBars)))
    val areas = getAreas("Pedal")
    assert(areas.size > 1)
  }

  @Test
  fun testAddPedalBarOfCrotchets() {
    repeat(4) { SMV(eventAddress = eav(1, crotchet().multiply(it))) }
    SAE(EventType.PEDAL, params = paramMapOf(EventParam.END to ea(1, minim(1))))
    val pedal = getArea("Pedal", ea(1))!!
    val note = getArea("Tadpole", eav(1, minim(1)).copy(id = 1))!!
    assert(pedal.coord.x + pedal.area.width > note.coord.x + note.area.width)
  }

  @Test
  fun testAddPedalHalfBar() {
    repeat(4) { SMV(eventAddress = eav(1, crotchet().multiply(it))) }
    SAE(EventType.PEDAL, params = paramMapOf(EventParam.END to ea(1, crotchet())))
    val pedal = getArea("Pedal", ea(1))!!
    val note = getArea("Tadpole", eav(1, minim()).copy(id = 1))!!
    assert(pedal.coord.x + pedal.area.width < note.coord.x)
  }

  @Test
  fun testPedalsAligned() {
    SMV(50, eventAddress = eav(2))
    SAE(EventType.PEDAL, params = paramMapOf(EventParam.END to ea(1, minim())))
    SAE(EventType.PEDAL,eav(2), params = paramMapOf(EventParam.END to ea(2, minim())))
    val one = getArea("Pedal", ea(1))!!.coord.y
    val two = getArea("Pedal", ea(2))!!.coord.y
    assertEqual(one, two)
  }

}