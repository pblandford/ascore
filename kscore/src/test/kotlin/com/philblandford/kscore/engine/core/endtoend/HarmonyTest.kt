package com.philblandford.kscore.engine.core.endtoend

import assertEqual
import com.philblandford.kscore.engine.core.area.Coord
import com.philblandford.kscore.engine.types.*


import com.philblandford.kscore.engine.duration.crotchet

import com.philblandford.kscore.engine.core.representation.RepTest
import org.junit.Test

class HarmonyTest : RepTest() {

  @Test
  fun testAddHarmony() {
    SMV()
    SAE(EventType.HARMONY, eventAddress = ea(1), params = paramMapOf(EventParam.TEXT to "Cm7"))
    RVA("Harmony", ea(1))
  }

  @Test
  fun testAddHarmonyAboveNote() {
    SMV(120)
    SAE(EventType.HARMONY, eventAddress = ea(1), params = paramMapOf(EventParam.TEXT to "Cm7"))
    val note = getArea("Tadpole", eav(1).copy(id = 1))!!
    val harmony = getArea("Harmony", ea(1))!!
    assert(harmony.coord.y + harmony.area.height < note.coord.y)
  }

  @Test
  fun testAddHarmoniesAligned() {
    SMV(90)
    SAE(EventType.HARMONY, eventAddress = ea(1), params = paramMapOf(EventParam.TEXT to "Cm7"))
    SAE(EventType.HARMONY, eventAddress = ea(2), params = paramMapOf(EventParam.TEXT to "Cm7"))

    val h1 = getArea("Harmony", ea(1))!!
    val h2 = getArea("Harmony", ea(2))!!
    assertEqual(h1.coord.y, h2.coord.y)
  }

  @Test
  fun testHarmoniesDontCollide() {
    SMV()
    SMV(eventAddress = eav(1, crotchet()))
    SAE(EventType.HARMONY, eventAddress = ea(1), params = paramMapOf(EventParam.TEXT to "C#m7/F#"))
    SAE(EventType.HARMONY, eventAddress = ea(1, crotchet()), params = paramMapOf(EventParam.TEXT to "C#m7/F#"))
    assert(isLeft("Harmony", ea(1), "Harmony", ea(1, crotchet())) == true)
  }

  @Test
  fun testMoveHarmonies() {
    SMV()
    SAE(EventType.HARMONY, eventAddress = ea(1), params = paramMapOf(EventParam.TEXT to "Cm7"))
    val oldOffset = getArea("StaveLines", ea(1))!!.coord.y - getArea("Harmony", ea(1))!!.coord.y
    SSO(EventParam.OPTION_HARMONY_OFFSET, Coord(0,-20))
    val newOffset = getArea("StaveLines", ea(1))!!.coord.y - getArea("Harmony", ea(1))!!.coord.y
    assertEqual(oldOffset + 20, newOffset)
  }

  @Test
  fun testMoveSingleHarmony() {
    SAE(EventType.HARMONY, eventAddress = ea(1), params = paramMapOf(EventParam.TEXT to "Cm7"))
    val oldOffset = getArea("StaveLines", ea(1))!!.coord.y - getArea("Harmony", ea(1))!!.coord.y
    SSP(EventType.HARMONY, EventParam.HARD_START, Coord(0,-20), ea(1))
    val newOffset = getArea("StaveLines", ea(1))!!.coord.y - getArea("Harmony", ea(1))!!.coord.y
    assertEqual(oldOffset + 20, newOffset)
  }
}