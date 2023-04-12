package com.philblandford.kscore.engine.core.endtoend

import assertEqual
import com.philblandford.kscore.engine.types.*


import com.philblandford.kscore.engine.duration.crotchet
import com.philblandford.kscore.engine.duration.minim
import com.philblandford.kscore.engine.duration.semibreve

import core.representation.*
import org.junit.Test

class WedgeTest : RepTest() {

  @Test
  fun testAddWedge() {
    SAE(
      EventType.WEDGE,
      params = paramMapOf(EventParam.END to ea(2),
        EventParam.IS_UP to true,
        EventParam.TYPE to WedgeType.CRESCENDO)
    )
    RVA("Wedge", ea(1))
  }

  @Test
  fun testAddWedgeOnlyOne() {
    SAE(
      EventType.WEDGE,
      params = paramMapOf(EventParam.END to ea(2), EventParam.TYPE to WedgeType.CRESCENDO)
    )
    val areas = getAreas("Wedge")
    assertEqual(1, areas.size)
  }

  @Test
  fun testAddWedgeAbove() {
    SAE(
      EventType.WEDGE,
      params = paramMapOf(EventParam.END to ea(2),
        EventParam.IS_UP to true,
        EventParam.TYPE to WedgeType.CRESCENDO)
    )
    assert(isAbove("Wedge", ea(1), "Stave", ea(1))!!)
  }

  @Test
  fun testAddWedgeBelow() {
    SMV(60)
    SAE(
      EventType.WEDGE,
      params = paramMapOf(EventParam.END to ea(2),
        EventParam.IS_UP to false,
        EventParam.TYPE to WedgeType.CRESCENDO)
    )
    val wedge = RCoord("Wedge", ea(1).copy(id = 1))!!
    val note = RCoord("Tadpole", eav(1).copy(id = 1))!!
    assert(wedge.y > note.y)
  }

  @Test
  fun testAddWedgeOverhang() {
    SAE(
      EventType.WEDGE,
      params = paramMapOf(EventParam.END to ea(EG().numBars), EventParam.TYPE to WedgeType.CRESCENDO)
    )
    val areas = getAreas("Wedge")
    val staveAreas = getAreas("Stave")
    assertEqual(staveAreas.size, areas.size)
  }

  @Test
  fun testAddWedgeBarOfCrotchets() {
    repeat(4) { SMV(eventAddress = eav(1, crotchet().multiply(it))) }
    SAE(
      EventType.WEDGE,
      params = paramMapOf(EventParam.END to ea(1, minim(1)),
        EventParam.IS_UP to true,
        EventParam.TYPE to WedgeType.CRESCENDO)
    )
    val wedge = getArea("Wedge", ea(1))!!
    val note = getArea("Tadpole", eav(1, minim(1)).copy(id = 1))!!
    assert(wedge.coord.x + wedge.area.width > note.coord.x + note.area.width)
  }

  @Test
  fun testAddWedgeSemibreve() {
    SMV(duration = semibreve())
    SAE(
      EventType.WEDGE,
      params = paramMapOf(EventParam.END to ea(1),
        EventParam.IS_UP to true,
        EventParam.TYPE to WedgeType.CRESCENDO)
    )
    RVA("Wedge", ea(1))
  }

  @Test
  fun testAddWedgesAligned() {
    SAE(
      EventType.WEDGE,
      params = paramMapOf(EventParam.END to ea(1),
        EventParam.IS_UP to true,
        EventParam.TYPE to WedgeType.CRESCENDO)
    )
    SMV(58, eventAddress = ea(2))
    SAE(
      EventType.WEDGE, ea(2),
      paramMapOf(EventParam.END to ea(2),
        EventParam.IS_UP to true,
        EventParam.TYPE to WedgeType.CRESCENDO)
    )
    val one = getArea("Wedge", ea(1))?.coord!!
    val two = getArea("Wedge", ea(2))?.coord!!
    assertEqual(one.y, two.y)
  }

  @Test
  fun testAddWedgesUpdownNotAligned() {
    SAE(
      EventType.WEDGE,
      params = paramMapOf(EventParam.END to ea(1),
        EventParam.IS_UP to true,
        EventParam.TYPE to WedgeType.CRESCENDO)
    )
    SAE(
      EventType.WEDGE, ea(2),
      paramMapOf(EventParam.END to ea(2),
        EventParam.IS_UP to false,
        EventParam.TYPE to WedgeType.CRESCENDO)
    )
    val one = getArea("Wedge", ea(1))?.coord!!
    val two = getArea("Wedge", ea(2).copy(id = 1))?.coord!!
    assert(one.y != two.y)
  }
}